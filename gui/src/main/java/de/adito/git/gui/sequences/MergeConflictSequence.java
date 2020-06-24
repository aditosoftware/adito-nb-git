package de.adito.git.gui.sequences;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EAutoResolveOptions;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.EButtons;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.panels.CheckboxPanel;
import de.adito.git.gui.dialogs.panels.NotificationPanel;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.impl.Util;
import de.adito.git.impl.data.diff.EConflictType;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 09.06.2020
 */
public class MergeConflictSequence
{

  private static final Logger logger = Logger.getLogger(MergeConflictSequence.class.getName());
  private final IDialogProvider dialogProvider;
  private final IPrefStore prefStore;
  private final IAsyncProgressFacade asyncProgressFacade;

  @Inject
  public MergeConflictSequence(IDialogProvider pDialogProvider, IPrefStore pPrefStore, IAsyncProgressFacade pAsyncProgressFacade)
  {
    dialogProvider = pDialogProvider;
    prefStore = pPrefStore;
    asyncProgressFacade = pAsyncProgressFacade;
  }

  /**
   * Determines if an auto-resolve should be performed by checking a flag and potentially asking the user, and then shows the merge dialog itself
   *
   * @param pRepo                Observable with the current repository
   * @param pMergeConflicts      list of mergeConflicts
   * @param pShowOnlyConflicting true if the list of mergeConflicts should be filtered by the status of the files
   * @param pDialogTitle         Optional title of the dialog, may be left out. Only first parameter is used
   * @return Result of the mergeConflictDialog
   */
  @NotNull
  public IMergeConflictDialogResult<?, ?> performMergeConflictSequence(@NotNull Observable<Optional<IRepository>> pRepo, @NotNull List<IMergeData> pMergeConflicts,
                                                                       boolean pShowOnlyConflicting, String... pDialogTitle)
  {
    boolean showAutoResolveButton = true;
    EAutoResolveOptions autoResolveSettingsFlag = EAutoResolveOptions.getFromStringValue(prefStore.get(Constants.AUTO_RESOLVE_SETTINGS_KEY));
    IUserPromptDialogResult<?, ?> promptDialogResult = null;
    // only show the dialog if the auto resolve setting is not set -> user can also choose to never use auto-resolve
    if (EAutoResolveOptions.ASK.equals(autoResolveSettingsFlag))
    {
      CheckboxPanel checkboxPanel = dialogProvider.getPanelFactory().createCheckboxPanel(Util.getResource(MergeConflictSequence.class, "autoResolveText"),
                                                                                         Util.getResource(MergeConflictSequence.class, "autoResolveCheckboxText"));
      NotificationPanel notificationPanel = dialogProvider.getPanelFactory()
          .createNotificationPanel(Util.getResource(MergeConflictSequence.class, "autoResolveAdditionalInfo"));
      promptDialogResult = dialogProvider.showDialog(dialogProvider.getPanelFactory().getExpandablePanel(checkboxPanel, notificationPanel),
                                                     Util.getResource(MergeConflictSequence.class, "autoResolveDialogTitle"),
                                                     List.of(EButtons.AUTO_RESOLVE, EButtons.SKIP),
                                                     List.of(EButtons.AUTO_RESOLVE));
      if ((Boolean) promptDialogResult.getInformation())
      {
        prefStore.put(Constants.AUTO_RESOLVE_SETTINGS_KEY, EAutoResolveOptions.getFromBoolean(String.valueOf(promptDialogResult.isOkay())).toString());
      }
    }
    Optional<IRepository> repositoryOptional = pRepo.blockingFirst(Optional.empty());
    if (repositoryOptional.isPresent() && (EAutoResolveOptions.ALWAYS.equals(autoResolveSettingsFlag) || (promptDialogResult != null && promptDialogResult.isOkay())))
    {
      showAutoResolveButton = false;
      performAutoResolve(pMergeConflicts, repositoryOptional.get(), asyncProgressFacade);
    }
    return dialogProvider.showMergeConflictDialog(pRepo, pMergeConflicts, pShowOnlyConflicting, showAutoResolveButton, pDialogTitle);
  }

