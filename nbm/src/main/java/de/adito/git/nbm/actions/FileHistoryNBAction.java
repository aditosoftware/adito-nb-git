package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.*;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.*;

/**
 * Action that shows a list with all commits that affected the selected file. Only active if exactly one file is selected
 *
 * @author m.kaspera, 06.02.2019
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.FileHistoryNBAction")
@ActionRegistration(displayName = "#LBL_FileHistoryNBAction_Name")
//Reference for the menu
@ActionReference(name = "Show file history", path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.SHOW_FILE_HISTORY_ACTION_RIGHT_CLICK,
    separatorAfter = INBActionPositions.SHOW_FILE_HISTORY_ACTION_RIGHT_CLICK + 1)
public class FileHistoryNBAction extends NBAction
{

  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = getCurrentRepository(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    actionProvider.getShowCommitsForFileAction(repository, Observable.just(getAllFilesOfNodes(pActivatedNodes))).actionPerformed(null);
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  private boolean isEnabled(@Nullable IRepository pRepository)
  {
    if (pRepository == null)
      return false;
    List<File> filesOfNodes = getAllFilesOfNodes(lastActivated);
    return filesOfNodes.size() == 1 && !pRepository.getTopLevelDirectory().equals(filesOfNodes.get(0));
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_FileHistoryNBAction_Path");
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(FileHistoryNBAction.class, "LBL_FileHistoryNBAction_Name");
  }
}
