package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.util.Optional;

/**
 * @author m.kaspera 06.11.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.ShowStatusWindowNBAction")
@ActionRegistration(displayName = "#LBL_ShowStatusWindowNBAction_Name")
@ActionReferences({
    @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = INBActionPositions.SHOW_STATUS_WINDOW_ACTION_TOOLBAR),
    //Reference for the menu
    @ActionReference(name = "Show local changes", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.SHOW_STATUS_WINDOW_ACTION_RIGHT_CLICK)
})
public class ShowStatusWindowNBAction extends NBAction
{

  @Override
  protected void performAction(Node[] pActiveNodes)
  {
    Observable<Optional<IRepository>> repository = ProjectUtility.findProjectFromActives(TopComponent.getRegistry())
        .map(pProj -> RepositoryCache.getInstance().findRepository(pProj))
        .orElse(Observable.just(Optional.empty()));
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    actionProvider.getShowStatusWindowAction(repository).actionPerformed(null);
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_ShowStatusWindowNBAction_Path");
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(obj -> true));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(ShowStatusWindowNBAction.class, "LBL_ShowStatusWindowNBAction_Name");
  }
}
