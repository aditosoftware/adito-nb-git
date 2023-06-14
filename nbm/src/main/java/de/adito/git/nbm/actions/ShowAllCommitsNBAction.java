package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.util.Optional;

/**
 * NetBeans Action for calling getting a display with the commit history for all commits of all
 * branches for the selected project
 *
 * @author m.kaspera 27.11.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.ShowAllCommitsNBAction")
@ActionRegistration(displayName = "#LBL_ShowCommitLogNBAction_Name")
@ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = INBActionPositions.SHOW_ALL_COMMITS_ACTION_TOOLBAR)
public class ShowAllCommitsNBAction extends NBAction
{

  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = ProjectUtility.findProjectFromActives(TopComponent.getRegistry())
        .map(pProj -> RepositoryCache.getInstance().findRepository(pProj))
        .orElse(Observable.just(Optional.empty()));
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    actionProvider.getShowAllCommitsAction(repository).actionPerformed(null);
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_ShowAllCommitsNBAction_Path");
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(obj -> true));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(ShowAllCommitsNBAction.class, "LBL_ShowCommitLogNBAction_Name");
  }
}
