package de.adito.git.nbm.actions;

import de.adito.git.api.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.*;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.io.File;
import java.util.Optional;

/**
 * @author m.kaspera, 10.06.2020
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.InitNBAction")
@ActionRegistration(displayName = "#LBL_InitNBAction_Name")
@ActionReference(name = "Init", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.GIT_INIT_RIGHT_CLICK)
public class InitNBAction extends NBAction
{

  @Override
  protected void performAction(Node[] pNodes)
  {
    ProjectUtility.findProjectFromActives(TopComponent.getRegistry()).ifPresent(pProject -> {
      INotifyUtil notifyUtil = IGitConstants.INJECTOR.getInstance(INotifyUtil.class);
      IBareRepo repo = IGitConstants.INJECTOR.getInstance(IBareRepo.class);
      try
      {
        repo.init(new File(pProject.getProjectDirectory().getPath()));
        RepositoryCache.getInstance().triggerUpdate();
        notifyUtil.notify(NbBundle.getMessage(InitNBAction.class, "TITLE_InitActionSuccess"), NbBundle.getMessage(InitNBAction.class, "MSG_InitActionSuccess"), false);
      }
      catch (AditoGitException pE)
      {
        notifyUtil.notify(pE, NbBundle.getMessage(InitNBAction.class, "MSG_InitActionFailed"), false);
      }
    });
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return null;
  }

  @Override
  protected boolean enable(Node[] pNodes)
  {
    return RepositoryUtility.getRepositoryObservable().blockingFirst(Optional.empty()).orElse(null) == null;
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(CommitNBAction.class, "LBL_InitNBAction_Name");
  }
}
