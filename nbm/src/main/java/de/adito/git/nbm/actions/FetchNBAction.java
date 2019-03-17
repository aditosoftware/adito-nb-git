package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * @author m.kaspera, 17.03.2019
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.FetchNBAction")
@ActionRegistration(displayName = "LBL_FetchNBAction_Name")
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.FETCH_ACTION_RIGHT_CLICK)
public class FetchNBAction extends NBAction
{


  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    try
    {
      actionProvider.getFetchAction(repository).actionPerformed(null);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    return findOneRepositoryFromNode(pActivatedNodes).blockingFirst().isPresent();
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(FetchNBAction.class, "LBL_FetchNBAction_Name");
  }
}
