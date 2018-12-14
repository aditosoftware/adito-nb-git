package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.*;

/**
 * @author a.arnold, 31.10.2018
 */

@ActionID(category = "System", id = "de.adito.git.nbm.actions.RevertNBAction")
@ActionRegistration(displayName = "LBL_RevertNBAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.REVERT_ACTION_RIGHT_CLICK)
public class RevertNBAction extends NBAction
{

  private final Subject<Optional<List<IFileChangeType>>> selectedFiles = BehaviorSubject.create();

  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    selectedFiles.onNext(getUncommittedFilesOfNodes(pActivatedNodes, repository));

    actionProvider.getRevertWorkDirAction(repository, selectedFiles).actionPerformed(null);

  }

  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    return !getUncommittedFilesOfNodes(pActivatedNodes, findOneRepositoryFromNode(pActivatedNodes)).orElse(Collections.emptyList()).isEmpty();
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(RevertNBAction.class, "LBL_RevertNBAction_Name");
  }

}
