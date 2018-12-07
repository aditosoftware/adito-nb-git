package de.adito.git.nbm.actions;

import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.openide.awt.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.util.*;

/**
 * An action class for NetBeans which adds data to the index
 *
 * @author a.arnold, 25.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.AddNBAction")
@ActionRegistration(displayName = "LBL_AddNBAction_Name")
@ActionReferences({
    //Reference for the menu
    @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 100)
})
public class AddNBAction extends NBAction
{

  /**
   * @param pActivatedNodes the activated nodes in Netbeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(pActivatedNodes);
    Subject<Optional<List<IFileChangeType>>> listFiles = BehaviorSubject.createDefault(getUncommittedFilesOfNodes(pActivatedNodes, repository));
    Injector injector = IGitConstants.INJECTOR;
    IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

    actionProvider.getAddAction(repository, listFiles).actionPerformed(null);
  }

  /**
   * @param pActivatedNodes the activated nodes in Netbeans
   * @return returns true if the files to add are real files (needs only one real file). If the files are synthetic, the return value is false.
   */
  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    for (Node node : pActivatedNodes)
    {
      final Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
      if (getUncommittedFilesOfNodes(pActivatedNodes, repository).orElse(Collections.emptyList()).isEmpty())
      {
        return false;
      }
      if (node.getLookup().lookup(FileObject.class) != null)
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(AddNBAction.class, "LBL_AddNBAction_Name");
  }

}
