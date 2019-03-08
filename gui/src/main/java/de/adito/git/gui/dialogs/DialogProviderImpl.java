package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.dialogs.results.StashChangesResult;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
  public DialogResult showMergeConflictDialog(Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs)
  {
    DialogResult<MergeConflictDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictDialog(pValidConsumer, pRepository, pMergeConflictDiffs),
                                          "Merge Conflicts");
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @Override
  public DialogResult showMergeConflictResolutionDialog(IMergeDiff pMergeDiff)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createMergeConflictResolutionDialog(pMergeDiff),
                                      "Conflict resolution for file "
                                          + pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath());
  }

  @Override
  public DialogResult showDiffDialog(@NotNull List<IFileDiff> pFileDiffs, @Nullable String pSelectedFile, boolean pAcceptChange,
                                     boolean pShowFileTable)
  {
    DialogResult<DiffDialog, ?> result = null;
    try
    {
      String title = "Diff for file ";
      if (pSelectedFile != null)
        title += pSelectedFile;
      else
        title += pFileDiffs.get(0).getFilePath();
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createDiffDialog(pFileDiffs, pSelectedFile, pAcceptChange, pShowFileTable),
                                          title);
      return result;
    }
    finally
    {
      if (result != null)
        result.getSource().discard();
    }
  }

  @Override
  public DialogResult<CommitDialog, CommitDialogResult> showCommitDialog(Observable<Optional<IRepository>> pRepository,
                                                                         Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                                                         String pMessageTemplate)
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

  @Override
  public DialogResult showNewBranchDialog(Observable<Optional<IRepository>> pRepository)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createNewBranchDialog(pValidConsumer, pRepository), "New Branch");
  }

  @Override
  public DialogResult<ResetDialog, EResetType> showResetDialog()
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createResetDialog(), "Reset");
  }

  @Override
  public DialogResult<PushDialog, Boolean> showPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList)
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

  @Override
  public DialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(Observable<Optional<IRepository>> pRepo,
                                                                                             List<ICommit> pStashedCommits)
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

  @Override
  public DialogResult<PasswordPromptDialog, char[]> showPasswordPromptDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createPasswordPromptDialog(), pMessage);
  }

  @Override
  public DialogResult showUserPromptDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createUserPromptDialog(), pMessage);
  }

  @Override
  public DialogResult showYesNoDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createYesNoDialog(pMessage), pMessage);
  }

  @Override
  public DialogResult showFileSelectionDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createFileSelectionDialog(), pMessage);
  }

  @Override
  public DialogResult<GitConfigDialog, Map<String, String>> showGitConfigDialog(Observable<Optional<IRepository>> pRepository)
  {
    String repoName = pRepository.blockingFirst().map(pRepo -> pRepo.getTopLevelDirectory().getName()).orElse("unknown repository");
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createGitConfigDialog(pRepository),
                                      "Setting for project: " + repoName);
  }

  @Override
  public DialogResult<StashChangesDialog, StashChangesResult> showStashChangesDialog()
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createStashChangesDialog(), "Reset");
  }
}
