package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 18.12.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.ExcludeAction")
@ActionRegistration(displayName = "LBL_ExcludeAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.EXCLUDE_ACTION_RIGHT_CLICK)
public class ExcludeNBAction extends IgnoreNBAction
{

  private final Subject<Optional<List<IFileChangeType>>> filesToIgnore = BehaviorSubject.create();

  /**
   * @param pActivatedNodes the active nodes in netBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IRepository currentRepo = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
    IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);

    filesToIgnore.onNext(Optional.of(getUntrackedSelectedFiles(currentRepo, pActivatedNodes)));
    actionProvider.getExcludeAction(repository, filesToIgnore).actionPerformed(null);
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_ExcludeNBAction_Path");
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(IgnoreNBAction.class, "LBL_ExcludeAction_Name");
  }

}
