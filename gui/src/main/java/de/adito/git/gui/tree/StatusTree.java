package de.adito.git.gui.tree;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IQuickSearchProvider;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.models.BaseObservingTreeModel;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Does the setup for a Tree with the StatusTreeModel with Quicksearch supprt and provides an observable of the selected items
 *
 * @author m.kaspera, 11.07.2019
 */
public class StatusTree
{

  private final SearchableTree searchableTree;
  private final Observable<Optional<List<IFileChangeType>>> selectionObservable;

  public StatusTree(@NotNull IQuickSearchProvider pQuickSearchProvider, @NotNull IFileSystemUtil pFileSystemUtil,
                    @NotNull BaseObservingTreeModel pTreeModel, boolean pUseFlatCellRenderer, @NotNull File pProjectDirectory, @NotNull JPanel pTreeViewPanel)
  {
    searchableTree = new SearchableTree();
    searchableTree.init(pTreeViewPanel, pTreeModel);
    if (pUseFlatCellRenderer)
      searchableTree.setCellRenderer(new FileChangeTypeFlatTreeCellRenderer(pFileSystemUtil, pProjectDirectory));
    else
      searchableTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil, pProjectDirectory));
    pQuickSearchProvider.attach(pTreeViewPanel, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(searchableTree));
    pTreeViewPanel.add(new JScrollPane(searchableTree), BorderLayout.CENTER);
    ObservableTreeSelectionModel observableTreeSelectionModel = new ObservableTreeSelectionModel(searchableTree.getSelectionModel());
    searchableTree.setSelectionModel(observableTreeSelectionModel);
    selectionObservable = observableTreeSelectionModel.getSelectedPaths().map(pSelected -> {
      if (pSelected == null)
        return Optional.of(Collections.emptyList());
      return Optional.of(Arrays.stream(pSelected)
                             .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo().getMembers())
                             .flatMap(Collection::stream)
                             .collect(Collectors.toList()));
    });
  }

  /**
   * @return The actual JTree/SearchableTree that this class initiates
   */
  @NotNull
  public SearchableTree getTree()
  {
    return searchableTree;
  }

  /**
   * @return Observable with the selected FileChangeTypes (if a non-leaf is selected, all leaves of the sub-tree with the selected node as root are returned)
   */
  @NotNull
  public Observable<Optional<List<IFileChangeType>>> getSelectionObservable()
  {
    return selectionObservable;
  }

}
