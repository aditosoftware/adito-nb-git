package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.*;

/**
 * An action class which opens the commit dialog, responsible for the action in the toolbar
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.CommitNBToolbarAction")
@ActionRegistration(displayName = "LBL_CommitNBToolbarAction_Name")
@ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = INBActionPositions.COMMIT_ACTION_TOOLBAR)
public class CommitNBToolbarAction extends CommitNBAction
{

  /**
   * open the commit dialog if the repository is notNull.
   *
   * @param pActivatedNodes the activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
    Subject<Optional<List<IFileChangeType>>> listNodes;
    listNodes = BehaviorSubject.createDefault(Optional.of(repository.blockingFirst()
                                                              .orElseThrow(() -> new RuntimeException(
                                                                  NbBundle.getMessage(CommitNBToolbarAction.class, "Invalid.RepositoryNotValid")))
                                                              .getStatus()
                                                              .blockingFirst().map(IFileStatus::getUncommitted)
                                                              .orElse(Collections.emptyList())));
    actionProvider.getCommitAction(repository, listNodes).actionPerformed(null);
  }

  /**
   * @param pActivatedNodes the activated nodes in NetBeans
   * @return return true if the nodes have one repository and there are files which are not committed.
   */
  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    if (pActivatedNodes != null)
    {
      Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(pActivatedNodes);
      return repository.blockingFirst()
          .map(pRepo -> !pRepo.getStatus().blockingFirst()
              .map(pStatus -> pStatus.getUncommitted().isEmpty())
              .orElse(true))
          .orElse(false);
    }
    return false;
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(CommitNBToolbarAction.class, "LBL_CommitNBToolbarAction_Name");
  }
}
