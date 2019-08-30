package de.adito.git.gui.dialogs;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.dialogs.results.StashChangesResult;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @Inject
  public DialogProviderImpl(IDialogDisplayer pDialogDisplayer, IDialogFactory pDialogFactory)
  {
    dialogDisplayer = pDialogDisplayer;
    dialogFactory = pDialogFactory;
  }

  @NotNull
  @Override
  public DialogResult showMergeConflictDialog(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull List<IMergeDiff> pMergeConflictDiffs,
                                              boolean pOnlyConflicting)
  {
    DialogResult<MergeConflictDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictDialog(pValidConsumer, pRepository, pMergeConflictDiffs, pOnlyConflicting),
                                          "Merge Conflicts");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult showMergeConflictResolutionDialog(@NotNull IMergeDiff pMergeDiff)
  {
    DialogResult<MergeConflictResolutionDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictResolutionDialog(pMergeDiff),
                                          "Conflict resolution for file "
                                              + pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileHeader().getFilePath());
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult showDiffDialog(@NotNull File pProjectDirectory, @NotNull List<IFileDiff> pFileDiffs, @Nullable String pSelectedFile, boolean pAcceptChange,
                                     boolean pShowFileTree)
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
                                          title);
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult<CommitDialog, CommitDialogResult> showCommitDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                         @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                                                         @NotNull String pMessageTemplate)
  {
    DialogResult<CommitDialog, CommitDialogResult> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pIsValidDescriptor -> dialogFactory.createCommitDialog(pIsValidDescriptor, pRepository, pFilesToCommit,
                                                                                                 pMessageTemplate),
                                          "Commit");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult<NewBranchDialog, Boolean> showNewBranchDialog(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createNewBranchDialog(pValidConsumer, pRepository), "New Branch");
  }

  @NotNull
  @Override
  public DialogResult<ResetDialog, EResetType> showResetDialog()
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createResetDialog(), "Reset");
  }

  @NotNull
  @Override
  public DialogResult<PushDialog, Boolean> showPushDialog(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull List<ICommit> pCommitList)
  {
    DialogResult<PushDialog, Boolean> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createPushDialog(pRepository, pCommitList), "List of commits to be pushed");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(@NotNull Observable<Optional<IRepository>> pRepo,
                                                                                             @NotNull List<ICommit> pStashedCommits)
  {
    DialogResult<StashedCommitSelectionDialog, String> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createStashedCommitSelectionDialog(pValidConsumer, pRepo, pStashedCommits),
                                          "Choose stashed commit");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult<PasswordPromptDialog, char[]> showPasswordPromptDialog(@NotNull String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createPasswordPromptDialog(), pMessage);
  }

  @NotNull
  @Override
  public DialogResult showUserPromptDialog(@NotNull String pMessage, @Nullable String pDefault)
  {
    final String defaultValue = pDefault;
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createUserPromptDialog(defaultValue), pMessage);
  }

  @NotNull
  @Override
  public DialogResult showYesNoDialog(@NotNull String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createYesNoDialog(pMessage), pMessage);
  }

  @NotNull
  @Override
  public DialogResult showRevertDialog(@NotNull List<IFileChangeType> pFilesToRevert, @NotNull File pProjectDirectory)
  {
    DialogResult<RevertFilesDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createRevertDialog(pFilesToRevert, pProjectDirectory), "Confirm revert of files");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult<DeleteBranchDialog, Boolean> showDeleteBranchDialog(@NotNull String pBranchName)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createDeleteBranchDialog(), "Delete Branch");
  }

  @NotNull
  @Override
  public DialogResult showFileSelectionDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createFileSelectionDialog(), pMessage);
  }

  @NotNull
  @Override
  public DialogResult<GitConfigDialog, Multimap<String, Object>> showGitConfigDialog(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    String repoName = pRepository.blockingFirst().map(pRepo -> pRepo.getTopLevelDirectory().getName()).orElse("unknown repository");
    DialogResult<GitConfigDialog, Multimap<String, Object>> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createGitConfigDialog(pRepository),
                                          "Setting for project: " + repoName);
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @NotNull
  @Override
  public DialogResult<StashChangesDialog, StashChangesResult> showStashChangesDialog()
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createStashChangesDialog(), "Stash Changes - stash message");
  }

  @NotNull
  @Override
  public DialogResult<SshInfoPrompt, char[]> showSshInfoPromptDialog(@NotNull String pMessage, @Nullable String pSshKeyLocation, @Nullable char[] pPassphrase,
                                                                     @NotNull IKeyStore pKeyStore)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createSshInfoPromptDialog(pMessage, pSshKeyLocation, pPassphrase, pKeyStore),
                                      "SSH key information");
  }

  @Override
  public @NotNull DialogResult<CheckboxPrompt, Boolean> showCheckboxPrompt(@NotNull String pMessage, @NotNull String pCheckboxText)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createCheckboxPrompt(pMessage, pCheckboxText), "");
  }

  @NotNull
  @Override
  public DialogResult<TagOverviewDialog, Object> showTagOverviewDialog(@NotNull Consumer<ICommit> pSelectedCommitCallback,
                                                                       @NotNull Observable<Optional<IRepository>> pRepository)
  {
    DialogResult<TagOverviewDialog, Object> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createTagOverviewDialog(pSelectedCommitCallback, pRepository), "Tag Overview");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @Override
  public DialogResult<ComboBoxDialog<Object>, Object> showComboBoxDialog(@NotNull String pMessage, @NotNull List<Object> pOptions)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createComboBoxDialog(pMessage, pOptions), "");
  }
}
