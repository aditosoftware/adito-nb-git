package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parent class for all NBActions, has several utility methods and implements those
 * methods from NodeAction that would be the same on all NBActions
 *
 * @author a.arnold, 25.10.2018
 */
abstract class NBAction extends NodeAction
{

  private static Observable<Optional<IRepository>> repositoryObservable = RepositoryUtility.getRepositoryObservable();
  private Observable<Optional<Boolean>> isEnabledObservable = null;
  // for caching the nodes that are passed when enabled is called by netbeans, these are usually more up-to-date than the ones retrieved by
  // TopComponent.getRegistry.getXXX()
  Node[] lastActivated = new Node[0];

  @NotNull
  static Optional<List<IFileChangeType>> getUncommittedFilesOfNodes(@NotNull Node[] pActivatedNodes,
                                                                    @NotNull Observable<Optional<IRepository>> pRepository)
  {
    List<File> files = getAllFilesOfNodes(pActivatedNodes);
    List<IFileChangeType> uncommittedFiles;

    IRepository repository = pRepository.blockingFirst().orElse(null);
    if (repository == null)
      return Optional.empty();
    uncommittedFiles = repository
        .getStatus()
        .blockingFirst()
        .map(IFileStatus::getUncommitted).orElse(Collections.emptyList());
    return Optional.of(uncommittedFiles
                           .stream()
                           .filter(pUncommittedFile -> files
                               .stream()
                               .anyMatch(pFile -> pUncommittedFile.getFile().getAbsolutePath().equals(pFile.getAbsolutePath())))
                           .collect(Collectors.toList()));
  }

  /**
   * @param pActivatedNodes the active nodes from NetBeans
   * @return a list of Files from activeNodes.
   */
  @NotNull
  static List<File> getAllFilesOfNodes(@NotNull Node[] pActivatedNodes)
  {
    List<File> fileList = new ArrayList<>();
    for (Node node : pActivatedNodes)
    {
      Lookup lookup = node.getLookup();
      if (lookup.lookup(FileObject.class) != null)
      {
        fileList.add(new File(lookup.lookup(FileObject.class).getPath()));
      }
      else if (lookup.lookup(File.class) != null)
      {
        fileList.add(lookup.lookup(File.class));
      }
      else if (lookup.lookup(DataObject.class) != null)
      {
        fileList.add(new File(lookup.lookup(DataObject.class).getPrimaryFile().getPath()));
      }
    }
    return fileList;
  }

  /**
   * get the repository of the project that contains the currently active file/node/topComponent
   *
   * @param pActivatedNodes The nodes to check the repository
   * @return The repository of the node
   */
  @NotNull
  static Observable<Optional<IRepository>> getCurrentRepository(@NotNull Node[] pActivatedNodes)
  {
    return Observable.just(repositoryObservable.blockingFirst(Optional.empty()));
  }

  @Override
  protected abstract void performAction(Node[] pNodes);

  @Override
  protected boolean enable(Node[] pNodes)
  {
    lastActivated = pNodes;
    if (isEnabledObservable == null)
    {
      isEnabledObservable = getIsEnabledObservable(repositoryObservable);
      isEnabledObservable.subscribe(pOptBoolean -> SwingUtilities.invokeLater(() -> setEnabled(pOptBoolean.orElse(Boolean.FALSE))));
    }
    return isEnabledObservable.blockingFirst().orElse(Boolean.FALSE);
  }

  /**
   * @param pRepositoryObservable Observable with the currently selected Repository
   * @return Observable that tells if the Action should be enabled or disabled (enabled = true)
   */
  protected abstract Observable<Optional<Boolean>> getIsEnabledObservable(@NotNull Observable<Optional<IRepository>> pRepositoryObservable);

  @Override
  public abstract String getName();

  @Override
  protected boolean asynchronous()
  {
    return false;
  }

  @Override
  public HelpCtx getHelpCtx()
  {
    return null;
  }
}
