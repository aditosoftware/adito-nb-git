package de.adito.git.gui.actions;

import de.adito.git.api.*;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.dialogs.results.INewBranchDialogResult;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.NonNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 01.07.2019
 */
abstract class AbstractNewBranchAction extends AbstractAction implements IDiscardable
{

  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<ICommit>> startingPoint;
  private final Disposable disposable;
  private final INotifyUtil notifyUtil;

  AbstractNewBranchAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, Observable<Optional<IRepository>> pRepository,
                          Observable<Optional<ICommit>> pStartingPoint, @NonNull INotifyUtil pNotifyUtil)
  {
    this(pProgressFacade, pDialogProvider, pRepository, pStartingPoint, Observable.just(Optional.of(true)), pNotifyUtil);
  }

  /**
   * @param pProgressFacade ProgressFacade which is used to report back the progess of the task
   * @param pDialogProvider The Interface to provide functionality of giving an overlying framework
   */
  AbstractNewBranchAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, Observable<Optional<IRepository>> pRepository,
                          Observable<Optional<ICommit>> pStartingPoint, Observable<Optional<Boolean>> pIsValidObservable, @NonNull INotifyUtil pNotifyUtil)
  {
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
    startingPoint = pStartingPoint;
    disposable = pIsValidObservable.subscribe(pIsValid -> setEnabled(pIsValid.orElse(false)));
    notifyUtil = pNotifyUtil;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    INewBranchDialogResult<?, Boolean> result = dialogProvider.showNewBranchDialog(repository);
    if (result.isCreateBranch())
    {
      progressFacade.executeAndBlockWithProgress("Creating branch " + result.getMessage(), pHandle -> {
        IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));
        GitIndexLockUtil.checkAndHandleLockedIndexFile(repo, dialogProvider, notifyUtil);
        repo.createBranch(result.getMessage(), startingPoint.blockingFirst().orElse(null), result.getInformation());
      });
    }
  }

  @Override
  public void discard()
  {
    if (disposable != null)
      disposable.dispose();
  }
}