  /**
   * Goes through the list of conflicting files and tries to perform an auto-resolve. This is done by checking if the file has no change delta that is marked as
   * conflicting, and if that is the case, accepting all changes and marking the file resolved. Files with conflicting change deltas are not touched
   *
   * @param pMergeConflicts List of merge conflicts to try and auto-resolve
   * @param pRepository     Repository, used to perform an add one the conflicting files to mark them as resolved
   */
  public static void performAutoResolve(@NotNull List<IMergeData> pMergeConflicts, @NotNull IRepository pRepository, @NotNull IAsyncProgressFacade pProgressFacade)
  {
    pProgressFacade.executeAndBlockWithProgress("Auto-Resolving", pProgressHandle -> {
      pProgressHandle.switchToDeterminate(pMergeConflicts.size());
      for (int index = pMergeConflicts.size() - 1; index > 0; index--)
      {
        IMergeData mergeData = pMergeConflicts.get(index);
        pProgressHandle.setDescription("Trying to resolve  " + mergeData.getFilePath());
        mergeData.markConflicting();
        if (mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().stream().noneMatch(pChangeDelta -> pChangeDelta.getConflictType() == EConflictType.CONFLICTING)
            && mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().stream().noneMatch(pChangeDelta -> pChangeDelta.getConflictType() == EConflictType.CONFLICTING))
        {
          acceptMergeSide(mergeData, EConflictSide.YOURS);
          acceptMergeSide(mergeData, EConflictSide.THEIRS);
          acceptManualVersion(mergeData, pRepository);
          pMergeConflicts.remove(mergeData);
        }
        pProgressHandle.progress(pMergeConflicts.size() - index);
      }
    });
  }

  public static void acceptMergeSide(IMergeData mergeData, EConflictSide pConflictSide)
  {
    for (IChangeDelta changeDelta : mergeData.getDiff(pConflictSide).getChangeDeltas())
    {
      if (changeDelta.getChangeStatus() == EChangeStatus.PENDING && changeDelta.getConflictType() != EConflictType.CONFLICTING)
      {
        mergeData.acceptDelta(changeDelta, pConflictSide);
      }
    }
  }

