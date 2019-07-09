package de.adito.git.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.*;
import de.adito.git.impl.dag.DAGFilterIterator;
import de.adito.git.impl.data.*;
import de.adito.git.impl.ssh.ISshProvider;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.adito.git.impl.Util.getRelativePath;

/**
 * @author A.Arnold 21.09.2018
 */
public class RepositoryImpl implements IRepository
{

  private static final String CHECKOUT_FORMATTED_STRING = "git checkout %s";
  private final ISshProvider sshProvider;
  private final IDataFactory dataFactory;
  private final Logger logger = Logger.getLogger(RepositoryImpl.class.getName());
  private final Git git;
  private final Observable<Optional<List<IBranch>>> branchList;
  private final Observable<List<ITag>> tagList;
  private final Observable<Optional<IFileStatus>> status;
  private final Observable<Optional<IRepositoryState>> currentStateObservable;
  private final IFileSystemUtil fileSystemUtil;
  private final IFileSystemObserver fileSystemObserver;

  @Inject
  public RepositoryImpl(IFileSystemObserverProvider pFileSystemObserverProvider,
                        IFileSystemUtil pIFileSystemUtil, ISshProvider pSshProvider, IDataFactory pDataFactory,
                        @Assisted IRepositoryDescription pRepositoryDescription) throws IOException
  {
    fileSystemUtil = pIFileSystemUtil;
    sshProvider = pSshProvider;
    dataFactory = pDataFactory;
    git = new Git(FileRepositoryBuilder.create(new File(pRepositoryDescription.getPath() + File.separator + ".git")));

    fileSystemObserver = pFileSystemObserverProvider.getFileSystemObserver(pRepositoryDescription);
    // listen for changes in the fileSystem for the status command
    status = Observable.create(new _FileSystemChangeObservable(fileSystemObserver))
        .throttleLatest(500, TimeUnit.MILLISECONDS)
        .subscribeWith(BehaviorSubject.createDefault(Optional.of(RepositoryImplHelper.status(git))))
        .observeOn(Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())));

    branchList = status.map(pStatus -> Optional.of(RepositoryImplHelper.branchList(git)))
        .share()
        .subscribeWith(BehaviorSubject.createDefault(Optional.of(RepositoryImplHelper.branchList((git)))));
    currentStateObservable = status.map(pStatus -> RepositoryImplHelper.currentState(git, this::getBranch))
        .share()
        .subscribeWith(BehaviorSubject.createDefault(RepositoryImplHelper.currentState(git, this::getBranch)));
    tagList = status.map(pStatus -> git.tagList().call().stream().map(TagImpl::new).collect(Collectors.<ITag>toList()))
        .distinctUntilChanged()
        .share()
        .subscribeWith(BehaviorSubject.createDefault(List.of()));
  }

  @Override
  public Observable<Optional<IRepositoryState>> getRepositoryState()
  {
    return currentStateObservable;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(List<File> pAddList) throws AditoGitException
  {
    logger.log(Level.FINE, () -> String.format("git add %s", pAddList));
    AddCommand adder = git.add();
    for (File file : pAddList)
    {
      adder.addFilepattern(getRelativePath(file, git));
    }
    try
    {
      adder.call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to add Files to staging area", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String commit(@NotNull String pMessage) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git commit -m \"%s\"", pMessage));
    CommitCommand commit = git.commit();
    RevCommit revCommit;
    try
    {
      // add all untracked files
      List<File> files = status.blockingFirst()
          .map(IFileStatus::getUntracked)
          .orElse(Set.of())
          .stream()
          .map(File::new)
          .collect(Collectors.toList());
      if (!files.isEmpty())
        add(files);

      // commit files
      revCommit = commit.setMessage(pMessage).call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to commit to local Area", e);
    }
    if (revCommit == null)
    {
      return "";
    }
    return ObjectId.toString(revCommit.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String commit(@NotNull String pMessage, List<File> pFileList, boolean pIsAmend) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git commit %s -m \"%s\" %s", pFileList, pMessage, pIsAmend ? "--amend" : ""));
    CommitCommand commit = git.commit();
    RevCommit revCommit;
    for (File file : pFileList)
    {
      commit.setOnly(getRelativePath(file, git));
    }
    try
    {
      add(pFileList);
      revCommit = commit.setMessage(pMessage).setAmend(pIsAmend).call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to commit to local Area", e);
    }
    if (revCommit == null)
    {
      return "";
    }
    return ObjectId.toString(revCommit.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, EPushResult> push(boolean pIsPushTags, @Nullable String pRemoteName) throws GitTransportFailureException
  {
    logger.log(Level.INFO, "git push {0}", pIsPushTags ? "--tags" : "");
    Map<String, EPushResult> resultMap = new HashMap<>();
    try
    {
      TransportConfigCallback transportConfigCallback = sshProvider.getTransportConfigCallBack(getConfig());
      PushCommand push = git.push().setTransportConfigCallback(transportConfigCallback);
      if (pIsPushTags)
        push.setPushTags();
      if (pRemoteName != null)
        push.setRemote(pRemoteName);
      Iterable<PushResult> pushResults = push.call();
      for (PushResult pushResult : pushResults)
      {
        for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates())
        {
          if (remoteRefUpdate.getStatus() != RemoteRefUpdate.Status.OK && remoteRefUpdate.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE)
          {
            resultMap.put(remoteRefUpdate.getRemoteName(), EnumMappings.toPushResult(remoteRefUpdate.getStatus()));
          }
        }
      }
    }
    catch (TransportException pE)
    {
      throw new GitTransportFailureException(pE);
    }
    catch (JGitInternalException | GitAPIException e)
    {
      throw new IllegalStateException("Unable to push into remote Git repository", e);
    }
    return resultMap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IRebaseResult pull(boolean pDoAbort) throws AditoGitException
  {
    try
    {
      if (pDoAbort)
      {
        logger.log(Level.INFO, "git rebase --abort");
        RebaseCommand rebaseCommand = git.rebase();
        rebaseCommand.setOperation(RebaseCommand.Operation.ABORT);
        RebaseResult rebaseResult = rebaseCommand.call();
        if (rebaseResult.getStatus() == RebaseResult.Status.ABORTED)
        {
          return new RebaseResultImpl(Collections.emptyList(), rebaseResult.getStatus());
        }
        else
          throw new AditoGitException("could not abort rebase");
      }
      IRebaseResult iRebaseResult;
      if (!git.getRepository().getRepositoryState().isRebasing())
      {
        logger.log(Level.INFO, "git pull --rebase");
        String currentHeadName = git.getRepository().getFullBranch();
        String targetName = RepositoryImplHelper.getRemoteTrackingBranch(git, null);
        PullCommand pullCommand = git.pull();
        pullCommand.setRebase(true);
        pullCommand.setTransportConfigCallback(sshProvider.getTransportConfigCallBack(getConfig()));

        PullResult pullResult = pullCommand.call();
        String currentCommitId = pullResult.getRebaseResult().getCurrentCommit() == null ?
            null : ObjectId.toString(pullResult.getRebaseResult().getCurrentCommit().getId());
        iRebaseResult = _handlePullResult(pullResult::getRebaseResult, currentCommitId, targetName,
                                          new CommitImpl(RepositoryImplHelper.findForkPoint(git, currentHeadName, targetName)));
      }
      else
      {
        logger.log(Level.INFO, "git rebase --continue");
        Set<String> conflictingFiles = status.blockingFirst().map(IFileStatus::getConflicting).orElse(Collections.emptySet());
        String targetName;
        String currentHeadName;
        try (BufferedReader reader = new BufferedReader(new FileReader(
            new File(git.getRepository().getDirectory().getAbsolutePath(), "rebase-merge/head"))))
        {
          currentHeadName = reader.readLine();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(
            new File(git.getRepository().getDirectory().getAbsolutePath(), "rebase-merge/onto"))))
        {
          targetName = reader.readLine();
        }
        if (!conflictingFiles.isEmpty())
        {
          RevCommit forkCommit = RepositoryImplHelper.findForkPoint(git, targetName, currentHeadName);
          return new RebaseResultImpl(RepositoryImplHelper.getMergeConflicts(git, targetName, currentHeadName, new CommitImpl(forkCommit),
                                                                             conflictingFiles, this::diff),
                                      RebaseResult.Status.CONFLICTS);
        }
        RebaseCommand rebaseCommand = git.rebase();
        rebaseCommand.setOperation(RebaseCommand.Operation.CONTINUE);
        RebaseResult rebaseResult = rebaseCommand.call();
        iRebaseResult = _handlePullResult(() -> rebaseResult, currentHeadName, targetName,
                                          rebaseResult.getCurrentCommit() == null ? null
                                              : new CommitImpl(rebaseResult.getCurrentCommit().getParent(0)));
      }
      return iRebaseResult;
    }
    catch (RefNotAdvertisedException pException)
    {
      throw new MissingTrackedBranchException("The current Branch does not have a corresponding remote branch, " +
                                                  "please switch to a branch that also exists in the remote repository", pException);
    }
    catch (IOException | GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @Override
  public ICherryPickResult cherryPick(List<ICommit> pCommitList) throws AditoGitException
  {
    CherryPickCommand cherryPickCommand = git.cherryPick();
    for (ICommit commit : pCommitList)
    {
      cherryPickCommand.include(ObjectId.fromString(commit.getId()));
    }
    try
    {
      CherryPickResult cherryPickResult = cherryPickCommand.call();
      List<IMergeDiff> mergeConflicts = new ArrayList<>();
      ICommit cherryPickCommit = null;
      if (cherryPickResult == CherryPickResult.CONFLICT)
      {
        cherryPickCommit = getCommit(ObjectId.toString(git.getRepository().readCherryPickHead()));
        String headId = ObjectId.toString(git.getRepository().resolve(Constants.HEAD));
        mergeConflicts = RepositoryImplHelper.getMergeConflicts(git, headId, cherryPickCommit.getId(), cherryPickCommit.getParents().get(0),
                                                                RepositoryImplHelper.status(git).getConflicting(), this::diff);
      }
      return new CherryPickResultImpl(cherryPickResult, cherryPickCommit, mergeConflicts);
    }
    catch (GitAPIException | IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @NotNull
  private IRebaseResult _handlePullResult(@NotNull Supplier<RebaseResult> pResultSupplier,
                                          String pCurrHeadName, String pTargetName, CommitImpl pForkPoint) throws AditoGitException
  {
    if (!pResultSupplier.get().getStatus().isSuccessful())
    {
      Set<String> conflictFilesSet;
      List<String> conflictFiles = pResultSupplier.get().getConflicts();
      if (conflictFiles == null)
      {
        IFileStatus currentStatus = RepositoryImplHelper.status(git);
        conflictFilesSet = currentStatus.getConflicting();
      }
      else
      {
        conflictFilesSet = Set.copyOf(conflictFiles);
      }
      if (!conflictFilesSet.isEmpty())
      {
        return new RebaseResultImpl(RepositoryImplHelper.getMergeConflicts(git, pCurrHeadName, pTargetName, pForkPoint, conflictFilesSet, this::diff),
                                    pResultSupplier.get().getStatus());
      }
    }
    return new RebaseResultImpl(Collections.emptyList(), pResultSupplier.get().getStatus());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fetch() throws AditoGitException
  {
    fetch(true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fetch(boolean pPrune) throws AditoGitException
  {
    logger.log(Level.INFO, "git fetch (with prune = {0})", pPrune);
    try
    {
      git.fetch().setTransportConfigCallback(sshProvider.getTransportConfigCallBack(getConfig())).setRemoveDeletedRefs(pPrune).call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @Override
  public List<IFileChangeChunk> diffOffline(@NotNull String pString, @NotNull File pFile) throws IOException
  {
    RawText fileContents = new RawText(Files.readAllBytes(pFile.toPath()));
    RawText currentFileContents = new RawText(pString.getBytes());

    EditList linesChanged = new HistogramDiff().diff(RawTextComparator.WS_IGNORE_TRAILING, fileContents, currentFileContents);
    List<IFileChangeChunk> changeChunks = new ArrayList<>();
    for (Edit edit : linesChanged)
    {
      changeChunks.add(new FileChangeChunkImpl(edit, "", "", EnumMappings.toEChangeType(edit.getType())));
    }
    return changeChunks;
  }

  @Override
  public List<IFileChangeChunk> diff(@NotNull String pFileContents, File pCompareWith) throws IOException
  {

    String headId = ObjectId.toString(git.getRepository().resolve(Constants.HEAD));


    RawText headFileContents = new RawText(getFileContents(getFileVersion(headId, Util.getRelativePath(pCompareWith, git)), pCompareWith).getFileContent().getBytes());
    RawText currentFileContents = new RawText(pFileContents.getBytes());

    EditList linesChanged = new HistogramDiff().diff(RawTextComparator.WS_IGNORE_TRAILING, headFileContents, currentFileContents);
    List<IFileChangeChunk> changeChunks = new ArrayList<>();
    for (Edit edit : linesChanged)
    {
      changeChunks.add(new FileChangeChunkImpl(edit, "", "", EnumMappings.toEChangeType(edit.getType())));
    }
    return changeChunks;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull List<IFileDiff> diff(@NotNull ICommit pOriginal, @NotNull ICommit pCompareTo)
  {
    try
    {
      List<IFileDiff> listDiffImpl = new ArrayList<>();

      List<DiffEntry> listDiff = RepositoryImplHelper.doDiff(git, ObjectId.fromString(pOriginal.getId()), ObjectId.fromString(pCompareTo.getId()));

      if (listDiff != null)
      {
        for (DiffEntry diff : listDiff)
        {
          try (DiffFormatter formatter = new DiffFormatter(null))
          {
            formatter.setRepository(git.getRepository());
            FileHeader fileHeader = formatter.toFileHeader(diff);
            IFileContentInfo oldFileContent = VOID_PATH.equals(diff.getOldPath()) ? new FileContentInfoImpl("", StandardCharsets.UTF_8)
                : getFileContents(getFileVersion(pCompareTo.getId(), diff.getOldPath()));
            IFileContentInfo newFileContent = VOID_PATH.equals(diff.getNewPath()) ? new FileContentInfoImpl("", StandardCharsets.UTF_8)
                : getFileContents(getFileVersion(pOriginal.getId(), diff.getNewPath()));
            listDiffImpl.add(new FileDiffImpl(diff, fileHeader, getTopLevelDirectory(), oldFileContent, newFileContent));
          }
        }
      }
      return listDiffImpl;
    }
    catch (AditoGitException | IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull List<IFileDiff> diff(@Nullable List<File> pFilesToDiff, @Nullable ICommit pCompareWith)
  {
    logger.log(Level.INFO, () -> String.format("git diff %s %s", pCompareWith == null ? "" : pCompareWith.getId(), pFilesToDiff));
    try
    {
      List<IFileDiff> returnList = new ArrayList<>();
      _checkExcludedIgnoredFiles(pFilesToDiff);

      // prepare the TreeIterators for the local working copy and the files in HEAD
      FileTreeIterator fileTreeIterator = new FileTreeIterator(git.getRepository());
      fileTreeIterator.setWalkIgnoredDirectories(true);
      ObjectId compareWithId = git.getRepository().resolve(pCompareWith == null ? Constants.HEAD : pCompareWith.getId());
      CanonicalTreeParser treeParser = RepositoryImplHelper.prepareTreeParser(git.getRepository(), compareWithId);

      // Use the DiffFormatter to retrieve a list of changes
      DiffFormatter diffFormatter = new DiffFormatter(null);
      diffFormatter.setRepository(git.getRepository());
      diffFormatter.setDiffComparator(RawTextComparator.WS_IGNORE_TRAILING);
      List<TreeFilter> pathFilters = new ArrayList<>();
      if (pFilesToDiff != null)
      {
        for (File fileToDiff : pFilesToDiff)
        {
          pathFilters.add(PathFilter.create(Util.getRelativePath(fileToDiff, git)));
        }
        if (pathFilters.size() > 1)
        {
          diffFormatter.setPathFilter(OrTreeFilter.create(pathFilters));
        }
        else if (!pathFilters.isEmpty())
        {
          diffFormatter.setPathFilter(pathFilters.get(0));
        }
      }
      List<DiffEntry> diffList = diffFormatter.scan(treeParser, fileTreeIterator);

      for (DiffEntry diffEntry : diffList)
      {
        // check if the diff is of a file in  the passed list, except if filesToDiff is null (all files are valid).
        if (pFilesToDiff == null
            || pFilesToDiff.stream().anyMatch(file -> getRelativePath(file, git).equals(diffEntry.getNewPath()))
            || pFilesToDiff.stream().anyMatch(file -> getRelativePath(file, git).equals(diffEntry.getOldPath())))
        {
          FileHeader fileHeader = diffFormatter.toFileHeader(diffEntry);
          // Can't use the ObjectLoader or anything similar provided by JGit because it wouldn't find the blob, so parse file by hand
          byte[] newFileBytes = null;
          if (!VOID_PATH.equals(diffEntry.getNewPath()))
          {
            newFileBytes = Files.readAllBytes(new File(getTopLevelDirectory(), diffEntry.getNewPath()).toPath());
          }
          IFileContentInfo oldFileContents = VOID_PATH.equals(diffEntry.getOldPath()) ? new FileContentInfoImpl("", StandardCharsets.UTF_8)
              : getFileContents(getFileVersion(ObjectId.toString(compareWithId), diffEntry.getOldPath()));
          Charset encoding = fileSystemUtil.getEncoding(new File(getTopLevelDirectory(), diffEntry.getNewPath()));
          returnList.add(new FileDiffImpl(diffEntry, fileHeader, getTopLevelDirectory(),
                                          oldFileContents, newFileBytes == null ? new FileContentInfoImpl("", StandardCharsets.UTF_8)
                                              : new FileContentInfoImpl(new String(newFileBytes, encoding), encoding)));
        }
      }
      return returnList;
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * @param pFileList List of files to check
   * @throws IOException if an error occurs during reading the ignore/exclude file
   */
  private void _checkExcludedIgnoredFiles(List<File> pFileList) throws IOException
  {
    File gitIgnore = new File(getTopLevelDirectory(), ".gitignore");
    File gitExclude = new File(git.getRepository().getDirectory(), "info/exclude");
    IgnoreNode ignoreNode = new IgnoreNode();
    if (gitIgnore.exists())
      ignoreNode.parse(new FileInputStream(gitIgnore));
    if (gitExclude.exists())
      ignoreNode.parse(new FileInputStream(gitExclude));
    if (pFileList != null)
    {
      for (File file : pFileList)
      {
        Boolean isIgnored = ignoreNode.checkIgnored(getRelativePath(file, git), false);
        if (isIgnored != null && isIgnored)
        {
          throw new RuntimeException("File " + file.getAbsolutePath() + " is in exclude or ignore file, cannot perform a diff");
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IFileContentInfo getFileContents(String pIdentifier, File pFile) throws IOException
  {
    Charset encoding = fileSystemUtil.getEncoding(pFile);
    logger.log(Level.FINE, () -> String.format("git: Encoding for file %s in version with id %s: %s", pFile.getAbsolutePath(), pIdentifier, encoding.toString()));
    ObjectLoader loader = git.getRepository().open(ObjectId.fromString(pIdentifier));
    return new FileContentInfoImpl(new String(loader.getBytes(), encoding), encoding);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IFileContentInfo getFileContents(String pIdentifier) throws IOException
  {
    ObjectLoader loader = git.getRepository().open(ObjectId.fromString(pIdentifier));
    byte[] bytes = loader.getBytes();
    Charset encoding = Util.getEncoding(bytes, fileSystemUtil);
    logger.log(Level.FINE, () -> String.format("git: Encoding for Object with identifier %s: %s", pIdentifier, encoding));
    return new FileContentInfoImpl(new String(bytes, encoding), encoding);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IFileChangeType getStatusOfSingleFile(@NotNull File pFile)
  {
    try
    {
      IndexDiff diff = new IndexDiff(git.getRepository(), "HEAD", new FileTreeIterator(git.getRepository()));
      if (getRelativePath(pFile, git).isEmpty())
        return new FileChangeTypeImpl(pFile, pFile, EChangeType.SAME);
      diff.setFilter(PathFilterGroup.createFromStrings(getRelativePath(pFile, git)));
      diff.diff();
      IFileChangeType result;
      if (!diff.getAdded().isEmpty())
      {
        result = new FileChangeTypeImpl(pFile, pFile, EChangeType.ADD);
      }
      else if (!diff.getChanged().isEmpty())
      {
        result = new FileChangeTypeImpl(pFile, pFile, EChangeType.CHANGED);
      }
      else if (!diff.getRemoved().isEmpty())
      {
        result = new FileChangeTypeImpl(new File(VOID_PATH), pFile, EChangeType.DELETE);
      }
      else if (!diff.getModified().isEmpty())
      {
        result = new FileChangeTypeImpl(pFile, pFile, EChangeType.MODIFY);
      }
      else if (!diff.getUntracked().isEmpty())
      {
        result = new FileChangeTypeImpl(pFile, new File(VOID_PATH), EChangeType.NEW);
      }
      else if (!diff.getConflicting().isEmpty())
      {
        result = new FileChangeTypeImpl(pFile, pFile, EChangeType.CONFLICTING);
      }
      else
      {
        result = new FileChangeTypeImpl(pFile, pFile, EChangeType.SAME);
      }
      return result;
    }
    catch (IOException pE)
    {
      throw new RuntimeException("Can't check Status of file: " + pFile, pE);
    }
    //return new FileChangeTypeImpl(pFile, EChangeType.SAME);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFileVersion(String pCommitId, String pFilename) throws IOException
  {
    try (RevWalk revWalk = new RevWalk(git.getRepository()))
    {
      RevCommit commit = revWalk.parseCommit(ObjectId.fromString(pCommitId));
      RevTree tree = commit.getTree();

      // find the specific file
      try (TreeWalk treeWalk = new TreeWalk(git.getRepository()))
      {
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(pFilename));
        if (!treeWalk.next())
        {
          throw new IllegalStateException("Could not find file " + pFilename + " in repository " + getDirectory());
        }
        return ObjectId.toString(treeWalk.getObjectId(0));
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean clone(@NotNull String pUrl, @NotNull File pLocalPath)
  {
    logger.log(Level.INFO, () -> String.format("git clone %s %s", pUrl, pLocalPath));
    if (Util.isDirEmpty(pLocalPath))
    {
      try
      {
        TransportConfigCallback transportConfigCallback = sshProvider.getTransportConfigCallBack(getConfig());
        CloneCommand cloneRepo = Git.cloneRepository()
            .setTransportConfigCallback(transportConfigCallback)
            .setURI(pUrl)
            .setDirectory(new File(pLocalPath, ""));
        cloneRepo.call();
      }
      catch (GitAPIException e)
      {
        throw new RuntimeException(e);
      }
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Observable<Optional<IFileStatus>> getStatus()
  {
    return status;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void ignore(@NotNull List<File> pFiles) throws IOException
  {
    logger.log(Level.INFO, () -> String.format("git: Writing files %s into the .gitignore", pFiles));
    File gitIgnore = new File(git.getRepository().getDirectory().getParent(), ".gitignore");
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true)))
    {
      for (File file : pFiles)
      {
        outputStream.write((getRelativePath(file, git) + "\n").getBytes());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void exclude(@NotNull List<File> pFiles) throws IOException
  {
    logger.log(Level.INFO, () -> String.format("git: Writing files %s into the exclude file (info/exclude)", pFiles));
    File gitIgnore = new File(git.getRepository().getDirectory(), "info/exclude");
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(gitIgnore, true)))
    {
      for (File file : pFiles)
      {
        outputStream.write((getRelativePath(file, git) + "\n").getBytes());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void revertWorkDir(@NotNull List<File> pFiles) throws AditoGitException
  {
    List<String> filesToCheckout = new ArrayList<>();
    List<String> newFiles = new ArrayList<>();
    status.blockingFirst().ifPresent(pStatus -> {
      newFiles.addAll(pStatus.getAdded());
      newFiles.addAll(pStatus.getUntracked());
    });
    try
    {
      ResetCommand resetCommand = git.reset();
      for (File file : pFiles)
      {
        String relativePath = Util.getRelativePath(file, git);
        if (newFiles.stream().noneMatch(pFilePath -> pFilePath.equals(relativePath)))
          filesToCheckout.add(relativePath);
        else
          Files.deleteIfExists(file.toPath());
        resetCommand.addPath(relativePath);
      }
      logger.log(Level.INFO, () -> String.format(CHECKOUT_FORMATTED_STRING, filesToCheckout));
      resetCommand.call();
      if (!filesToCheckout.isEmpty())
      {
        CheckoutCommand checkoutCommand = git.checkout();
        checkoutCommand.addPaths(filesToCheckout);
        checkoutCommand.setStartPoint(git.getRepository().parseCommit(git.getRepository().resolve(Constants.HEAD)));
        checkoutCommand.call();
      }
    }
    catch (GitAPIException | IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(@NotNull List<File> pFiles) throws AditoGitException
  {
    try
    {
      ResetCommand resetCommand = git.reset();
      for (File file : pFiles)
        resetCommand.addPath(Util.getRelativePath(file, git));
      logger.log(Level.INFO, () -> String.format("git reset -- %s", pFiles));
      resetCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(@NotNull String pIdentifier, @NotNull EResetType pResetType) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git reset --%s %s", pResetType, pIdentifier));
    try
    {
      ResetCommand resetCommand = git.reset();
      resetCommand.setRef(pIdentifier);
      if (pResetType == EResetType.HARD)
        resetCommand.setMode(ResetCommand.ResetType.HARD);
      else if (pResetType == EResetType.MIXED)
        resetCommand.setMode(ResetCommand.ResetType.MIXED);
      else resetCommand.setMode(ResetCommand.ResetType.SOFT);
      resetCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createBranch(@NotNull String pBranchName, @Nullable ICommit pStartPoint, boolean pCheckout) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git branch %s", pBranchName));
    try
    {
      List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

      if (refs.stream().anyMatch(ref -> ref.getName().equals("refs/heads/" + pBranchName)))
      {
        throw new AditoGitException("Branch already exist. " + pBranchName);
      }

      RevCommit startingPoint = pStartPoint == null ? null : git.getRepository().parseCommit(ObjectId.fromString(pStartPoint.getId()));
      git.branchCreate().setName(pBranchName).setStartPoint(startingPoint).call();
      // the next line of code is for an automatically push after creating a branch
      if (pCheckout)
      {
        logger.log(Level.INFO, () -> String.format(CHECKOUT_FORMATTED_STRING, pBranchName));
        String fullBranchString = pBranchName;
        if (!fullBranchString.startsWith("refs/heads"))
          fullBranchString = Paths.get("refs", "heads", fullBranchString).toString().replace("\\", "/");
        checkout(getBranch(fullBranchString));
      }
    }
    catch (GitAPIException | IOException e)
    {
      throw new AditoGitException("Unable to create new branch: " + pBranchName, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteBranch(@NotNull String pBranchName, boolean pDeleteRemoteBranch, boolean pIsForceDelete) throws AditoGitException
  {
    String destination = "refs/heads/" + pBranchName;
    try
    {
      git.branchDelete()
          .setForce(pIsForceDelete)
          .setBranchNames(destination)
          .call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to delete the branch: " + pBranchName + ", reason: " + e.getMessage(), e);
    }
    if (pDeleteRemoteBranch)
    {
      RefSpec refSpec = new RefSpec().setSource(null).setDestination(destination);
      try
      {
        TransportConfigCallback transportConfigCallback = sshProvider.getTransportConfigCallBack(getConfig());
        git.push().setTransportConfigCallback(transportConfigCallback).setRefSpecs(refSpec).setRemote("origin").call();
      }
      catch (GitAPIException e)
      {
        throw new AditoGitException("Unable to push the delete branch comment @ " + pBranchName, e);
      }
    }
  }

  @NotNull
  public Observable<Optional<IBlame>> getBlame(@NotNull File pFile)
  {

    // TODO: 25.01.2019 Map of observables? -> actualy for each getBlame new observable
    return Observable.create(emitter -> {
      BlameCommand blameCommand = git
          .blame()
          .setFilePath(getRelativePath(pFile, git))
          .setTextComparator(RawTextComparator.WS_IGNORE_ALL);
      emitter.onNext(Optional.of(new BlameImpl(blameCommand.call())));
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkout(@NotNull String pId) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format(CHECKOUT_FORMATTED_STRING, pId));
    try
    {
      git.checkout().setName(pId).call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkoutFileVersion(@NotNull String pId, List<String> pPaths) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git checkout %s %s", pId, pPaths));
    try
    {
      git.checkout().setStartPoint(pId).addPaths(pPaths).call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkout(@NotNull IBranch pBranch) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format(CHECKOUT_FORMATTED_STRING, pBranch));
    CheckoutCommand checkout = git.checkout().setName(pBranch.getName()).setCreateBranch(false).setStartPoint(pBranch.getName());
    try
    {
      checkout.call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to checkout Branch " + pBranch.getName(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkoutRemote(@NotNull IBranch pBranch, @NotNull String pLocalName) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git checkout %s %s", pLocalName, pBranch));
    CheckoutCommand checkoutCommand = git.checkout().
        setCreateBranch(true).
        setName(pLocalName).
        setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
        setStartPoint(pBranch.getName());
    try
    {
      checkoutCommand.call();
    }
    catch (GitAPIException e)
    {
      throw new AditoGitException("Unable to checkout remote Branch " + pBranch.getName(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public List<IMergeDiff> getConflicts() throws AditoGitException
  {
    try
    {
      Set<String> conflictingFiles = status.blockingFirst().map(IFileStatus::getConflicting).orElse(Collections.emptySet());
      logger.log(Level.INFO, () -> String.format("found conflicting files: %s", conflictingFiles));
      if (!conflictingFiles.isEmpty())
      {
        File aConflictingFile = new File(git.getRepository().getDirectory().getParent(), conflictingFiles.iterator().next());
        String currentBranchId = git.getRepository().getBranch();
        String conflictingBranchId = RepositoryImplHelper.getConflictingBranch(aConflictingFile);
        if (conflictingBranchId == null)
        {
          if (git.getRepository().readCherryPickHead() != null)
          {
            conflictingBranchId = ObjectId.toString(git.getRepository().readCherryPickHead());
          }
          else if (new File(git.getRepository().getDirectory(), "rebase-merge/head").exists())
          {
            conflictingBranchId = Files.readAllLines(new File(git.getRepository().getDirectory(), "rebase-merge/head").toPath()).get(0);
          }
          else
            throw new TargetBranchNotFoundException("Cannot determine target branch of conflict",
                                                    getCommit(ObjectId.toString(git.getRepository().readOrigHead())));
        }
        if (conflictingBranchId.contains("Stashed changes"))
        {
          List<ICommit> stashedCommits = RepositoryImplHelper.getStashedCommits(git);
          if (stashedCommits.size() > 1)
            throw new AmbiguousStashCommitsException("num stashed commits: " + stashedCommits.size());
          else if (!stashedCommits.isEmpty())
            return RepositoryImplHelper.getStashConflictMerge(git, conflictingFiles, stashedCommits.get(0).getId(), this::diff, this::diff);
          else throw new AditoGitException("Conflict from failed un-stashing, but no more stashed commits exist");
        }
        if (git.getRepository().getRepositoryState() == RepositoryState.CHERRY_PICKING)
        {
          return RepositoryImplHelper.getMergeConflicts(git, currentBranchId, conflictingBranchId,
                                                        getCommit(conflictingBranchId).getParents().get(0),
                                                        conflictingFiles, this::diff);
        }
        return RepositoryImplHelper.getMergeConflicts(git, currentBranchId, conflictingBranchId,
                                                      new CommitImpl(RepositoryImplHelper.findForkPoint(git, currentBranchId, conflictingBranchId)),
                                                      conflictingFiles, this::diff);
      }
      return Collections.emptyList();
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  @NotNull
  public List<IMergeDiff> getStashConflicts(String pStashedCommitId) throws AditoGitException
  {
    Set<String> conflictingFiles = status.blockingFirst().map(IFileStatus::getConflicting).orElse(Collections.emptySet());
    try
    {
      return RepositoryImplHelper.getStashConflictMerge(git, conflictingFiles, pStashedCommitId, this::diff, this::diff);
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<IMergeDiff> merge(@NotNull IBranch pParentBranch, @NotNull IBranch pBranchToMerge) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("git merge %s %s", pParentBranch, pBranchToMerge));
    try
    {
      String parentID = pParentBranch.getId();
      String toMergeID = pBranchToMerge.getId();
      List<IMergeDiff> mergeConflicts = new ArrayList<>();
      if (!status.blockingFirst().map(pStatus -> pStatus.getConflicting().isEmpty()).orElse(true))
      {
        RevCommit forkCommit = RepositoryImplHelper.findForkPoint(git, parentID, toMergeID);
        return RepositoryImplHelper.getMergeConflicts(git, parentID, toMergeID, new CommitImpl(forkCommit),
                                                      status.blockingFirst().map(IFileStatus::getConflicting).orElse(Collections.emptySet()),
                                                      this::diff);
      }
      try
      {
        checkout(pParentBranch);
      }
      catch (Exception e)
      {
        throw new AditoGitException("Unable to checkout the parentBranch: " + parentID + " at the merge command", e);
      }
      ObjectId mergeBase;
      try
      {
        mergeBase = git.getRepository().resolve(toMergeID);
      }
      catch (IOException e)
      {
        throw new AditoGitException("Unable to merge the branch " + toMergeID + " and " + parentID, e);
      }
      try
      {
        MergeResult mergeResult = git.merge()
            .include(mergeBase)
            .setCommit(false)
            .setFastForward(MergeCommand.FastForwardMode.NO_FF).call();
        if (mergeResult.getConflicts() != null)
        {
          RevCommit forkCommit = RepositoryImplHelper.findForkPoint(git, parentID, toMergeID);
          logger.log(Level.INFO, () -> String.format("base commit for merge: %s", forkCommit));
          if (forkCommit != null)
            mergeConflicts = RepositoryImplHelper.getMergeConflicts(git, parentID, toMergeID, new CommitImpl(forkCommit),
                                                                    mergeResult.getConflicts().keySet(), this::diff);
        }
      }
      catch (GitAPIException e)
      {
        throw new AditoGitException("Unable to execute the merge command: " + parentID + "and " + toMergeID, e);
      }
      return mergeConflicts;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<IDiffInfo> getCommittedFiles(String pCommitId) throws AditoGitException
  {
    try
    {
      RevCommit thisCommit = RepositoryImplHelper.getRevCommit(git, pCommitId);
      List<IDiffInfo> diffInfos = new ArrayList<>();
      for (RevCommit parentCommit : thisCommit.getParents())
      {
        List<IFileChangeType> fileChangeTypes = RepositoryImplHelper.doDiff(git, thisCommit.getId(), parentCommit).stream()
            .map(pDiffEntry -> {
              EChangeType changeType = EnumMappings.toEChangeType(pDiffEntry.getChangeType());
              return new FileChangeTypeImpl(new File(pDiffEntry.getNewPath()), new File(pDiffEntry.getOldPath()), changeType);
            })
            .distinct()
            .collect(Collectors.toList());
        diffInfos.add(new DiffInfoImpl(new CommitImpl(thisCommit), new CommitImpl(parentCommit), fileChangeTypes));
      }
      return diffInfos;
    }
    catch (IOException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public ICommit getCommit(@Nullable String pIdentifier) throws AditoGitException
  {
    try
    {
      if (pIdentifier == null)
      {
        // only one RevCommit expected as result, so only take the first RevCommit
        return new CommitImpl(git.log().add(git.getRepository().resolve(Constants.HEAD)).setMaxCount(1).call().iterator().next());
      }
      return new CommitImpl(RepositoryImplHelper.getRevCommit(git, pIdentifier));
    }
    catch (IOException | GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public DAGFilterIterator<ICommit> getCommits(@Nullable ICommitFilter pCommitFilter) throws AditoGitException
  {
    return RepositoryImplHelper.getCommits(git, pCommitFilter == null ? new CommitFilterImpl() : pCommitFilter);
  }

  @NotNull
  @Override
  public List<ICommit> getUnPushedCommits() throws AditoGitException
  {
    List<ICommit> unPushedCommits = new ArrayList<>();
    try
    {
      LogCommand logCommand = git.log()
          .add(git.getRepository().resolve(git.getRepository().getFullBranch()));
      String remoteTrackingBranch = new BranchConfig(git.getRepository().getConfig(), git.getRepository().getBranch()).getRemoteTrackingBranch();
      ObjectId remoteTrackingId = null;
      if (remoteTrackingBranch != null)
      {
        remoteTrackingId = git.getRepository().resolve(remoteTrackingBranch);
      }
      else
      {
        remoteTrackingBranch = new BranchConfig(git.getRepository().getConfig(), "master").getRemoteTrackingBranch();
        if (remoteTrackingBranch != null)
          remoteTrackingId = git.getRepository().resolve(remoteTrackingBranch);
      }
      if (remoteTrackingId != null)
        logCommand.not(remoteTrackingId);
      logger.log(Level.INFO, "remote tracking branch for unpushed commits: {0}", remoteTrackingBranch);
      Iterable<RevCommit> unPushedCommitsIter = logCommand.call();
      unPushedCommitsIter.forEach(pUnPushedCommit -> unPushedCommits.add(new CommitImpl(pUnPushedCommit)));
    }
    catch (GitAPIException | IOException pE)
    {
      throw new AditoGitException(pE);
    }
    return unPushedCommits;
  }

  @Override
  public String getDirectory()
  {
    return String.valueOf(git.getRepository().getDirectory());
  }

  @NotNull
  @Override
  public Observable<String> displayName()
  {
    File tld = getTopLevelDirectory();
    return Observable.just(tld != null ? tld.getName() : getDirectory());
  }

  @Nullable
  @Override
  public File getTopLevelDirectory()
  {
    return git.getRepository().getDirectory().getParent() != null ? new File(git.getRepository().getDirectory().getParent()) : null;
  }

  @Override
  public void refreshStatus()
  {
    fileSystemObserver.fireChange();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IBranch getBranch(@NotNull String pBranchString)
  {
    try
    {
      if ((!Pattern.matches("/refs/heads/.*", pBranchString) || Pattern.matches("refs/remotes/.*", pBranchString))
          && (pBranchString.startsWith("/") || !pBranchString.contains("/")))
      {
        pBranchString = Paths.get("refs", "heads", pBranchString).toString().replace("\\", "/");
      }
      return new BranchImpl(git.getRepository().getRefDatabase().getRef(pBranchString));
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public Observable<Optional<List<IBranch>>> getBranches()
  {
    return branchList;
  }

  @Override
  public void createTag(@NotNull String pName, @Nullable String pCommitId)
  {
    try (RevWalk walk = new RevWalk(git.getRepository()))
    {
      git.tag().setName(pName).setObjectId(pCommitId == null ? null : walk.parseAny(ObjectId.fromString(pCommitId))).call();
    }
    catch (IOException | GitAPIException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<String> deleteTag(ITag pTag)
  {
    logger.log(Level.INFO, () -> String.format("git tag --delete %s", pTag));
    List<String> deletedTags;
    try
    {
      deletedTags = git.tagDelete().setTags(pTag.getName()).call();
    }
    catch (GitAPIException pE)
    {
      throw new RuntimeException(pE);
    }
    return deletedTags;
  }

  @NotNull
  @Override
  public Observable<List<ITag>> getTags()
  {
    return tagList;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<ICommit> getStashedCommits() throws AditoGitException
  {
    return RepositoryImplHelper.getStashedCommits(git);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable String peekStash() throws AditoGitException
  {
    try
    {
      Collection<RevCommit> stashedCommits = git.stashList().call();
      if (!stashedCommits.isEmpty())
      {
        return stashedCommits.iterator().next().getName();
      }
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable String stashChanges(String pMessage, boolean pIncludeUnTracked) throws AditoGitException
  {
    logger.log(Level.INFO, () -> "trying to stash local changes to the working tree");
    try
    {
      StashCreateCommand stashCreate = git.stashCreate();
      if (pMessage != null)
      {
        stashCreate.setWorkingDirectoryMessage(pMessage);
        stashCreate.setIndexMessage(pMessage);
      }
      stashCreate.setIncludeUntracked(pIncludeUnTracked);
      RevCommit stashCommit = stashCreate.call();
      if (stashCommit != null)
      {
        logger.log(Level.INFO, () -> String.format("successfully stashed local changes, stash commit id is %s", ObjectId.toString(stashCommit.getId())));
        return stashCommit.getName();
      }
      logger.log(Level.INFO, () -> "JGit returned null as stash id, probably because there are no changes to the local working tree");
      return null;
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<IMergeDiff> unStashIfAvailable() throws AditoGitException
  {
    String topMostStashedCommitId = peekStash();
    if (topMostStashedCommitId != null)
    {
      return unStashChanges(topMostStashedCommitId);
    }
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<IMergeDiff> unStashChanges(@NotNull String pStashCommitId) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("unstashing stash with commit id %s", pStashCommitId));
    int stashIndexForId = RepositoryImplHelper.getStashIndexForId(git, pStashCommitId);
    if (stashIndexForId < 0)
      throw new AditoGitException("Could not find a stashed commit for id " + pStashCommitId);
    try
    {
      git.stashApply().setStashRef(pStashCommitId).call();
    }
    catch (StashApplyFailureException pStashApplyFailureEx)
    {
      if (pStashApplyFailureEx.getMessage().contains("Applying stashed changes resulted in a conflict"))
      {
        try
        {
          List<IMergeDiff> stashConflicts = RepositoryImplHelper.getStashConflictMerge(git, RepositoryImplHelper.status(git).getConflicting(),
                                                                                       pStashCommitId, this::diff, this::diff);
          if (stashConflicts.isEmpty())
            throw new AditoGitException("Could not determine conflicting files, commit or undo your changes before trying the unstash again", pStashApplyFailureEx);
          else return stashConflicts;
        }
        catch (IOException pE1)
        {
          throw new AditoGitException(pE1);
        }
      }
      else throw new AditoGitException("Failed to apply stash, there may be existing changes. Undo/commit these changes and try again " +
                                           "or check the attached stackTrace for more information", pStashApplyFailureEx);
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropStashedCommit(@Nullable String pStashCommitId) throws AditoGitException
  {
    logger.log(Level.INFO, () -> String.format("deleting stash commit with id %s", pStashCommitId));
    try
    {
      StashDropCommand stashDropCommand = git.stashDrop();
      if (pStashCommitId != null)
        stashDropCommand.setStashRef(RepositoryImplHelper.getStashIndexForId(git, pStashCommitId));
      stashDropCommand.call();
    }
    catch (GitAPIException pE)
    {
      throw new AditoGitException(pE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IConfig getConfig()
  {
    return dataFactory.createConfig(git);
  }

  @Override
  public @NotNull Set<String> getRemoteNames()
  {
    return git.getRepository().getRemoteNames();
  }


  /**
   * Bridge from the FileSystemChangeListener to Observables
   */
  private class _FileSystemChangeObservable extends AbstractListenerObservable<IFileSystemChangeListener, IFileSystemObserver, Optional<IFileStatus>>
  {

    _FileSystemChangeObservable(@NotNull IFileSystemObserver pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected IFileSystemChangeListener registerListener(@NotNull IFileSystemObserver pIFileSystemObserver,
                                                         @NotNull IFireable<Optional<IFileStatus>> pIFireable)
    {
      Executor service = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new VoidingPseudoBlockingArrayQueue<>(1));
      IFileSystemChangeListener listener = () -> service.execute(() -> pIFireable.fireValueChanged(Optional.of(RepositoryImplHelper.status(git))));
      pIFileSystemObserver.addListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull IFileSystemObserver pListenableValue, @NotNull IFileSystemChangeListener pLISTENER)
    {
      pListenableValue.removeListener(pLISTENER);
    }
  }
}