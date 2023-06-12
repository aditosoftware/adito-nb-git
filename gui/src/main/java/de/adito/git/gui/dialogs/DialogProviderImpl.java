package de.adito.git.gui.dialogs;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.git.IBeforeCommitAction;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.*;
import de.adito.git.gui.DelayedSupplier;
import de.adito.git.gui.NewFileDialog;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.panels.*;
import de.adito.git.gui.dialogs.results.*;
import de.adito.git.gui.swing.CommitDialogConditionalButton;
import de.adito.git.gui.swing.ConditionalDialogButton;
import de.adito.git.gui.swing.MergeConflictConditionalButton;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
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
  public @NonNull IMergeConflictDialogResult<MergeConflictDialog, Object> showMergeConflictDialog(@NonNull Observable<Optional<IRepository>> pRepository,
                                                                                                  @NonNull IMergeDetails pMergeDetails, boolean pOnlyConflicting,
                                                                                                  boolean pShowAutoResolve, String... pDialogTitle)
  {
    DialogResult<MergeConflictDialog, Object> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictDialog(pValidConsumer, pRepository, pMergeDetails, pOnlyConflicting,
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
  public @NonNull IMergeConflictResolutionDialogResult<MergeConflictResolutionDialog, Object> showMergeConflictResolutionDialog(@NonNull IMergeData pMergeDiff,
                                                                                                                                @NonNull String pYoursOrigin,
                                                                                                                                @NonNull String pTheirsOrigin)
  {
    DialogResult<MergeConflictResolutionDialog, Object> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictResolutionDialog(pMergeDiff, pYoursOrigin, pTheirsOrigin),
                                          "Conflict resolution for file "
                                              + pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getFilePath(),
                                          List.of(new MergeConflictConditionalButton(pMergeDiff, this), EButtons.ACCEPT_YOURS, EButtons.ACCEPT_THEIRS, EButtons.CANCEL).toArray(new Object[0]));
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
      super(pDialogResult.getSource(), getPressedButton(pDialogResult.getSelectedButton()), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean isAcceptChanges()
    {
      return selectedButton == EButtons.ACCEPT_CHANGES;
    }
  }

  @Override
  public @NonNull IDiffDialogResult<DiffDialog, Object> showDiffDialog(@NonNull File pProjectDirectory, @NonNull List<IFileDiff> pFileDiffs,
                                                                       @Nullable String pSelectedFile, @Nullable String pTitle, @Nullable String pLeftHeader,
                                                                       @Nullable String pRightHeader, boolean pAcceptChange, boolean pShowFileTree)
  {
    DialogResult<DiffDialog, Object> result = null;
    try
    {
      String title;
      if (pTitle != null)
        title = pTitle;
      else
      {
        title = "Diff for file ";
        if (pSelectedFile != null)
          title += pSelectedFile;
        else if (!pFileDiffs.isEmpty())
          title += pFileDiffs.get(0).getFileHeader().getFilePath();
      }
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createDiffDialog(pProjectDirectory, pFileDiffs, pSelectedFile, pLeftHeader, pRightHeader,
                                                                                           pAcceptChange, pShowFileTree),
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
  public @NonNull ICommitDialogResult<CommitDialog, CommitDialogResult> showCommitDialog(@NonNull Observable<Optional<IRepository>> pRepository,
                                                                                         @NonNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                                                                         @NonNull String pMessageTemplate)
  {
    DialogResult<CommitDialog, CommitDialogResult> result = null;
    try
    {
      DelayedSupplier<List<File>> filesToCommitSupplier = new DelayedSupplier<>();
      DelayedSupplier<List<IBeforeCommitAction>> beforeCommitActions = new DelayedSupplier<>();
      result = dialogDisplayer.showDialog(pIsValidDescriptor -> dialogFactory.createCommitDialog(pIsValidDescriptor, pRepository, pFilesToCommit,
                                                                                                 pMessageTemplate, beforeCommitActions, filesToCommitSupplier),
                                          "Commit", List.of(new CommitDialogConditionalButton(EButtons.COMMIT, beforeCommitActions, filesToCommitSupplier),
                                                            new CommitDialogConditionalButton(EButtons.COMMIT_AND_PUSH, beforeCommitActions, filesToCommitSupplier),
                                                            EButtons.CANCEL).toArray(new Object[0]));
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
      super(pDialogResult.getSource(), getPressedButton(pDialogResult.getSelectedButton()), pDialogResult.getMessage(), pDialogResult.getInformation());
    }

    @Override
    public boolean doCommit()
    {
      return selectedButton == EButtons.COMMIT || selectedButton == EButtons.COMMIT_AND_PUSH;
    }

    @Override
    public boolean isPush()
    {
      return selectedButton == EButtons.COMMIT_AND_PUSH;
    }
  }

  @Override
  public @NonNull INewBranchDialogResult<NewBranchDialog, Boolean> showNewBranchDialog(@NonNull Observable<Optional<IRepository>> pRepository)
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
  public @NonNull IResetDialogResult<ResetDialog, EResetType> showResetDialog()
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
  public @NonNull IPushDialogResult<PushDialog, Boolean> showPushDialog(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull List<ICommit> pCommitList)
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
  public @NonNull IStashedCommitSelectionDialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(
      @NonNull Observable<Optional<IRepository>> pRepo, @NonNull List<ICommit> pStashedCommits)
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
  public @NonNull IUserPromptDialogResult<PasswordPromptDialog, char[]> showPasswordPromptDialog(@NonNull String pMessage)
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
  public @NonNull IUserPromptDialogResult<UserPromptPanel, Object> showUserPromptDialog(@NonNull String pMessage, @Nullable String pDefault)
  {
    final String defaultValue = pDefault;
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createUserPromptPanel(defaultValue),
                                                                       pMessage, List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public @NonNull IUserPromptDialogResult<NotificationPanel, Object> showYesNoDialog(@NonNull String pMessage)
  {
    return this.showYesNoDialog(pMessage, pMessage);
  }

  @Override
  public @NonNull IUserPromptDialogResult<NotificationPanel, Object> showYesNoDialog(@NonNull String pTitle, @NonNull String pMessage)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createNotificationPanel(pMessage),
                                                                       pTitle, List.of(EButtons.YES, EButtons.NO).toArray(new EButtons[0])))
    {
      @Override
      public boolean isOkay()
      {
        return selectedButton == EButtons.YES;
      }
    };
  }

  @Override
  public IUserPromptDialogResult<NotificationPanel, Object> showMessageDialog(@Nullable String pDialogTitle, @NonNull String pMessage,
                                                                              @NonNull List<EButtons> pShownButtons, @NonNull List<EButtons> pOkayButtons)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createNotificationPanel(pMessage),
                                                                       pDialogTitle == null ? "Git Plugin" : pDialogTitle, pShownButtons.toArray(new EButtons[0])))
    {
      @Override
      public boolean isOkay()
      {
        return pOkayButtons.contains(selectedButton);
      }
    };
  }

  @NonNull
  @Override
  public IChangeTrackedBranchDialogResult<NotificationPanel, Object> showChangeTrackedBranchDialog(@NonNull String pMessage)
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

  @NonNull
  @Override
  public IRevertDialogResult<RevertFilesDialog, Object> showRevertDialog(@NonNull Observable<Optional<IRepository>> pRepositoryObs,
                                                                         @NonNull List<IFileChangeType> pFilesToRevert, @NonNull File pProjectDirectory)
  {

    DialogResult<RevertFilesDialog, Object> result = null;
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
  public @NonNull IFileSelectionDialogResult<FileSelectionDialog, Object> showFileSelectionDialog(@NonNull String pMessage,
                                                                                                  @NonNull FileChooserProvider.FileSelectionMode pFileSelectionMode,
                                                                                                  @Nullable FileFilter pFileFilter)
  {
    DialogResult<FileSelectionDialog, Object> result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory
                                                                                      .createFileSelectionDialog(pFileSelectionMode, pFileFilter),
                                                                                  pMessage, List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0]));
    return new FileSelectionDialogResultImpl<>(result);
  }

  @Override
  public @NonNull IFileSelectionDialogResult<NewFileDialog, Object> showNewFileDialog(@NonNull String pMessage,
                                                                                      @NonNull FileChooserProvider.FileSelectionMode pFileSelectionMode,
                                                                                      @Nullable FileFilter pFileFilter, @Nullable String pFileName)
  {
    DialogResult<NewFileDialog, Object> result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory
                                                                                .createNewFileDialog(pFileSelectionMode, pFileFilter, pFileName),
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
  public @NonNull IGitConfigDialogResult<GitConfigDialog, Multimap<String, Object>> showGitConfigDialog(@NonNull Observable<Optional<IRepository>> pRepository)
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
  public @NonNull IStashChangesDialogResult<StashChangesDialog, StashChangesResult> showStashChangesDialog()
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
  public @NonNull IUserPromptDialogResult<SshInfoPrompt, char[]> showSshInfoPromptDialog(@NonNull String pMessage, @Nullable String pSshKeyLocation,
                                                                                         @Nullable char[] pPassphrase, @NonNull IKeyStore pKeyStore)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           dialogFactory.createSshInfoPromptDialog(pMessage, pSshKeyLocation, pValidConsumer,
                                                                                                                   pPassphrase, pKeyStore),
                                                                       "SSH key information", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public @NonNull IUserPromptDialogResult<CheckboxPanel, Boolean> showCheckboxPrompt(@NonNull String pMessage, @NonNull String pCheckboxText)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                           panelFactory.createCheckboxPanel(pMessage, pCheckboxText),
                                                                       "", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @NonNull
  @Override
  public DialogResult<TagOverviewDialog, Object> showTagOverviewDialog(@NonNull Consumer<ICommit> pSelectedCommitCallback,
                                                                       @NonNull Observable<Optional<IRepository>> pRepository)
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
  public IUserPromptDialogResult<ComboBoxPanel<Object>, Object> showComboBoxDialog(@NonNull String pMessage, @NonNull List<Object> pOptions)
  {
    return new UserPromptDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer -> panelFactory.createComboBoxPanel(pMessage, pOptions),
                                                                       "", List.of(EButtons.OK, EButtons.CANCEL).toArray(new EButtons[0])));
  }

  @Override
  public IStashChangesQuestionDialogResult<StashChangesQuestionDialog, Object> showStashChangesQuestionDialog(@NonNull Observable<Optional<IRepository>> pRepositoryObs,
                                                                                                              @NonNull List<IFileChangeType> pFilesToRevert,
                                                                                                              @NonNull File pProjectDir)
  {
    return new StashChangesQuestionDialogResultImpl<>(dialogDisplayer.showDialog(pValidConsumer ->
                                                                                     dialogFactory.createStashChangesQuestionDialog(pRepositoryObs, pFilesToRevert,
                                                                                                                                    pProjectDir),
                                                                                 "Local changes detected",
                                                                                 List.of(EButtons.STASH_CHANGES, EButtons.DISCARD_CHANGES, EButtons.ABORT)
                                                                                     .toArray(new EButtons[0])));
  }

  @Override
  public <T> IUserPromptDialogResult<AditoBaseDialog<T>, T> showDialog(@NonNull AditoBaseDialog<T> pComponent, @NonNull String pTitle, @NonNull List<EButtons> pButtonList,
                                                                       @NonNull List<EButtons> pOkayButtons)
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

  /**
   * In case a ConditionalDialogButton is used, this method returns the button that the ConditionalDialogButton signals as set
   * Can be used to retro-actively change the pressed button in case of an ConditionalDialogButton, e.g. when the user pressed the "Commit" button, but during any of the
   * pre-commit actions clicks "Cancel" -> the commit should not be performed, and the ConditionalDialogButton will return "Cancel" as the pressed button
   *
   * @param pOriginalPressedButton Object/Button to check
   * @return the object itself in case it is not a ConditionalDialogButton, the button that the ConditionalDialogButton reports as pressed otherwise
   */
  private static Object getPressedButton(@NonNull Object pOriginalPressedButton)
  {
    if (pOriginalPressedButton instanceof ConditionalDialogButton)
    {
      return ((ConditionalDialogButton) pOriginalPressedButton).getPressedButton();
    }
    return pOriginalPressedButton;
  }
}