  /**
   * @param pSelectedMergeDiff list of selected IMergeDatas
   * @param pConflictSide      Side of the IMergeDatas that should be accepted
   * @param pRepository        Repository to call add to mark files as resolved
   */
  public static void acceptDefaultVersion(@NotNull List<IMergeData> pSelectedMergeDiff, @NotNull EConflictSide pConflictSide, @NotNull IRepository pRepository)
  {

    try
    {
      for (IMergeData selectedMergeDiff : pSelectedMergeDiff)
      {
        String path = selectedMergeDiff.getDiff(pConflictSide).getFileHeader().getAbsoluteFilePath();
        if (path != null)
        {
          File selectedFile = new File(path);
          if (selectedMergeDiff.getDiff(pConflictSide).getFileHeader().getChangeType() == EChangeType.DELETE)
          {
            pRepository.remove(List.of(selectedFile));
          }
          else
          {
            _saveVersion(pConflictSide, selectedMergeDiff, selectedFile);
          }
        }
      }
      pRepository.add(pSelectedMergeDiff.stream()
                          .map(pMergeDiff -> pMergeDiff.getDiff(pConflictSide).getFileHeader())
                          .filter(pFileDiffHeader -> pFileDiffHeader.getChangeType() != EChangeType.DELETE)
                          .map(IFileDiffHeader::getAbsoluteFilePath)
                          .filter(Objects::nonNull)
                          .map(File::new)
                          .collect(Collectors.toList()));
    }
    catch (Exception pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * @param pConflictSide     side of the conflict that was accepted by the user
   * @param selectedMergeDiff the mergeDiff that should be resolved
   * @param pSelectedFile     File that this mergeDiff is for
   */
  private static void _saveVersion(EConflictSide pConflictSide, IMergeData selectedMergeDiff, File pSelectedFile) throws IOException
  {
    String fileContents = selectedMergeDiff.getDiff(pConflictSide).getText(EChangeSide.NEW);
    logger.log(Level.INFO, () -> String.format(Util.getResource(MergeConflictSequence.class, "mergeConflictSequenceLogText"), pSelectedFile.getAbsolutePath(),
                                               selectedMergeDiff.getDiff(pConflictSide).getEncoding(EChangeSide.NEW)));
    _writeToFile(fileContents, selectedMergeDiff.getDiff(pConflictSide).getEncoding(EChangeSide.NEW), pSelectedFile);
  }

  public static void acceptManualVersion(IMergeData pMergeDiff, IRepository pRepository)
  {
    String path = pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getAbsoluteFilePath();
    if (path != null)
    {
      File selectedFile = new File(pRepository.getTopLevelDirectory(), pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getFilePath());
      String fileContents = pMergeDiff.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD);
      logger.log(Level.INFO, () -> String.format(Util.getResource(MergeConflictSequence.class, "mergeConflictSequenceLogText"), path,
                                                 pMergeDiff.getDiff(EConflictSide.YOURS).getEncoding(EChangeSide.NEW)));
      try
      {
        _writeToFile(_adjustLineEndings(fileContents, pMergeDiff), pMergeDiff.getDiff(EConflictSide.YOURS).getEncoding(EChangeSide.NEW), selectedFile);
        pRepository.add(Collections.singletonList(selectedFile));
      }
      catch (Exception pE)
      {
        throw new RuntimeException(pE);
      }
    }
  }

  /**
   * @param pFileContents Contents that should be written to the file
   * @param pCharset      Charset used to write pFileContents to disk (gets transferred from String to byte array)
   * @param pSelectedFile file which should be overridden with pFileContents
   */
  private static void _writeToFile(String pFileContents, Charset pCharset, File pSelectedFile) throws IOException
  {
    if (!pSelectedFile.exists())
      pSelectedFile.getParentFile().mkdirs();
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pSelectedFile, false), pCharset))
    {
      writer.write(pFileContents);
    }
  }

  /**
   * replaces all lineEndings with those determined by the MergeData
   *
   * @param pFileContent content for which to change the newlines
   * @param pMergeData   IMergeData containing the FileContentInfos used to determine the used lineEndings
   * @return String with changed newlines
   */
  public static String _adjustLineEndings(String pFileContent, IMergeData pMergeData)
  {
    ELineEnding lineEnding = _getLineEnding(pMergeData);
    if (lineEnding == ELineEnding.UNIX)
    {
      return pFileContent.replaceAll("\r\n", ELineEnding.UNIX.getLineEnding()).replaceAll("\r", ELineEnding.UNIX.getLineEnding());
    }
    else if (lineEnding == ELineEnding.WINDOWS)
    {
      return pFileContent.replaceAll("\r(?!\n)", ELineEnding.WINDOWS.getLineEnding()).replaceAll("(?<!\r)\n", ELineEnding.WINDOWS.getLineEnding());
    }
    else return pFileContent.replaceAll("\r\n", ELineEnding.MAC.getLineEnding()).replaceAll("\n", ELineEnding.MAC.getLineEnding());
  }

  /**
   * Determines the lineEnding to use by checking the two NEW versions of the ConflictSides, if those have the same lineEnding then that lineEnding is used.
   * Otherwise the lineEnding used by the sytem is returned
   *
   * @param pMergeData IMergeData containing the FileContentInfos used to determine the used lineEndings
   * @return LineEnding
   */
  private static ELineEnding _getLineEnding(IMergeData pMergeData)
  {
    if (pMergeData.getDiff(EConflictSide.THEIRS).getFileContentInfo(EChangeSide.NEW).getLineEnding().get()
        == pMergeData.getDiff(EConflictSide.YOURS).getFileContentInfo(EChangeSide.NEW).getLineEnding().get())
    {
      return pMergeData.getDiff(EConflictSide.THEIRS).getFileContentInfo(EChangeSide.NEW).getLineEnding().get();
    }
    else return ELineEnding.getLineEnding(System.lineSeparator());
  }

}
