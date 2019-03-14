package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.models.StatusTreeModel;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
class StatusWindowContent extends JPanel implements IDiscardable
{

  private final Observable<Optional<IRepository>> repository;
  private final IActionProvider actionProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectionObservable;
  private final JPanel tableViewPanel = new JPanel(new BorderLayout());
  private final SearchableTree statusTree;
  private final StatusTreeModel statusTreeModel;
  private final Action openFileAction;
  private JPopupMenu popupMenu;

  @Inject
  StatusWindowContent(IFileSystemUtil pFileSystemUtil, IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider,
                      @Assisted Observable<Optional<IRepository>> pRepository)
  {
    repository = pRepository;
    actionProvider = pActionProvider;
    Observable<Optional<IFileStatus>> status = repository
        .switchMap(pRepo -> pRepo
            .map(IRepository::getStatus)
            .orElse(Observable.just(Optional.empty())));
    statusTree = new SearchableTree();
    File projecDirectory = repository.blockingFirst().map(IRepository::getTopLevelDirectory)
        .orElseThrow(() -> new RuntimeException("could not determine project root directory"));
    statusTreeModel = new StatusTreeModel(status.map(pOptStatus -> pOptStatus.map(IFileStatus::getUncommitted).orElse(List.of())),
                                          projecDirectory);
    statusTree.init(tableViewPanel, statusTreeModel);
    statusTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil));
    pQuickSearchProvider.attach(tableViewPanel, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(statusTree));
    tableViewPanel.add(new JScrollPane(statusTree), BorderLayout.CENTER);
    statusTree.addMouseListener(new _DoubleClickListener());
    ObservableTreeSelectionModel observableTreeSelectionModel = new ObservableTreeSelectionModel(statusTree.getSelectionModel());
    statusTree.setSelectionModel(observableTreeSelectionModel);
    selectionObservable = Observable.combineLatest(observableTreeSelectionModel.getSelectedPaths(), status, (pSelected, pStatus) -> {
      if (pSelected == null)
        return Optional.of(Collections.emptyList());
      return Optional.of(Arrays.stream(pSelected)
                             .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo().getMembers())
                             .flatMap(Collection::stream)
                             .collect(Collectors.toList()));
    });
    openFileAction = actionProvider.getOpenFileAction(selectionObservable);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    _initActions();
    add(tableViewPanel, BorderLayout.CENTER);
  }

  private void _initActions()
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
    JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getRefreshStatusAction(repository, statusTreeModel::reload));
    toolBar.addSeparator();
    toolBar.add(commitAction);
    toolBar.add(revertWorkDirAction);
    toolBar.addSeparator();
    toolBar.add(diffToHeadAction);
    toolBar.add(showCommitsForFileAction);
    tableViewPanel.add(toolBar, BorderLayout.WEST);

    popupMenu = new JPopupMenu();
    popupMenu.add(actionProvider.getOpenFileAction(selectionObservable));
    popupMenu.addSeparator();
    popupMenu.add(commitAction);
    popupMenu.add(actionProvider.getIgnoreAction(repository, selectionObservable));
    popupMenu.add(actionProvider.getExcludeAction(repository, selectionObservable));
    popupMenu.add(revertWorkDirAction);
    popupMenu.addSeparator();
    popupMenu.add(diffToHeadAction);
    popupMenu.add(showCommitsForFileAction);
    popupMenu.addSeparator();
    popupMenu.add(actionProvider.getResolveConflictsAction(repository, selectionObservable));
    statusTree.addMouseListener(new PopupMouseListener(popupMenu));
  }

  @Override
  public void discard()
  {
    statusTreeModel.discard();
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
        if (source.isPathSelected(sourcePath) && statusTree.getModel().isLeaf(sourcePath.getLastPathComponent()))
          openFileAction.actionPerformed(null);
      }
    }
  }

}
