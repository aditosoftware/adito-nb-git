package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IRepositoryState;
import de.adito.git.api.progress.IAsyncProgressFacade;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action that checks out a commit and leaves the repository in a HEADless state
 *
 * @author m.kaspera, 12.09.2019
 */
public class CheckoutCommitAction extends AbstractTableAction
{

  private INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  public CheckoutCommitAction(INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade,
                              @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super("Checkout Commit", _getIsEnabledObservable(pSelectedCommitObservable, pRepository));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    if (selectedCommits.size() == 1)
    {
      progressFacade.executeInBackground("Resetting to commit " + selectedCommits.get(0).getId(), pHandle -> {
        repository.blockingFirst()
            .orElseThrow(() -> new RuntimeException("no valid repository found"))
            .checkout(selectedCommits.get(0).getId());
        notifyUtil.notify("Checkout successful", "Checkout of commit with id "
            + selectedCommits.get(0).getId() + " was successful", false);
      });
    }
  }

  /**
   * Checks if exactly one commit is selected and the repository is in a state that allows a chechout
   *
   * @param pSelectedCommitObservable Observable with the currently selected commit
   * @param pRepository               Observable of the repository
   * @return Observable that signifies if the action is enabled or disabled
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(@NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                                                       @NotNull Observable<Optional<IRepository>> pRepository)
  {
    Observable<Optional<IRepositoryState>> repoState = pRepository.switchMap(pRepoOpt -> pRepoOpt.map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    return Observable.combineLatest(pSelectedCommitObservable, repoState, (pSelectedCommitOpt, pRepoStateOpt)
        -> Optional.of(pSelectedCommitOpt.orElse(Collections.emptyList()).size() == 1 && pRepoStateOpt.map(IRepositoryState::canCheckout).orElse(false)));
  }
}
