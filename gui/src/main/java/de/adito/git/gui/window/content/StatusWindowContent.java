package de.adito.git.gui.window.content;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.*;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.ObservableTreePanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.MutableIconActionButton;
import de.adito.git.gui.tree.StatusTree;
import de.adito.git.gui.tree.models.*;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
class StatusWindowContent extends ObservableTreePanel implements IDiscardable, ILookupComponent<File>
{

  private static final String STANDARD_ACTION_STRING = "STANDARD_ACTION";

  private final IIconLoader iconLoader;
  private final IPrefStore prefStore;
  private final Observable<Optional<IRepository>> repository;
  private final IActionProvider actionProvider;
  private Observable<Optional<List<IFileChangeType>>> selectionObservable;
  private StatusTree statusTree;
  private BaseObservingTreeModel<IFileChangeType> statusTreeModel;
  private Action openFileAction;
  private final List<IDiscardable> discardableActions = new ArrayList<>();
  private ObservableTreeUpdater<IFileChangeType> treeUpdater;
  private final BehaviorSubject<Observable<Optional<List<IFileChangeType>>>> subject = BehaviorSubject.createDefault(Observable.just(Optional.empty()));
  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposables = new CompositeDisposable();

  @Inject
  StatusWindowContent(IIconLoader pIconLoader, IFileSystemUtil pFileSystemUtil, IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider,
                      IPrefStore pPrefStore, IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository,
                      @Assisted Supplier<Multimap<Integer, Component>> pPopupMenuEntries)
  {
    super();
    iconLoader = pIconLoader;
    prefStore = pPrefStore;
    repository = pRepository;
    actionProvider = pActionProvider;
    disposables.add(new ObservableCacheDisposable(observableCache));
    pProgressFacade.executeInBackground("Preparing status window", pHandle -> {
      Observable<Optional<IFileStatus>> status = repository
          .switchMap(pRepo -> pRepo
              .map(IRepository::getStatus)
              .orElse(Observable.just(Optional.empty())))
          .debounce(500, TimeUnit.MILLISECONDS);
      File projectDirectory = repository.blockingFirst().map(IRepository::getTopLevelDirectory)
          .orElseThrow(() -> new RuntimeException("could not determine project root directory"));
      Observable<List<IFileChangeType>> changedFilesObs = status.map(pOptStatus -> pOptStatus.map(IFileStatus::getUncommitted).orElse(List.of())).distinctUntilChanged();
      boolean useFlatTree = Constants.TREE_VIEW_FLAT.equals(pPrefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY));
      statusTreeModel = useFlatTree ? new FlatStatusTreeModel(projectDirectory) : new StatusTreeModel(projectDirectory);
      statusTree = new StatusTree(pQuickSearchProvider, pFileSystemUtil, statusTreeModel, useFlatTree,
                                  projectDirectory, treeViewPanel, treeScrollpane);
      Runnable[] doAfterJobs = {this::showTree};
      treeUpdater = new ObservableTreeUpdater<>(changedFilesObs, statusTreeModel, pFileSystemUtil, doAfterJobs, this::showLoading);

      selectionObservable = statusTree.getSelectionObservable();
      subject.onNext(selectionObservable);
      openFileAction = actionProvider.getOpenFileAction(selectionObservable);
      statusTree.getTree().addMouseListener(new _DoubleClickListener());
      _initGui(projectDirectory, pPopupMenuEntries);
    });
  }

  private void _initGui(@NotNull File pProjectDirectory, Supplier<Multimap<Integer, Component>> pPopupMenuEntries)
  {
    setLayout(new BorderLayout());
    _initActions(pProjectDirectory, pPopupMenuEntries);
    add(treeViewPanel, BorderLayout.CENTER);
  }

  private void _initActions(@NotNull File pProjectDirectory, Supplier<Multimap<Integer, Component>> pPopupMenuEntries)
  {
    Action commitAction = actionProvider.getCommitAction(repository, selectionObservable, "");
    Action diffToHeadAction = actionProvider.getDiffToHeadAction(repository, selectionObservable, true);
    Action showCommitsForFileAction = actionProvider.getShowCommitsForFileAction(repository, selectionObservable
        .map(pFileChangeTypes -> pFileChangeTypes
            .map(pChangeTypes -> pChangeTypes.stream()
                .map(IFileChangeType::getFile)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList())));
    Action revertWorkDirAction = actionProvider.getRevertWorkDirAction(repository, selectionObservable);
    Action ignoreAction = actionProvider.getIgnoreAction(repository, selectionObservable);
    Action excludeAction = actionProvider.getExcludeAction(repository, selectionObservable);
    Action createPatchAction = actionProvider.getCreatePatchAction(repository, selectionObservable);
    Action applyPatchAction = actionProvider.getApplyPatchAction(repository);
    JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getRefreshStatusAction(repository, statusTreeModel::reload));
    toolBar.addSeparator();
    toolBar.add(commitAction);
    toolBar.add(revertWorkDirAction);
    toolBar.addSeparator();
    toolBar.add(diffToHeadAction);
    toolBar.add(showCommitsForFileAction);
    toolBar.addSeparator();
    toolBar.add(actionProvider.getExpandTreeAction(statusTree.getTree()));
    toolBar.add(actionProvider.getCollapseTreeAction(statusTree.getTree()));
    toolBar.add(new MutableIconActionButton(actionProvider.getSwitchTreeViewAction(statusTree.getTree(), pProjectDirectory, this.getClass().getName(), treeUpdater),
                                            () -> Constants.TREE_VIEW_FLAT.equals(this.getClass().getName() + prefStore.get(Constants.TREE_VIEW_TYPE_KEY)),
                                            iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_HIERARCHICAL),
                                            iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT))
                    .getButton());
    treeViewPanel.add(toolBar, BorderLayout.WEST, 0);

    Multimap<Integer, Action> actions = ArrayListMultimap.create();
    actions.put(0, openFileAction);
    actions.put(100, null);
    actions.put(200, commitAction);
    actions.put(300, ignoreAction);
    actions.put(400, excludeAction);
    actions.put(500, revertWorkDirAction);
    actions.put(600, null);
    actions.put(700, diffToHeadAction);
    actions.put(800, showCommitsForFileAction);
    actions.put(900, null);
    actions.put(1000, createPatchAction);
    actions.put(1100, applyPatchAction);
    actions.put(1200, null);
    actions.put(1300, actionProvider.getResolveConflictsAction(repository, selectionObservable));
    actions.put(1400, actionProvider.getMarkResolvedAction(repository, selectionObservable));
    statusTree.getTree().addMouseListener(new PopupMouseListener(() -> _getPopupMenu(actions, pPopupMenuEntries)));
    statusTree.getTree().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), STANDARD_ACTION_STRING);
    statusTree.getTree().getActionMap().put(STANDARD_ACTION_STRING, openFileAction);

    discardableActions.add((IDiscardable) commitAction);
    discardableActions.add((IDiscardable) revertWorkDirAction);
    discardableActions.add((IDiscardable) diffToHeadAction);
    discardableActions.add((IDiscardable) showCommitsForFileAction);
    discardableActions.add((IDiscardable) ignoreAction);
    discardableActions.add((IDiscardable) excludeAction);
    discardableActions.add((IDiscardable) openFileAction);
    discardableActions.add((IDiscardable) createPatchAction);
    discardableActions.add((IDiscardable) applyPatchAction);
  }

  private JPopupMenu _getPopupMenu(@NotNull Multimap<Integer, Action> pActions, @NotNull Supplier<Multimap<Integer, Component>> pAdditionalComponents)
  {
    JPopupMenu popupMenu = new JPopupMenu();
    Multimap<Integer, Component> componentMultimap = pAdditionalComponents.get();
    List<Integer> actionPositions = new ArrayList<>();
    actionPositions.addAll(pActions.keySet());
    actionPositions.addAll(componentMultimap.keySet());
    actionPositions.sort(Integer::compare);
    for (Integer actionPosition : actionPositions)
    {
      for (Action action : pActions.get(actionPosition))
      {
        if (action == null)
        {
          popupMenu.addSeparator();
        }
        else
        {
          popupMenu.add(action);
        }
      }
      for (Component component : componentMultimap.get(actionPosition))
      {
        if (component == null)
        {
          popupMenu.addSeparator();
        }
        else
        {
          popupMenu.add(component);
        }
      }
    }
    return popupMenu;
  }

  @Override
  public void discard()
  {
    statusTreeModel.discard();
    statusTree.discard();
    discardableActions.forEach(IDiscardable::discard);
    treeUpdater.discard();
    disposables.dispose();
  }

  @Override
  protected JTree getTree()
  {
    return statusTree.getTree();
  }

  @NotNull
  @Override
  public JComponent getComponent()
  {
    return this;
  }

  @NotNull
  @Override
  public Observable<Optional<List<File>>> observeSelectedItems()
  {
    return observableCache.calculateParallel("selectedItems", () -> subject.switchMap(pObs -> pObs
        .map(pOpt -> pOpt
            .map(pChangeTypes -> pChangeTypes
                .stream()
                .map(IFileChangeType::getFile)
                .collect(Collectors.toList())))));
  }

  private class _DoubleClickListener extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent pEvent)
    {
      if (pEvent.getClickCount() == 2 && pEvent.getSource() instanceof JTree)
      {
        JTree source = (JTree) pEvent.getSource();
        TreePath sourcePath = source.getClosestPathForLocation(pEvent.getX(), pEvent.getY());
        if (source.isPathSelected(sourcePath) && statusTree.getTree().getModel().isLeaf(sourcePath.getLastPathComponent()))
          openFileAction.actionPerformed(null);
      }
    }
  }

}
