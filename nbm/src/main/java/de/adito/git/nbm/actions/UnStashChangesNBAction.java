package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
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
@ActionRegistration(displayName = "LBL_UnStashChangesNBAction_Name")
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
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    actionProvider.getUnStashChangesAction(repository).actionPerformed(null);
  }

  /**
   * @param pActivatedNodes The activated nodes in NetBeans
   * @return return true, if there is one repository for the nodes
   */
  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    return findOneRepositoryFromNode(pActivatedNodes).blockingFirst().isPresent();
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(UnStashChangesNBAction.class, "LBL_UnStashChangesNBAction_Name");
  }

}
