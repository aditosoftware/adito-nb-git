package de.adito.git.gui.dialogs;

import com.google.inject.*;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import io.reactivex.Observable;

import java.util.*;

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
                                          + pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW));
  }

  @Override
  public DialogResult showDiffDialog(List<IFileDiff> pFileDiffs)
  {
    DialogResult<DiffDialog, ?> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createDiffDialog(pFileDiffs), "DiffDialog");
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
                                                                         Observable<Optional<List<IFileChangeType>>> pFilesToCommit)
  {
    DialogResult<CommitDialog, CommitDialogResult> result = null;
    try
    {
      result = dialogDisplayer.showDialog(pIsValidDescriptor -> dialogFactory.createCommitDialog(pIsValidDescriptor, pRepository, pFilesToCommit),
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
  public DialogResult<PushDialog, Object> showPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList)
  {
    DialogResult<PushDialog, Object> result = null;
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

  public DialogResult showUserPromptDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createUserPromptDialog(), pMessage);
  }

  public DialogResult showYesNoDialog(String pMessage)
  {
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createYesNoDialog(pMessage), pMessage);
  }

  @Override
  public DialogResult<GitConfigDialog, Map<String, String>> showGitConfigDialog(Observable<Optional<IRepository>> pRepository)
  {
    String repoName = pRepository.blockingFirst().map(pRepo -> pRepo.getTopLevelDirectory().getName()).orElse("unknown repository");
    return dialogDisplayer.showDialog(pValidConsumer -> dialogFactory.createGitConfigDialog(pRepository),
                                      "Setting for project: " + repoName);
  }
}
