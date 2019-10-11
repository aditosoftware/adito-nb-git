package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.Constants;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.ObservableTreePanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.MutableIconActionButton;
import de.adito.git.gui.tree.StatusTree;
import de.adito.git.gui.tree.models.*;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
class StatusWindowContent extends ObservableTreePanel implements IDiscardable
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
  private List<IDiscardable> discardableActions = new ArrayList<>();
  private ObservableTreeUpdater<IFileChangeType> treeUpdater;

  @Inject
  StatusWindowContent(IIconLoader pIconLoader, IFileSystemUtil pFileSystemUtil, IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider,
                      IPrefStore pPrefStore, IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    super();
    iconLoader = pIconLoader;
    prefStore = pPrefStore;
    repository = pRepository;
    actionProvider = pActionProvider;
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
      openFileAction = actionProvider.getOpenFileAction(selectionObservable);
      statusTree.getTree().addMouseListener(new _DoubleClickListener());
      _initGui(projectDirectory);
    });
  }

  private void _initGui(File pProjectDirectory)
  {
    setLayout(new BorderLayout());
    _initActions(pProjectDirectory);
    add(treeViewPanel, BorderLayout.CENTER);
  }

  private void _initActions(File pProjectDirectory)
  {
    Action commitAction = actionProvider.getCommitAction(repository, selectionObservable, "");
    Action diffToHeadAction = actionProvider.getDiffToHeadAction(repository, selectionObservable);
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

    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(openFileAction);
    popupMenu.addSeparator();
    popupMenu.add(commitAction);
    popupMenu.add(ignoreAction);
    popupMenu.add(excludeAction);
    popupMenu.add(revertWorkDirAction);
    popupMenu.addSeparator();
    popupMenu.add(diffToHeadAction);
    popupMenu.add(showCommitsForFileAction);
    popupMenu.addSeparator();
    popupMenu.add(createPatchAction);
    popupMenu.add(applyPatchAction);
    popupMenu.addSeparator();
    popupMenu.add(actionProvider.getResolveConflictsAction(repository, selectionObservable));
    statusTree.getTree().addMouseListener(new PopupMouseListener(popupMenu));
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

  @Override
  public void discard()
  {
    statusTreeModel.discard();
    statusTree.discard();
    discardableActions.forEach(IDiscardable::discard);
    treeUpdater.discard();
  }

  @Override
  protected JTree getTree()
  {
    return statusTree.getTree();
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
