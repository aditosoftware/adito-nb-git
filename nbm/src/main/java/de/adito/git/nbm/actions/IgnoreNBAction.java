package de.adito.git.nbm.actions;

import com.google.inject.Injector;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.impl.data.FileChangeTypeImpl;
import de.adito.git.nbm.IGitConstants;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An action class for NetBeans which ignores files for the version control system
 *
 * @author a.arnold, 31.10.2018
 */
@ActionID(category = "System", id = "de.adito.git.nbm.actions.IgnoreAction")
@ActionRegistration(displayName = "LBL_IgnoreAction_Name")
//Reference for the menu
@ActionReference(path = IGitConstants.RIGHTCLICK_ACTION_PATH, position = 500)
public class IgnoreNBAction extends NBAction
{

  private final Subject<Optional<List<IFileChangeType>>> filesToIgnore = BehaviorSubject.create();

  /**
   * Ignore files in the version control system
   *
   * @param pActivatedNodes the activated nodes in NetBeans
   */
  @Override
  protected void performAction(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = findOneRepositoryFromNode(pActivatedNodes);
    IRepository currentRepo = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
    Injector injector = IGitConstants.INJECTOR;
    IActionProvider actionProvider = injector.getInstance(IActionProvider.class);

    List<IFileChangeType> untrackedSelectedFiles = _getUntrackedFiles(currentRepo).stream()
        .filter(untrackedFile -> getAllFilesOfNodes(pActivatedNodes)
            .stream()
            .anyMatch(selectedFile -> selectedFile.toURI()
                .equals(untrackedFile.getFile().toURI())))
        .collect(Collectors.toList());
    filesToIgnore.onNext(Optional.of(untrackedSelectedFiles));
    actionProvider.getIgnoreAction(repository, filesToIgnore).actionPerformed(null);
  }

  /**
   * @param pActivatedNodes the activated nodes in NetBeans
   * @return true if the can be ignored (no synthetic files) and the files are uncommitted, else false
   */
  @Override
  protected boolean enable(Node[] pActivatedNodes)
  {
    Observable<Optional<IRepository>> repository = NBAction.findOneRepositoryFromNode(pActivatedNodes);
    IRepository currentRepo = repository.blockingFirst().orElse(null);
    if (currentRepo == null)
      return false;
    return _getUntrackedFiles(currentRepo).stream()
        .anyMatch(untrackedFile -> getAllFilesOfNodes(pActivatedNodes)
            .stream()
            .anyMatch(selectedFile -> selectedFile.toURI()
                .equals(untrackedFile.getFile().toURI())));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(IgnoreNBAction.class, "LBL_IgnoreAction_Name");
  }

  private List<IFileChangeType> _getUntrackedFiles(IRepository pCurrentRepo)
  {
    File projectDir = pCurrentRepo.getTopLevelDirectory();
    return pCurrentRepo
        .getStatus()
        .blockingFirst()
        .getUntracked().stream()
        .map(pFilePath -> new FileChangeTypeImpl(new File(projectDir, pFilePath), EChangeType.NEW))
        .collect(Collectors.toList());
  }

}
