package de.adito.git.gui.tree;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IQuickSearchProvider;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.models.BaseObservingTreeModel;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import de.adito.util.reactive.cache.ObservableCache;
import de.adito.util.reactive.cache.ObservableCacheDisposable;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.BorderLayout;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Does the setup for a Tree with the StatusTreeModel with Quicksearch supprt and provides an observable of the selected items
 *
 * @author m.kaspera, 11.07.2019
 */
public class StatusTree implements IDiscardable
{

  private final SearchableTree searchableTree;
  private final ObservableTreeSelectionModel observableTreeSelectionModel;
  private final ObservableCache observableCache = new ObservableCache();

  public StatusTree(@NonNull IQuickSearchProvider pQuickSearchProvider, @NonNull IFileSystemUtil pFileSystemUtil, @NonNull BaseObservingTreeModel<?> pTreeModel,
                    boolean pUseFlatCellRenderer, @NonNull File pProjectDirectory, @NonNull JComponent pTreeViewPanel, @Nullable JScrollPane pScrollPane)
  {
    searchableTree = new SearchableTree();
    searchableTree.init(pTreeViewPanel, pTreeModel);
    if (pUseFlatCellRenderer)
      searchableTree.setCellRenderer(new FileChangeTypeFlatTreeCellRenderer(pFileSystemUtil, pProjectDirectory));
    else
      searchableTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil, pProjectDirectory));
    pQuickSearchProvider.attach(pTreeViewPanel, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(searchableTree));
    if (pScrollPane != null)
      pScrollPane.setViewportView(searchableTree);
    observableTreeSelectionModel = new ObservableTreeSelectionModel(searchableTree.getSelectionModel());
    searchableTree.setSelectionModel(observableTreeSelectionModel);
  }

  /**
   * @return The actual JTree/SearchableTree that this class initiates
   */
  @NonNull
  public SearchableTree getTree()
  {
    return searchableTree;
  }

  /**
   * @return Observable with the selected FileChangeTypes (if a non-leaf is selected, all leaves of the sub-tree with the selected node as root are returned)
   */
  @NonNull
  public Observable<Optional<List<IFileChangeType>>> getSelectionObservable()
  {
    return observableCache.calculateParallel("selection", () -> observableTreeSelectionModel.getSelectedPaths()
        .map(pSelected -> {
          if (pSelected == null)
            return Optional.of(Collections.emptyList());
          return Optional.of(Arrays.stream(pSelected)
                                 .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo())
                                 .filter(Objects::nonNull)
                                 .map(FileChangeTypeNodeInfo::getMembers)
                                 .flatMap(Collection::stream)
                                 .distinct()
                                 .collect(Collectors.toList()));
        }));
  }

  @Override
  public void discard()
  {
    new ObservableCacheDisposable(observableCache).dispose();
    observableTreeSelectionModel.discard();
    searchableTree.discard();
    TreeCellRenderer cellRenderer = searchableTree.getCellRenderer();
    if (cellRenderer instanceof IDiscardable)
      ((IDiscardable) cellRenderer).discard();
  }
}
