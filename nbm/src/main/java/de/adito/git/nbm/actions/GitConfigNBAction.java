package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.Optional;

/**
 * @author m.kaspera, 07.01.2019
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.GitConfigNBAction")
@ActionRegistration(displayName = "#LBL_GitConfigNBAction_Name")
@ActionReference(name = "Settings", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.GIT_CONFIG_ACTION_RIGHT_CLICK)
public class GitConfigNBAction extends NBAction
{

  @Override
  protected void performAction(Node[] pNodes)
  {
    IGitConstants.INJECTOR.getInstance(IActionProvider.class).getGitConfigAction(getCurrentRepository(pNodes)).actionPerformed(null);
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_GitConfigNBAction_Path");
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(obj -> true));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(GitConfigNBAction.class, "LBL_GitConfigNBAction_Name");
  }
}
