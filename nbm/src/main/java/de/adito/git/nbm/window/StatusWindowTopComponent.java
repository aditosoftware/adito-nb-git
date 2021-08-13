package de.adito.git.nbm.window;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.window.content.ILookupComponent;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A {@link AbstractRepositoryTopComponent} that shows the status of changed files in the project
 *
 * @author m.kaspera 06.11.2018
 */
public class StatusWindowTopComponent extends AbstractRepositoryTopComponent implements ExplorerManager.Provider
{

  @NotNull
  private final IPrefStore prefStore;
  private final CompositeDisposable disposable = new CompositeDisposable();
  private final ExplorerManager em;

  @Inject
  StatusWindowTopComponent(IWindowContentProvider pWindowContentProvider, @NotNull IPrefStore pPrefStore, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super(pRepository);
    prefStore = pPrefStore;
    setLayout(new BorderLayout());
    em = new ExplorerManager();
    // extract the actions that are set for the path of this topcomponent and pass them to the content component
    Supplier<Multimap<Integer, Component>> popupMenuEntriesSupplier = () -> {
      List<? extends Action> actions = Utilities.actionsForPath("de/adito/action/git/localChanges");
      JPopupMenu popupMenu = Utilities.actionsToPopup(actions.toArray(new Action[0]), ExplorerUtils.createLookup(em, getActionMap()));
      Component[] menuComponents = popupMenu.getComponents();
      Multimap<Integer, Component> componentMap = ArrayListMultimap.create();
      for (Component menuComponent : menuComponents)
      {
        componentMap.put(650, menuComponent);
      }
      if (!componentMap.keySet().isEmpty())
        componentMap.put(680, null);
      return componentMap;
    };
    ILookupComponent<File> statusWindowContent = pWindowContentProvider.createStatusWindowContent(pRepository, popupMenuEntriesSupplier);
    disposable.add(statusWindowContent.observeSelectedItems()
                       .map(pOpt -> pOpt
                           .map(pFileList -> pFileList.stream()
                               .map(FileUtil::toFileObject)
                               .filter(Objects::nonNull)
                               .collect(Collectors.toList())))
                       .distinctUntilChanged()
                       .subscribe(pOpt -> pOpt.ifPresent(this::_setActiveNodes)));
    add(statusWindowContent.getComponent());
  }

  /**
   * Sets the active nodes such that each passed fileObject is contained in the lookup of a fictional, active node
   *
   * @param pFileObjects List of FileObjects that should be in the lookup of the active nodes
   */
  private void _setActiveNodes(List<FileObject> pFileObjects)
  {
    RootNode rootNode = new RootNode(pFileObjects);
    em.setRootContext(rootNode);
    try
    {
      em.setSelectedNodes(rootNode.getChildren().getNodes());
      setActivatedNodes(rootNode.getChildren().getNodes());
    }
    catch (PropertyVetoException pE)
    {
      Logger.getLogger(StatusWindowTopComponent.class.getName()).log(Level.WARNING, pE, () -> NbBundle.getMessage(StatusWindowTopComponent.class,
                                                                                                                  "Label.setActiveNodesError"));
    }
  }

  @Override
  protected String getInitialMode()
  {
    return prefStore.get(StatusWindowTopComponent.class.getName()) == null ? "output" : prefStore.get(StatusWindowTopComponent.class.getName());
  }

  @Override
  protected String getTopComponentName()
  {
    return NbBundle.getMessage(StatusWindowTopComponent.class, "Label.StatusWindow");
  }

  @Override
  protected void componentClosed()
  {
    super.componentClosed();
    disposable.dispose();
    Mode mode = WindowManager.getDefault().findMode(this);
    if (mode != null)
      prefStore.put(StatusWindowTopComponent.class.getName(), mode.getName());
  }

  @Override
  public ExplorerManager getExplorerManager()
  {
    return em;
  }

  /**
   * Einfache Node, die im Lookup das übergebene FileObject hat
   */
  private static class FileNode extends AbstractNode
  {
    public FileNode(@NotNull FileObject pFileObject)
    {
      super(Children.LEAF, Lookups.fixed(pFileObject));
    }
  }

  /**
   * Einfache Node, die als RootNode dienen kann, da sie für die ihr übergebenen FileObjects FileNodes erzeugt und diese als ihre Kinder setzt
   */
  private static class RootNode extends AbstractNode
  {
    public RootNode(@NotNull List<FileObject> pFileObjects)
    {
      super(Children.create(new FOChildFactory(pFileObjects), false));
    }

    /**
     * ChildFactory für das Erzeugen der Kind-Nodes aus den FileObjects
     */
    private static class FOChildFactory extends ChildFactory<FileObject>
    {
      private final List<FileObject> fileObjects;

      public FOChildFactory(@NotNull List<FileObject> pFileObjects)
      {
        fileObjects = pFileObjects;
      }

      @Override
      protected boolean createKeys(@NotNull List<FileObject> toPopulate)
      {
        toPopulate.addAll(fileObjects);
        return true;
      }

      @NotNull
      @Override
      protected Node createNodeForKey(@NotNull FileObject pKey)
      {
        return new FileNode(pKey);
      }

      @NotNull
      @Override
      protected Node[] createNodesForKey(@NotNull FileObject pKey)
      {
        return new Node[]{createNodeForKey(pKey)};
      }
    }
  }

}
