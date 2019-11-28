package de.adito.git.gui.dialogs;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.results.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static de.adito.git.gui.dialogs.IDialogDisplayer.EButtons;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class DialogProviderImpl implements IDialogProvider
{
  private final IDialogDisplayer dialogDisplayer;
  private final IDialogFactory dialogFactory;

  @Inject
  public DialogProviderImpl(IDialogDisplayer pDialogDisplayer, IDialogFactory pDialogFactory)
  {
    dialogDisplayer = pDialogDisplayer;
    dialogFactory = pDialogFactory;
  }

  @Override
  public @NotNull IMergeConflictDialogResult showMergeConflictDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                     @NotNull List<IMergeDiff> pMergeConflictDiffs, boolean pOnlyConflicting, String... pDialogTitle)
  {
    DialogResult<MergeConflictDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictDialog(pValidConsumer, pRepository, pMergeConflictDiffs, pOnlyConflicting),
                                          pDialogTitle.length == 1 ? pDialogTitle[0] : "Merge Conflicts", List.of(EButtons.OK, EButtons.ABORT).toArray(new EButtons[0]));
      return new MergeConflictDialogResultImpl<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class MergeConflictDialogResultImpl<S, T> extends DialogResult<S, T> implements IMergeConflictDialogResult<S, T>
  {

    private MergeConflictDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isFinishMerge()
    {
      return selectedButton == EButtons.OK;
    }
  }

  @Override
  public @NotNull IMergeConflictResolutionDialogResult showMergeConflictResolutionDialog(@NotNull IMergeDiff pMergeDiff)
  {
    DialogResult<MergeConflictResolutionDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictResolutionDialog(pMergeDiff),
                                          "Conflict resolution for file "
                                              + pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileHeader().getFilePath(),
                                          List.of(EButtons.ACCEPT_CHANGES, EButtons.CANCEL).toArray(new EButtons[0]));
      return new MergeConflictResulutionDialogResultImpl<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class MergeConflictResulutionDialogResultImpl<S, T> extends DialogResult<S, T> implements IMergeConflictResolutionDialogResult<S, T>
  {

    private MergeConflictResulutionDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isAcceptChanges()
    {
      return selectedButton == EButtons.ACCEPT_CHANGES;
    }
  }

  @Override
  public @NotNull IDiffDialogResult showDiffDialog(@NotNull File pProjectDirectory, @NotNull List<IFileDiff> pFileDiffs, @Nullable String pSelectedFile,
                                                   boolean pAcceptChange, boolean pShowFileTree)
  {
    DialogResult<DiffDialog, ?> result = null;
    try
    {
      String title = "Diff for file ";
      if (pSelectedFile != null)
        title += pSelectedFile;
      else if (!pFileDiffs.isEmpty())
        title += pFileDiffs.get(0).getFileHeader().getFilePath();
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createDiffDialog(pProjectDirectory, pFileDiffs, pSelectedFile, pAcceptChange, pShowFileTree),
                                          title,
                                          pAcceptChange ? List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0]) :
                                              List.of(EButtons.CLOSE).toArray(new EButtons[0]));
      return new DiffDialogResultImpl<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class DiffDialogResultImpl<S, T> extends DialogResult<S, T> implements IDiffDialogResult<S, T>
  {

    private DiffDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isPressedOkay()
    {
      return selectedButton == EButtons.OK;
    }
  }

  @Override
  public @NotNull ICommitDialogResult<CommitDialog, CommitDialogResult> showCommitDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                                         @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                                                                         @NotNull String pMessageTemplate)
  {
    DialogResult<CommitDialog, CommitDialogResult> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pIsValidDescriptor -> dialogFactory.createCommitDialog(pIsValidDescriptor, pRepository, pFilesToCommit,
                                                                                                 pMessageTemplate),
                                          "Commit", List.of(EButtons.COMMIT, EButtons.CANCEL).toArray(new EButtons[0]));
      return new CommitDialogResultImpl<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class CommitDialogResultImpl<S, T> extends DialogResult<S, T> implements ICommitDialogResult<S, T>
  {

    private CommitDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean doCommit()
    {
      return selectedButton == EButtons.COMMIT;
    }
  }

  @Override
  public @NotNull INewBranchDialogResult<NewBranchDialog, Boolean> showNewBranchDialog(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return new NewBranchDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createNewBranchDialog(pValidConsumer, pRepository),
                                                                      "New Branch", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  private static class NewBranchDialogResultImpl<S, T> extends DialogResult<S, T> implements INewBranchDialogResult<S, T>
  {

    private NewBranchDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isCreateBranch()
    {
      return selectedButton == EButtons.OK;
    }
  }

  @Override
  public @NotNull IResetDialogResult<ResetDialog, EResetType> showResetDialog()
  {
    return new ResetDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createResetDialog(),
                                                                  "Reset", List.of(EButtons.OK, EButtons.ABORT).toArray(new EButtons[0])));
  }

  private static class ResetDialogResultImpl<S, T> extends DialogResult<S, T> implements IResetDialogResult<S, T>
  {

    private ResetDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isPerformReset()
    {
      return selectedButton == EButtons.OK;
    }
  }

  @Override
  public @NotNull IPushDialogResult<PushDialog, Boolean> showPushDialog(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull List<ICommit> pCommitList)
  {
    DialogResult<PushDialog, Boolean> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createPushDialog(pRepository, pCommitList), "List of commits to be pushed",
                                          List.of(EButtons.PUSH, EButtons.CANCEL).toArray(new EButtons[0]));
      return new PushDialogResultImpl<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class PushDialogResultImpl<S, T> extends DialogResult<S, T> implements IPushDialogResult<S, T>
  {

    private PushDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isPush()
    {
      return selectedButton == EButtons.PUSH;
    }
  }

  @Override
  public @NotNull IStashedCommitSelectionDialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(
      @NotNull Observable<Optional<IRepository>> pRepo, @NotNull List<ICommit> pStashedCommits)
  {
    DialogResult<StashedCommitSelectionDialog, String> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createStashedCommitSelectionDialog(pValidConsumer, pRepo, pStashedCommits),
                                          "Choose stashed commit", List.of(EButtons.UNSTASH, EButtons.CLOSE).toArray(new EButtons[0]));
      return new StashedCommitSelectionDialogResult<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class StashedCommitSelectionDialogResult<S, T> extends DialogResult<S, T> implements IStashedCommitSelectionDialogResult<S, T>
  {

    private StashedCommitSelectionDialogResult(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean doUnStash()
    {
      return selectedButton == EButtons.UNSTASH;
    }
  }

  @Override
  public @NotNull IUserPromptDialogResult<PasswordPromptDialog, char[]> showPasswordPromptDialog(@NotNull String pMessage)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createPasswordPromptDialog(),
                                                                       pMessage, List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  private static class UserPromptDialogResultImpl<S, T> extends DialogResult<S, T> implements IUserPromptDialogResult<S, T>
  {

    private UserPromptDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isOkay()
    {
      return selectedButton == EButtons.OK;
    }
  }

  @Override
  public @NotNull IUserPromptDialogResult<UserPromptDialog, Object> showUserPromptDialog(@NotNull String pMessage, @Nullable String pDefault)
  {
    final String defaultValue = pDefault;
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           dialogFactory.createUserPromptDialog(defaultValue),
                                                                       pMessage, List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public @NotNull IUserPromptDialogResult showYesNoDialog(@NotNull String pMessage)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           dialogFactory.createYesNoDialog(pMessage),
                                                                       pMessage, List.of(EButtons.YES, EButtons.NO).toArray(new EButtons[0])))
    {
      @Override
      public boolean isOkay()
      {
        return selectedButton == EButtons.YES;
      }
    };
  }

  @NotNull
  @Override
  public IRevertDialogResult showRevertDialog(@NotNull List<IFileChangeType> pFilesToRevert, @NotNull File pProjectDirectory)
  {

    DialogResult<RevertFilesDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createRevertDialog(pFilesToRevert, pProjectDirectory), "Confirm revert of files",
                                          List.of(EButtons.CONFIRM, EButtons.CANCEL).toArray(new EButtons[0]));
      return new RevertDialogResult(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class RevertDialogResult<S, T> extends DialogResult<S, T> implements IRevertDialogResult<S, T>
  {
    private RevertDialogResult(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    public boolean isRevertAccepted()
    {
      return selectedButton == IDialogDisplayer.EButtons.CONFIRM;
    }
  }

  @Override
  public @NotNull IDeleteBranchDialogResult<DeleteBranchDialog, Boolean> showDeleteBranchDialog(@NotNull String pBranchName)
  {
    DialogResult<DeleteBranchDialog, Boolean> result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createDeleteBranchDialog(),
                                                                                  "Delete Branch", List.of(EButtons.DELETE, EButtons.CANCEL).toArray(new EButtons[0]));
    return new DeleteBranchDialogResultImpl<>(result);
  }

  private static class DeleteBranchDialogResultImpl<S, T> extends DialogResult<S, T> implements IDeleteBranchDialogResult<S, T>
  {

    private DeleteBranchDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isDelete()
    {
      return selectedButton == IDialogDisplayer.EButtons.DELETE;
    }
  }

  @Override
  public @NotNull IFileSelectionDialogResult<FileSelectionDialog, Object> showFileSelectionDialog(@NotNull String pMessage, @NotNull String pLabel,
                                                                                                  @NotNull FileChooserProvider.FileSelectionMode pFileSelectionMode,
                                                                                                  @Nullable FileFilter pFileFilter)
  {
    DialogResult<FileSelectionDialog, Object> result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory
                                                                                      .createFileSelectionDialog(pLabel, pFileSelectionMode, pFileFilter),
                                                                                  pMessage, List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0]));
    return new FileSelectionDialogResultImpl<>(result);
  }

  private static class FileSelectionDialogResultImpl<S, T> extends DialogResult<S, T> implements IFileSelectionDialogResult<S, T>
  {

    private FileSelectionDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean acceptFiles()
    {
      return selectedButton == IDialogDisplayer.EButtons.OK;
    }
  }

  @Override
  public @NotNull IGitConfigDialogResult<GitConfigDialog, Multimap<String, Object>> showGitConfigDialog(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    String repoName = pRepository.blockingFirst().map(pRepo -> pRepo.getTopLevelDirectory().getName()).orElse("unknown repository");
    DialogResult<GitConfigDialog, Multimap<String, Object>> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createGitConfigDialog(pRepository),
                                          "Setting for project: " + repoName, List.of(EButtons.SAVE, EButtons.CLOSE).toArray(new EButtons[0]));
      return new GitConfigDialogResultImpl<>(result);
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  private static class GitConfigDialogResultImpl<S, T> extends DialogResult<S, T> implements IGitConfigDialogResult<S, T>
  {

    private GitConfigDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }


    @Override
    public boolean doSave()
    {
      return selectedButton == EButtons.SAVE;
    }
  }

  @Override
  public @NotNull IStashChangesDialogResult<StashChangesDialog, StashChangesResult> showStashChangesDialog()
  {
    return new StashChangesDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createStashChangesDialog(),
                                                                         "Stash Changes - stash message",
                                                                         List.of(EButtons.STASH_CHANGES, EButtons.ABORT).toArray(new EButtons[0])));
  }

  private static class StashChangesDialogResultImpl<S, T> extends DialogResult<S, T> implements IStashChangesDialogResult<S, T>
  {

    private StashChangesDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean doStash()
    {
      return selectedButton == EButtons.STASH_CHANGES;
    }
  }

  @Override
  public @NotNull IUserPromptDialogResult<SshInfoPrompt, char[]> showSshInfoPromptDialog(@NotNull String pMessage, @Nullable String pSshKeyLocation,
                                                                                         @Nullable char[] pPassphrase, @NotNull IKeyStore pKeyStore)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           dialogFactory.createSshInfoPromptDialog(pMessage, pSshKeyLocation, pPassphrase, pKeyStore),
                                                                       "SSH key information", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public @NotNull IUserPromptDialogResult<CheckboxPrompt, Boolean> showCheckboxPrompt(@NotNull String pMessage, @NotNull String pCheckboxText)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           dialogFactory.createCheckboxPrompt(pMessage, pCheckboxText),
                                                                       "", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @NotNull
  @Override
  public DialogResult<TagOverviewDialog, Object> showTagOverviewDialog(@NotNull Consumer<ICommit> pSelectedCommitCallback,
                                                                       @NotNull Observable<Optional<IRepository>> pRepository)
  {
    DialogResult<TagOverviewDialog, Object> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createTagOverviewDialog(pSelectedCommitCallback, pRepository), "Tag Overview",
                                          List.of(EButtons.CLOSE).toArray(new EButtons[0]));
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @Override
  public IUserPromptDialogResult<ComboBoxDialog<Object>, Object> showComboBoxDialog(@NotNull String pMessage, @NotNull List<Object> pOptions)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createComboBoxDialog(pMessage, pOptions),
                                                                       "", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public IStashChangesQuestionDialogResult<StashChangesQuestionDialog, Object> showStashChangesQuestionDialog(@NotNull List<IFileChangeType> pFilesToRevert,
                                                                                                              @NotNull File pProjectDir)
  {
    return new StashChangesQuestionDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                                     dialogFactory.createStashChangesQuestionDialog(pFilesToRevert, pProjectDir),
                                                                                 "Local changes detected",
                                                                                 List.of(EButtons.STASH_CHANGES, EButtons.DISCARD_CHANGES, EButtons.ABORT)
                                                                                     .toArray(new EButtons[0])));
  }

  private static class StashChangesQuestionDialogResultImpl<S, T> extends DialogResult<S, T> implements IStashChangesQuestionDialogResult<S, T>
  {

    private StashChangesQuestionDialogResultImpl(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isDiscardChanges()
    {
      return selectedButton == EButtons.DISCARD_CHANGES;
    }

    @Override
    public boolean isStashChanges()
    {
      return selectedButton == EButtons.STASH_CHANGES;
    }

    @Override
    public boolean isAbosrt()
    {
      return selectedButton == EButtons.ABORT;
    }
  }
}
