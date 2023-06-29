package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import lombok.NonNull;
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
abstract class NBAction extends NodeAction implements Disposable
{

  private static final Observable<Optional<IRepository>> repositoryObservable = RepositoryUtility.getRepositoryObservable();
  private Observable<Optional<Boolean>> isEnabledObservable = null;
  private final BehaviorSubject<Object> doEnableUpdate = BehaviorSubject.createDefault(new Object());
  private CompositeDisposable disposable;
  private boolean isCurrentlyEnabled = false;
  // for caching the nodes that are passed when enabled is called by netbeans, these are usually more up-to-date than the ones retrieved by
  // TopComponent.getRegistry.getXXX()
  Node[] lastActivated = new Node[0];

  @NonNull
  static Optional<List<IFileChangeType>> getUncommittedFilesOfNodes(Node @NonNull [] pActivatedNodes,
                                                                    @NonNull Observable<Optional<IRepository>> pRepository)
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
                               .anyMatch(pFile -> pUncommittedFile.getFile().toPath().startsWith(pFile.toPath())))
                           .collect(Collectors.toList()));
  }

  /**
   * @param pActivatedNodes the active nodes from NetBeans
   * @return a list of Files from activeNodes.
   */
  @NonNull
  static List<File> getAllFilesOfNodes(Node @NonNull [] pActivatedNodes)
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
  @NonNull
  static Observable<Optional<IRepository>> getCurrentRepository(Node @NonNull [] pActivatedNodes)
  {
    return Observable.just(repositoryObservable.blockingFirst(Optional.empty()));
  }

  @Override
  protected abstract void performAction(Node[] pNodes);

  @Override
  protected boolean enable(Node[] pNodes)
  {
    lastActivated = pNodes;
    doEnableUpdate.onNext(new Object());
    if (disposable == null && isEnabledObservable == null)
    {
      disposable = new CompositeDisposable();
      Observable<Optional<IRepository>> combinedObs = Observable.combineLatest(repositoryObservable, doEnableUpdate, (pRepo, pObj) -> pRepo);
      isEnabledObservable = getIsEnabledObservable(combinedObs);
      disposable.add(isEnabledObservable.subscribe(pOptBoolean -> {
        isCurrentlyEnabled = pOptBoolean.orElse(false);
        SwingUtilities.invokeLater(() -> setEnabled(pOptBoolean.orElse(Boolean.FALSE)));
      }));
    }
    return isCurrentlyEnabled;
  }

  /**
   * @param pRepositoryObservable Observable with the currently selected Repository
   * @return Observable that tells if the Action should be enabled or disabled (enabled = true)
   */
  protected abstract Observable<Optional<Boolean>> getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepositoryObservable);

  @Override
  public void dispose()
  {
    if (disposable != null)
      disposable.dispose();
  }

  @Override
  public boolean isDisposed()
  {
    if (disposable != null)
      return disposable.isDisposed();
    return true;
  }

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
