package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 01.07.2019
 */
class NewBranchFromCommitAction extends NewBranchBaseAction
{
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> startingPoint;

  /**
   * @param pProgressFacade ProgressFacade which is used to report back the progess of the task
   * @param pDialogProvider The Interface to provide functionality of giving an overlying framework
   * @param pRepository     The repository where the new branch should exists
   * @param pStartingPoint  Observable of the selected startPoint, if the startPoint can only be HEAD pass Observable.just(Optional.empty()). Should be list of size 1
   */
  @Inject
  NewBranchFromCommitAction(IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                            @Assisted Observable<Optional<List<ICommit>>> pStartingPoint)
  {
    super(pProgressFacade, pDialogProvider);
    repository = pRepository;
    startingPoint = pStartingPoint;
  }

  @Override
  Observable<Optional<ICommit>> getStartPoint()
  {
    return startingPoint.map(pOpt -> pOpt.map(pList -> pList.size() == 1 ? pList.get(0) : null));
  }

  @Override
  Observable<Optional<IRepository>> getRepository()
  {
    return repository;
  }

  @Override
  String getActionName()
  {
    return "Create Branch from here";
  }

  @Override
  String getShortDescription()
  {
    return "Create a new branch with this commit as its source/origin";
  }
}
