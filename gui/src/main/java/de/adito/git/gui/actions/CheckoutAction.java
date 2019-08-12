package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.*;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * An action class to checkout to another branch.
 *
 * @author A.Arnold 18.10.2018
 */
class CheckoutAction extends AbstractTableAction
{
  private static final String STASH_ID_KEY = "checkout::stashCommitId";
  private final IPrefStore prefStore;
  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFactory;
  private Observable<Optional<IRepository>> repositoryObservable;
  private final ISaveUtil saveUtil;
  private Observable<Optional<IBranch>> branchObservable;

  /**
   * @param pRepository The repository where the branch is
   * @param pBranch     the branch list of selected branches
   */
  @Inject
  CheckoutAction(IPrefStore pPrefStore, IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFactory, ISaveUtil pSaveUtil,
                 @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<IBranch>> pBranch)
  {
    super("Checkout", _getIsEnabledObservable(pRepository));
    prefStore = pPrefStore;
    dialogProvider = pDialogProvider;
    progressFactory = pProgressFactory;
    saveUtil = pSaveUtil;
    branchObservable = pBranch;
    putValue(Action.NAME, "Checkout");
    putValue(Action.SHORT_DESCRIPTION, "Command to change the branch to another one");
    repositoryObservable = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    saveUtil.saveUnsavedFiles();
    IRepository repository = repositoryObservable.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
    Optional<IBranch> branchOpt = branchObservable.blockingFirst();
    if (branchOpt.isPresent())
    {
      IBranch branch = branchOpt.get();
      progressFactory.executeInBackground("Checking out " + branch.getSimpleName(), pProgress -> {
        try
        {
          if (repository.getStatus().blockingFirst().map(IFileStatus::hasUncommittedChanges).orElse(false))
          {
            if (ActionUtility.isAbortAutostash(prefStore, dialogProvider))
              return;
            pProgress.setDescription("Stashing uncommitted local changes");
            prefStore.put(STASH_ID_KEY, repository.stashChanges(null, true));
          }
          if (branch.getType() == EBranchType.REMOTE)
          {
            DialogResult dialogResult = dialogProvider.showUserPromptDialog("Choose a name for the local branch", branch.getSimpleName().replace("origin/", ""));
            if (dialogResult.isPressedOk())
            {
              String branchName = dialogResult.getMessage();
              repository.checkoutRemote(branch, branchName);
            }
          }
          else
          {
            repository.checkout(branch);
          }
        }
        finally
        {
          pProgress.setDescription("Un-stashing saved uncommitted local changes");
          String stashedCommitId = prefStore.get(STASH_ID_KEY);
          if (stashedCommitId != null)
          {
            try
            {
              StashCommand.doUnStashing(dialogProvider, stashedCommitId, Observable.just(Optional.of(repository)));
            }
            finally
            {
              prefStore.put(STASH_ID_KEY, null);
            }
          }
        }
      });
    }
  }

  /**
   * return an observable that contains information about whether the checkout action can be executed in the current repository state
   *
   * @param pRepository Observable of the RepositoryObject of the current Project
   * @return Observable that signals if the user may do a checkout in the current repository state
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository)
  {
    return pRepository.switchMap(pRepoOpt -> pRepoOpt
        .map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())))
        .map(pRepoStateOpt -> pRepoStateOpt.map(IRepositoryState::canCheckout));
  }
}
