package de.adito.git.gui.dialogs;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.panels.ComboBoxPanel;
import de.adito.git.gui.dialogs.panels.IPanelFactory;
import de.adito.git.gui.dialogs.panels.UserPromptPanel;
import de.adito.git.gui.dialogs.results.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author m.kaspera 26.10.2018
 */
@Singleton
class DialogProviderImpl implements IDialogProvider
{
  private final IDialogDisplayer dialogDisplayer;
  private final IDialogFactory dialogFactory;
  private final IPanelFactory panelFactory;

  @Inject
  public DialogProviderImpl(IDialogDisplayer pDialogDisplayer, IDialogFactory pDialogFactory, IPanelFactory pPanelFactory)
  {
    dialogDisplayer = pDialogDisplayer;
    dialogFactory = pDialogFactory;
    panelFactory = pPanelFactory;
  }

  @Override
  public @NotNull IMergeConflictDialogResult<MergeConflictDialog, ?> showMergeConflictDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                                             @NotNull List<IMergeData> pMergeConflictDiffs, boolean pOnlyConflicting,
                                                                                             boolean pShowAutoResolve, String... pDialogTitle)
  {
    DialogResult<MergeConflictDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictDialog(pValidConsumer, pRepository, pMergeConflictDiffs, pOnlyConflicting,
                                                                                                    pShowAutoResolve),
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

    @Override
    public boolean isAbortMerge()
    {
      return selectedButton == EButtons.ABORT;
    }
  }

  @Override
  public @NotNull IMergeConflictResolutionDialogResult<MergeConflictResolutionDialog, ?> showMergeConflictResolutionDialog(@NotNull IMergeData pMergeDiff)
  {
    DialogResult<MergeConflictResolutionDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictResolutionDialog(pMergeDiff),
                                          "Conflict resolution for file "
                                              + pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getFilePath(),
                                          List.of(EButtons.ACCEPT_CHANGES, EButtons.ACCEPT_YOURS, EButtons.ACCEPT_THEIRS, EButtons.CANCEL).toArray(new EButtons[0]));
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
  public @NotNull IUserPromptDialogResult<UserPromptPanel, Object> showUserPromptDialog(@NotNull String pMessage, @Nullable String pDefault)
  {
    final String defaultValue = pDefault;
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createUserPromptPanel(defaultValue),
                                                                       pMessage, List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public @NotNull IUserPromptDialogResult showYesNoDialog(@NotNull String pMessage)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createNotificationPanel(pMessage),
                                                                       pMessage, List.of(EButtons.YES, EButtons.NO).toArray(new EButtons[0])))
    {
      @Override
      public boolean isOkay()
      {
        return selectedButton == EButtons.YES;
      }
    };
  }

  @Override
  public IUserPromptDialogResult<?, ?> showMessageDialog(@NotNull String pMessage, @NotNull List<EButtons> pShownButtons, @NotNull List<EButtons> pOkayButtons)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createNotificationPanel(pMessage),
                                                                       "Git Plugin", pShownButtons.toArray(new EButtons[0])))
    {
      @Override
      public boolean isOkay()
      {
        return pOkayButtons.contains(selectedButton);
      }
    };
  }

  @NotNull
  @Override
  public IChangeTrackedBranchDialogResult showChangeTrackedBranchDialog(@NotNull String pMessage)
  {
    return new ChangeTrackedBranchDialogResult<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                                panelFactory.createNotificationPanel(pMessage),
                                                                            "", List.of(EButtons.CREATE_NEW_BRANCH, EButtons.KEEP_TRACKING, EButtons.ABORT)
                                                                                .toArray(new EButtons[0])));
  }

  private static class ChangeTrackedBranchDialogResult<S, T> extends DialogResult<S, T> implements IChangeTrackedBranchDialogResult<S, T>
  {

    private ChangeTrackedBranchDialogResult(DialogResult<S, T> pDialogResult)
    {
      super(pDialogResult.getSource(), pDialogResult.getSelectedButton(), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isCancel()
    {
      return selectedButton == EButtons.ABORT || selectedButton == EButtons.ESCAPE;
    }

    @Override
    public boolean isChangeBranch()
    {
      return selectedButton == EButtons.CREATE_NEW_BRANCH;
    }

    @Override
    public boolean isKeepTrackedBranch()
    {
      return selectedButton == EButtons.KEEP_TRACKING;
    }
  }

  @NotNull
  @Override
  public IRevertDialogResult<RevertFilesDialog, ?> showRevertDialog(@NotNull Observable<Optional<IRepository>> pRepositoryObs,
                                                                    @NotNull List<IFileChangeType> pFilesToRevert, @NotNull File pProjectDirectory)
  {

    DialogResult<RevertFilesDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createRevertDialog(pRepositoryObs, pFilesToRevert, pProjectDirectory),
                                          "Confirm revert of files", List.of(EButtons.CONFIRM, EButtons.CANCEL).toArray(new EButtons[0]));
      return new RevertDialogResult<>(result);
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
      return selectedButton == EButtons.CONFIRM;
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
      return selectedButton == EButtons.OK;
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
                                                                           dialogFactory.createSshInfoPromptDialog(pMessage, pSshKeyLocation, pValidConsumer,
                                                                                                                   pPassphrase, pKeyStore),
                                                                       "SSH key information", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public @NotNull IUserPromptDialogResult<?, Boolean> showCheckboxPrompt(@NotNull String pMessage, @NotNull String pCheckboxText)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createCheckboxPanel(pMessage, pCheckboxText),
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
  public IUserPromptDialogResult<ComboBoxPanel<Object>, Object> showComboBoxDialog(@NotNull String pMessage, @NotNull List<Object> pOptions)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> panelFactory.createComboBoxPanel(pMessage, pOptions),
                                                                       "", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public IStashChangesQuestionDialogResult<StashChangesQuestionDialog, Object> showStashChangesQuestionDialog(@NotNull Observable<Optional<IRepository>> pRepositoryObs,
                                                                                                              @NotNull List<IFileChangeType> pFilesToRevert,
                                                                                                              @NotNull File pProjectDir)
  {
    return new StashChangesQuestionDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                                     dialogFactory.createStashChangesQuestionDialog(pRepositoryObs, pFilesToRevert,
                                                                                                                                    pProjectDir),
                                                                                 "Local changes detected",
                                                                                 List.of(EButtons.STASH_CHANGES, EButtons.DISCARD_CHANGES, EButtons.ABORT)
                                                                                     .toArray(new EButtons[0])));
  }

  @Override
  public <T> IUserPromptDialogResult<?, T> showDialog(@NotNull AditoBaseDialog<T> pComponent, @NotNull String pTitle, @NotNull List<EButtons> pButtonList,
                                                      @NotNull List<EButtons> pOkayButtons)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> pComponent, pTitle, pButtonList.toArray(new EButtons[0])))
    {
      @Override
      public boolean isOkay()
      {
        return pOkayButtons.contains(selectedButton);
      }
    };
  }

  @Override
  public IPanelFactory getPanelFactory()
  {
    return panelFactory;
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
    public boolean isAbort()
    {
      return selectedButton == EButtons.ABORT || selectedButton == EButtons.ESCAPE;
    }
  }
}
