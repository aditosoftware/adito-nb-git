package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import de.adito.git.gui.actions.commands.StashCommand;
import de.adito.git.gui.dialogs.EButtons;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.panels.CompositePanel;
import de.adito.git.gui.dialogs.panels.NotificationPanel;
import de.adito.git.gui.dialogs.panels.UserPromptPanel;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
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
  private final Observable<Optional<IRepository>> repositoryObservable;
  private final ISaveUtil saveUtil;
  private final MergeConflictSequence mergeConflictSequence;
  private final Observable<Optional<IBranch>> branchObservable;

  /**
   * @param pRepository The repository where the branch is
   * @param pBranch     the branch list of selected branches
   */
  @Inject
  CheckoutAction(IPrefStore pPrefStore, IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFactory, ISaveUtil pSaveUtil,
                 MergeConflictSequence pMergeConflictSequence, @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<IBranch>> pBranch)
  {
    super(Util.getResource(CheckoutAction.class, "checkoutTitle"), _getIsEnabledObservable(pRepository, pBranch));
    prefStore = pPrefStore;
    dialogProvider = pDialogProvider;
    progressFactory = pProgressFactory;
    saveUtil = pSaveUtil;
    mergeConflictSequence = pMergeConflictSequence;
    branchObservable = pBranch;
    putValue(Action.NAME, Util.getResource(CheckoutAction.class, "checkoutTitle"));
    putValue(Action.SHORT_DESCRIPTION, Util.getResource(CheckoutAction.class, "checkoutTooltip"));
    repositoryObservable = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    saveUtil.saveUnsavedFiles();
    IRepository repository = repositoryObservable.blockingFirst().orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));
    Optional<IBranch> branchOpt = branchObservable.blockingFirst();
    if (branchOpt.isPresent())
    {
      IBranch branch = branchOpt.get();
      progressFactory.executeAndBlockWithProgress(Util.getResource(CheckoutAction.class, "checkoutProgressMsg") + " " + branch.getSimpleName(), pProgress -> {
        try
        {
          repository.setUpdateFlag(false);
          if (repository.getStatus().blockingFirst().map(IFileStatus::hasUncommittedChanges).orElse(false) &&
              !ActionUtility.handleStash(prefStore, dialogProvider, repository, STASH_ID_KEY, pProgress))
          {
            return;
          }
          _doCheckout(repository, branch);
        }
        finally
        {
          _unstash(repository, pProgress);
        }
      });
    }
  }

  private void _doCheckout(IRepository pRepository, IBranch pBranch) throws AditoGitException
  {
    if (pBranch.getType() == EBranchType.REMOTE)
    {
      UserPromptPanel userPromptPanel = dialogProvider.getPanelFactory().createUserPromptPanel(pBranch.getSimpleName().replace("origin/", ""));
      NotificationPanel notificationPanel = dialogProvider.getPanelFactory().createNotificationPanel(Util.getResource(CheckoutAction.class, "checkoutChooseNameMsg"));
      IUserPromptDialogResult<?, Object> promptDialogResult = dialogProvider.showDialog(
          new CompositePanel<>(List.of(notificationPanel, userPromptPanel), userPromptPanel::getMessage),
          Util.getResource(CheckoutAction.class, "checkoutChooseNameTitle"),
          List.of(EButtons.OK, EButtons.CANCEL), List.of(EButtons.OK));
      if (promptDialogResult.isOkay())
      {
        String branchName = (String) promptDialogResult.getInformation();
        pRepository.checkoutRemote(pBranch, branchName);
      }
    }
    else
    {
      pRepository.checkout(pBranch);
    }
  }

  private void _unstash(IRepository pRepository, @NotNull IProgressHandle pProgress)
  {
    pRepository.setUpdateFlag(true);
    pProgress.setDescription(Util.getResource(CheckoutAction.class, "checkoutProgressUnstashMsg"));
    String stashedCommitId = prefStore.get(STASH_ID_KEY);
    if (stashedCommitId != null)
    {
      try
      {
        StashCommand.doUnStashing(mergeConflictSequence, stashedCommitId, Observable.just(Optional.of(pRepository)));
      }
      finally
      {
        prefStore.put(STASH_ID_KEY, null);
      }
    }
  }

  /**
   * return an observable that contains information about whether the checkout action can be executed in the current repository state
   *
   * @param pRepository       Observable of the RepositoryObject of the current Project
   * @param pBranchObservable Observable of the Branch to check out
   * @return Observable that signals if the user may do a checkout in the current repository state. Observable is false if the selected Branch is the current one
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranchObservable)
  {
    Observable<Optional<IRepositoryState>> repositoryStateObs = pRepository.switchMap(pRepoOpt -> pRepoOpt
        .map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(repositoryStateObs, pBranchObservable, (pRepoStateOpt, pBranchOpt) -> pRepoStateOpt
        .map(pRepositoryState -> pRepositoryState.canCheckout() && pBranchOpt.isPresent() && !pBranchOpt.get().equals(pRepositoryState.getCurrentBranch())));
  }
}
