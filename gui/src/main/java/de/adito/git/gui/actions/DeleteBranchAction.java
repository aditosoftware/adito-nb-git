package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.IDeleteBranchDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Action for deleting a branch
 *
 * @author m.kaspera, 13.03.2019
 */
class DeleteBranchAction extends AbstractTableAction
{

  private static final String PROGRESS_MESSAGE_STRING = "Deleting branch ";
  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<IBranch>> branch;

  @Inject
  DeleteBranchAction(INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
                     @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<IBranch>> pBranch)
  {
    super("Delete Branch", _getIsEnabledObservable(pRepository, pBranch));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
    branch = pBranch;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    String branchName = branch.blockingFirst().map(IBranch::getSimpleName).orElse(null);
    if (branchName != null)
    {
      IDeleteBranchDialogResult<?, Boolean> result = dialogProvider.showDeleteBranchDialog(branchName);
      if (result.isDelete())
      {
        progressFacade.executeInBackground(PROGRESS_MESSAGE_STRING + branchName, pHandle -> {
          IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
          _deleteBranch(branchName, result.getInformation(), repo);
        });
      }
      else
      {
        notifyUtil.notify(PROGRESS_MESSAGE_STRING, "Delete was aborted", true);
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
      if (pE.getMessage().contains("Branch was not deleted as it has not been merged yet; use the force option to delete it anyway"))
      {
        IUserPromptDialogResult dialogResult = dialogProvider.showYesNoDialog("Branch contains unmerged changes, do you want to force delete it? " +
                                                                                  "(WARNING: all changes on that branch are lost)");
        if (dialogResult.isOkay())
        {
          pRepo.deleteBranch(pBranchName, pIsDeleteRemoteBranch, true);
        }
        else
        {
          notifyUtil.notify(PROGRESS_MESSAGE_STRING, "Delete was aborted", true);
          // return here so the message in the last line of this method is not printed (would have to have that last message twice otherwise)
          return;
        }
      }
      else
      {
        notifyUtil.notify(pE, "An error occurred while deleting the branch. ", false);
        return;
      }
    }
    notifyUtil.notify(PROGRESS_MESSAGE_STRING, "Deleting branch was successful", true);
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
