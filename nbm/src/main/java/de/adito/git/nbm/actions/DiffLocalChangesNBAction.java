package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import org.jetbrains.annotations.Nullable;
import org.openide.awt.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Action for the toolbar/rightClick menu that allows the user to quickly diff the currently opened file
 *
 * @author m.kaspera, 16.03.2019
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.DiffLocalChangesNBAction")
@ActionRegistration(displayName = "LBL_DiffLocalChangesNBAction_Name")
@ActionReferences({
    @ActionReference(path = IGitConstants.TOOLBAR_ACTION_PATH, position = INBActionPositions.DIFF_LOCAL_CHANGES_ACTION_TOOLBAR),
    @ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = INBActionPositions.DIFF_LOCAL_CHANGES_ACTION_RIGHT_CLICK)
})
public class DiffLocalChangesNBAction extends NBAction
{
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    if (pActivatedNodes != null && pActivatedNodes.length == 1)
    {
      Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
      IActionProvider actionProvider = IGitConstants.INJECTOR.getInstance(IActionProvider.class);
      IFileChangeType changeType = _getIFileChangeType(pActivatedNodes, repository);
      if (changeType != null)
        actionProvider.getDiffToHeadAction(repository, Observable.just(Optional.of(List.of(changeType)))).actionPerformed(null);
    }
  }

  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    if (pActivatedNodes != null && pActivatedNodes.length == 1)
    {
      Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(pActivatedNodes);
      if (repository.blockingFirst().isPresent())
      {
        IFileChangeType changeType = _getIFileChangeType(pActivatedNodes, repository);
        return changeType != null && changeType.getChangeType() != EChangeType.SAME;
      }
    }
    return false;
  }

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(PushNBAction.class, "ICON_DiffLocalChangesAction_Path");
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(DiffLocalChangesNBAction.class, "LBL_DiffLocalChangesNBAction_Name");
  }

  @Nullable
  private static IFileChangeType _getIFileChangeType(Node[] pActivatedNodes, Observable<Optional<IRepository>> pRepository)
  {
    if (pActivatedNodes.length == 1)
    {
      FileObject fileObject = pActivatedNodes[0].getLookup().lookup(FileObject.class);
      if (fileObject != null)
      {
        File fileFromNode = new File(fileObject.getPath());
        return pRepository.blockingFirst().map(pRepo -> pRepo.getStatusOfSingleFile(fileFromNode)).orElse(null);
      }
    }
    return null;
  }
}
