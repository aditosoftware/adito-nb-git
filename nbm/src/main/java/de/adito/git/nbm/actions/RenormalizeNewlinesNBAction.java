package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * @author m.kaspera, 04.02.2021
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.RenormalizeNewlinesNBAction")
@ActionRegistration(displayName = "#LBL_RenormalizeNewlinesNBAction_Name")
@ActionReferences({
    //Reference for the menu
    @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.RENORMALIZE_NEWLINES_RIGHT_CLICK)
})
public class RenormalizeNewlinesNBAction extends NBAction
{

  /**
   * @param pActivatedNodes The activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    actionProvider.getRenormalizeNewlinesAction(repository).actionPerformed(null);
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(obj -> true));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(RenormalizeNewlinesNBAction.class, "LBL_RenormalizeNewlinesNBAction_Name");
  }

}
