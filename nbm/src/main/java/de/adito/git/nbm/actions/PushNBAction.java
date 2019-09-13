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
@ActionID(category = "System", id = "de.adito.git.nbm.actions.PushNBAction")
@ActionRegistration(displayName = "LBL_PushNBAction_Name")
@ActionReferences({
    //Reference for the toolbar
    @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = INBActionPositions.PUSH_ACTION_TOOLBAR),
    //Reference for the menu
    @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.PUSH_ACTION_RIGHT_CLICK)
})
public class PushNBAction extends NBAction
{

  /**
   * @param pActivatedNodes The activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    actionProvider.getPushAction(repository).actionPerformed(null);
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_PushNBAction_Path");
  }

  /**
   * @param pActivatedNodes The activated nodes in NetBeans
   * @return return true, if there is one repository for the nodes
   */
  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    return getCurrentRepository(pActivatedNodes).blockingFirst().isPresent();
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(PushNBAction.class, "LBL_PushNBAction_Name");
  }

}
