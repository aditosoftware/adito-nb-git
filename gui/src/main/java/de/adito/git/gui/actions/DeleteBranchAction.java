package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.EButtons;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.impl.Util;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * Action for deleting a branch
 *
 * @author m.kaspera, 13.03.2019
 */
class DeleteBranchAction extends AbstractTableAction
{

  private static final String PROGRESS_MESSAGE_STRING = Util.getResource(DeleteBranchAction.class, "deleteBranchProgressMsg");
  private static final String JGIT_UNMERGED_CHANGES_MESSAGE = "Branch was not deleted as it has not been merged yet; use the force option to delete it anyway";
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<IBranch>> branchObs;

  @Inject
  DeleteBranchAction(INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
                     @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<IBranch>> pBranchObs)
  {
    super(Util.getResource(DeleteBranchAction.class, "deleteBranchTitle"), _getIsEnabledObservable(pRepository, pBranchObs));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
    branchObs = pBranchObs;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    Optional<IBranch> branchOpt = branchObs.blockingFirst(Optional.empty());
    String branchName = branchOpt.map(IBranch::getSimpleName).orElse(null);
    IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));
    if (branchName != null)
    {
      IUserPromptDialogResult<?, Object> dialogResult = null;
      Boolean hasRemoteTrackedBranch = branchOpt.map(pBranch -> pBranch.getTrackedBranchStatus() != TrackedBranchStatus.NONE).orElse(false);
      if (hasRemoteTrackedBranch)
      {
        dialogResult = dialogProvider.showDialog(
            dialogProvider.getPanelFactory().createNotificationPanel(Util.getResource(DeleteBranchAction.class, "deleteBranchDeleteRemote")),
            Util.getResource(DeleteBranchAction.class, "deleteBranchTitle"),
            List.of(EButtons.YES, EButtons.NO, EButtons.CANCEL),
            List.of(EButtons.YES, EButtons.NO));
      }
      if (!hasRemoteTrackedBranch || dialogResult.isOkay())
      {
        boolean isDeleteRemoteBranch = hasRemoteTrackedBranch && dialogResult.getSelectedButton() == EButtons.OK;
        progressFacade.executeInBackground(PROGRESS_MESSAGE_STRING + " " + branchName, pHandle -> {
          _deleteBranch(branchName, isDeleteRemoteBranch, repo);
        });
      }
      else
      {
        notifyUtil.notify(PROGRESS_MESSAGE_STRING, Util.getResource(DeleteBranchAction.class, "deleteBranchAbortMsg"), true);
      }
    }
  }

  /**
   * @param pBranchName           Name of the branch to be deleted
   * @param pIsDeleteRemoteBranch whether or not to delete the remote-tracked branch as well (if it exists)
   * @param pRepo                 Repository on which to call the delete branch method
   * @throws AditoGitException if an error occurs during the force-delete of the branch
   */
  private void _deleteBranch(String pBranchName, Boolean pIsDeleteRemoteBranch, IRepository pRepo) throws AditoGitException
  {
    try
    {
      pRepo.deleteBranch(pBranchName, pIsDeleteRemoteBranch, false);
    }
    catch (AditoGitException pE)
    {
      if (pE.getMessage().contains(JGIT_UNMERGED_CHANGES_MESSAGE))
      {
        IUserPromptDialogResult dialogResult = dialogProvider.showDialog(
            dialogProvider.getPanelFactory().createNotificationPanel(Util.getResource(DeleteBranchAction.class, "deleteBranchUnmergedChangesMsg")),
            Util.getResource(DeleteBranchAction.class, "deleteBranchUnmergedChangesTitle"),
            List.of(EButtons.DELETE, EButtons.CANCEL), List.of(EButtons.DELETE));
        if (dialogResult.isOkay())
        {
          pRepo.deleteBranch(pBranchName, pIsDeleteRemoteBranch, true);
        }
        else
        {
          notifyUtil.notify(PROGRESS_MESSAGE_STRING, Util.getResource(DeleteBranchAction.class, "deleteBranchAbortMsg"), true);
          // return here so the message in the last line of this method is not printed (would have to have that last message twice otherwise)
          return;
        }
      }
      else
      {
        notifyUtil.notify(pE, Util.getResource(DeleteBranchAction.class, "deleteBranchErrorMsg"), false);
        return;
      }
    }
    notifyUtil.notify(PROGRESS_MESSAGE_STRING, Util.getResource(DeleteBranchAction.class, "deleteBranchSuccessMsg"), true);
  }

  /**
   * check the selection of columns in branch list
   *
   * @return Observable that is true if the selected branch is a local branch and not the currently active branch
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranchObservable)
  {
    Observable<Optional<IRepositoryState>> repositoryStateObs = pRepository.switchMap(pRepoOpt -> pRepoOpt
        .map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(repositoryStateObs, pBranchObservable, (pRepoStateOpt, pBranchOpt) ->
        pRepoStateOpt.map(pRepoState -> pBranchOpt.isPresent()
            && pBranchOpt.get().getType() == EBranchType.LOCAL
            && !pRepoState.getCurrentBranch().equals(pBranchOpt.get()))
    );
  }
}
