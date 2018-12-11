package de.adito.git.nbm.actions;

import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * An action class to pull the commits of a repository
 *
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.PullNBAction")
@ActionRegistration(displayName = "LBL_PullNBAction_Name")
//Reference for the menu
@ActionReferences({
    @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 100),
    @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = 200)
})
public class PullNBAction extends NBAction
{

  /**
   * get the actual repository and pull the current branch.
   *
   * @param pActivatedNodes The activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    Injector injector = IGitConstants.INJECTOR;
    IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

    try
    {
      actionProvider.getPullAction(repository).actionPerformed(null);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_PullNBAction_Path");
  }

  /**
   * @param pActivatedNodes The activated nodes of NetBeans
   * @return true if there is one repository for the files
   */
  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    return findOneRepositoryFromNode(pActivatedNodes).blockingFirst().isPresent();
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(PullNBAction.class, "LBL_PullNBAction_Name");
  }

}
