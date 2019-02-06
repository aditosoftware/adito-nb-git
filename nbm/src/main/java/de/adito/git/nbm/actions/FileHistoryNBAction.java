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
 * Action that shows a list with all commits that affected the selected file. Only active if exactly one file is selected
 *
 * @author m.kaspera, 06.02.2019
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.FileHistoryNBAction")
@ActionRegistration(displayName = "LBL_FileHistoryNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.SHOW_FILE_HISTORY_ACTION_RIGHT_CLICK)
public class FileHistoryNBAction extends NBAction
{

  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    actionProvider.getShowCommitsForFileAction(repository, Observable.just(getAllFilesOfNodes(pActivatedNodes))).actionPerformed(null);
  }

  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    return getAllFilesOfNodes(pActivatedNodes).size() == 1;
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
