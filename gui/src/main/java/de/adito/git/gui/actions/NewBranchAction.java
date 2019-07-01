package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.util.Optional;

/**
 * This action class creates a newBranchWindow.
 * Also the action open the createBranch in {@link IRepository}. There is an automatically call for the remote
 *
 * @author A.Arnold 18.10.2018
 */
class NewBranchAction extends NewBranchBaseAction
{
  private Observable<Optional<IRepository>> repository;

  /**
   * @param pProgressFacade ProgressFacade which is used to report back the progess of the task
   * @param pDialogProvider The Interface to provide functionality of giving an overlying framework
   * @param pRepository     The repository where the new branch should exists
   */
  @Inject
  NewBranchAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super(pProgressFacade, pDialogProvider);
    repository = pRepository;
  }

  @Override
  Observable<Optional<ICommit>> getStartPoint()
  {
    return Observable.just(Optional.empty());
  }

  @Override
  Observable<Optional<IRepository>> getRepository()
  {
    return repository;
  }

  @Override
  String getActionName()
  {
    return "New Branch";
  }

  @Override
  String getShortDescription()
  {
    return "Create a new branch in the repository";
  }
}
