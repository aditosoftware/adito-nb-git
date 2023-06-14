package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * An action class to push all current commits
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.UnStashChangesNBAction")
@ActionRegistration(displayName = "#LBL_UnStashChangesNBAction_Name")
@ActionReferences({
    //Reference for the menu
    @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.UN_STASH_CHANGES_ACTION_RIGHT_CLICK,
        separatorAfter = INBActionPositions.UN_STASH_CHANGES_ACTION_RIGHT_CLICK + 1)
})
public class UnStashChangesNBAction extends NBAction
{

  /**
   * @param pActivatedNodes The activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    actionProvider.getUnStashChangesAction(repository).actionPerformed(null);
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(obj -> true));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(UnStashChangesNBAction.class, "LBL_UnStashChangesNBAction_Name");
  }

}
