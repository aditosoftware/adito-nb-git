package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IQuickSearchProvider;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.ITag;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.renderer.TagTreeCellRenderer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dialog that shows an overview of all tags in a repository
 *
 * @author m.kaspera, 06.06.2019
 */
class TagOverviewDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private final Map<Path, ITag> pathToTagMapping = new HashMap<>();
  private final SearchableTree tree;
  private final Consumer<ICommit> selectedCommitCallback;
  private final Observable<Optional<IRepository>> repository;
  private final Disposable tagDisposable;
  private ObservableTreeSelectionModel selectionModel;
  private _SelectCommitAction selectCommitAction;

  @Inject
  TagOverviewDialog(IActionProvider pActionProvider, IIconLoader pIconLoader, IQuickSearchProvider pQuickSearchProvider,
                    @Assisted Consumer<ICommit> pSelectedCommitCallback, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    selectedCommitCallback = pSelectedCommitCallback;
    repository = pRepository;
    setLayout(new BorderLayout());
    tree = new SearchableTree();
    DefaultTreeModel treeModel = _initTree(pIconLoader, pQuickSearchProvider);
    Observable<List<ITag>> tagList = pRepository.blockingFirst().map(IRepository::getTags).orElse(Observable.just(List.of()));
    tagDisposable = tagList.subscribe(pTagList -> {
      pathToTagMapping.clear();
      pTagList.forEach(pTag -> pathToTagMapping.put(Paths.get(pTag.getName()), pTag));
      _updateTree((DefaultMutableTreeNode) treeModel.getRoot(), treeModel, Paths.get(""),
                  pTagList.stream().map(pTag -> Paths.get(pTag.getName())).collect(Collectors.toList()));
    });
    Observable<Optional<ITag>> selectedTagObservable = selectionModel.getSelectedPaths().map(pSelectedPaths -> {
      if (pSelectedPaths != null && pSelectedPaths.length == 1)
        return Optional.of(pathToTagMapping.get(_pathFromTreePath(pSelectedPaths[0])));
      else return Optional.empty();
    });
    // expand the tree initially
    pActionProvider.getExpandTreeAction(tree).actionPerformed(null);
    PopupMouseListener popupMouseListener = _getPopupMouseListener(pActionProvider, pRepository, selectionModel, selectedTagObservable);
    tree.addMouseListener(popupMouseListener);
  }

  /**
   * @param pIconLoader          IconLoader for the TreeCellRenderer to load icons
   * @param pQuickSearchProvider QuickSearchProvider to attach to a tree/table for QuickSearch support
   * @return TreeModel for the Tree used in this dialog
   */
  @NotNull
  private DefaultTreeModel _initTree(IIconLoader pIconLoader, IQuickSearchProvider pQuickSearchProvider)
  {
    DefaultTreeModel treeModel = new DefaultTreeModel(null);
    tree.init(this, treeModel);
    selectionModel = new ObservableTreeSelectionModel(tree.getSelectionModel());
    tree.setSelectionModel(selectionModel);
    tree.setCellRenderer(new TagTreeCellRenderer(pIconLoader));
    add(new JScrollPane(tree), BorderLayout.CENTER);
    pQuickSearchProvider.attach(this, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(tree));
    return treeModel;
  }

  /**
   * @param pActionProvider        injected IActionProvider
   * @param pRepository            IRepository of the repository for the project the user has selected
   * @param pSelectionModel        the SelectionModel of the Tree
   * @param pSelectedTagObservable Observable with the currently selected Tag
   * @return PopupMouseListener that shows a popup for the selected item
   */
  @NotNull
  private PopupMouseListener _getPopupMouseListener(@NotNull IActionProvider pActionProvider, @NotNull Observable<Optional<IRepository>> pRepository,
                                                    @NotNull ObservableTreeSelectionModel pSelectionModel, @NotNull Observable<Optional<ITag>> pSelectedTagObservable)
  {
    JPopupMenu popupMenu = new JPopupMenu();
    selectCommitAction = new _SelectCommitAction(pSelectionModel);
    popupMenu.add(selectCommitAction);
    popupMenu.add(pActionProvider.getDeleteTagAction(pRepository, pSelectedTagObservable));
    PopupMouseListener popupMouseListener = new PopupMouseListener(popupMenu);
    popupMouseListener.setDoubleClickAction(selectCommitAction);
    return popupMouseListener;
  }

  /**
   * update the tree, either with initial values or changed values. Does not change nodes if it does not have to be
   *
   * @param pParent    parent node, can be null (e.g. no root yet)
   * @param pTreeModel treeModel used to insert/update/remove nodes
   * @param pPath      path to the current node (empty if root)
   * @param pMembers   paths leading to this node, can also contain the path to the node itself
   */
  private void _updateTree(@Nullable DefaultMutableTreeNode pParent, @NotNull DefaultTreeModel pTreeModel, @NotNull Path pPath, @NotNull List<Path> pMembers)
  {
    if (pParent != null)
    {
      List<Path> childPaths = new ArrayList<>();
      for (int childIndex = pParent.getChildCount() - 1; childIndex >= 0; childIndex--)
      {
        Path childPath = _pathFromTreePath(new TreePath(pTreeModel.getPathToRoot(pParent.getChildAt(childIndex))));
        if (pMembers.stream().noneMatch(pMemPath -> pMemPath.startsWith(childPath)))
        {
          pTreeModel.removeNodeFromParent((MutableTreeNode) pParent.getChildAt(childIndex));
        }
        else
        {
          childPaths.add(childPath);
          List<Path> childMembers = new ArrayList<>();
          // pMembers will have the matching childMembers removed. At first this probably seems strange, since we'd probably want them in the for loop down below.
          // However, the removed childMembers were already matched to a node here, so we do no longer have to check them below (it's actually an advantage,
          // less work below)
          _getChildMembers(pPath, pMembers, childMembers);
          _updateTree((DefaultMutableTreeNode) pParent.getChildAt(childIndex), pTreeModel, childPath, childMembers);
        }
      }
      for (Path member : pMembers)
      {
        if (childPaths.stream().noneMatch(member::startsWith) && !member.equals(pPath))
        {
          List<Path> childrenMembers = new ArrayList<>();
          Path childPath = _getChildMembers(pPath, pMembers, childrenMembers);
          _createTreeNodes(pParent, pTreeModel, childPath, pMembers);
        }
      }
    }
    else
    {
      _createTreeNodes(null, pTreeModel, pPath, pMembers);
    }
  }

  /**
   * Inserts nodes into the tree
   *
   * @param pParent    parent node, can be null
   * @param pTreeModel treeModel to call for inserting the nodes
   * @param pPath      path to the current node
   * @param pMembers   paths that are children of the current node (or the current path itself)
   */
  private void _createTreeNodes(@Nullable DefaultMutableTreeNode pParent, @NotNull DefaultTreeModel pTreeModel, @NotNull Path pPath, @NotNull List<Path> pMembers)
  {
    DefaultMutableTreeNode newNode;
    if (pParent == null)
    {
      newNode = new DefaultMutableTreeNode("Tags");
      pTreeModel.setRoot(newNode);
    }
    else
    {
      newNode = new DefaultMutableTreeNode(pPath.getFileName());
      pTreeModel.insertNodeInto(newNode, pParent, 0);
    }
    while (!pMembers.isEmpty())
    {
      if (pMembers.get(0).equals(pPath))
      {
        pMembers.remove(0);
      }
      else
      {
        List<Path> childrenMembers = new ArrayList<>();
        Path childPath = _getChildMembers(pPath, pMembers, childrenMembers);
        _createTreeNodes(newNode, pTreeModel, childPath, childrenMembers);
      }
    }
  }

  /**
   * Retrieve all members of the path belonging to the childNode that will be formed by the first element in pMembers
   *
   * @param pPath            path to the current Node
   * @param pMembers         List with all valid paths for the current Node. The list will be changed in this method (all members that get inserted into
   *                         pChildrenMembers are removed)
   * @param pChildrenMembers list to fill with the members of the childNode
   * @return the new path to the childNode
   */
  private Path _getChildMembers(@NotNull Path pPath, @NotNull List<Path> pMembers, @NotNull List<Path> pChildrenMembers)
  {
    Path childPath = pMembers.get(0).subpath(0, pPath.getFileName().toString().isEmpty() ? pPath.getNameCount() : pPath.getNameCount() + 1);
    for (int index = pMembers.size() - 1; index >= 0; index--)
    {
      if (pMembers.get(index).startsWith(childPath))
      {
        pChildrenMembers.add(pMembers.get(index));
        pMembers.remove(index);
      }
    }
    return childPath;
  }

  /**
   * Backwards extracts the full name of a tag from the TreePath its leaf node has
   *
   * @param pTreePath TreePath of the selected node
   * @return full name of the tag of the selected node
   */
  private Path _pathFromTreePath(TreePath pTreePath)
  {
    // PathCount == 1: only root, the root label does not factor into the tag path
    if (pTreePath.getPathCount() > 1)
    {
      String[] pathComponents = new String[pTreePath.getPathCount() - 2];
      // index starts at 2 because the first node is the root node, which does not belong in the tag path, and the second component is passed seperately
      for (int index = 2; index < pTreePath.getPathCount(); index++)
      {
        // the component retrieved from the treePath is offset by 2 because the first real path element is passed seperately in Paths.get, and root is moot ;)
        pathComponents[index - 2] = pTreePath.getPathComponent(index).toString();
      }
      return Paths.get(pTreePath.getPathComponent(1).toString(), pathComponents);
    }
    return Paths.get("");
  }

  @Override
  public @Nullable String getMessage()
  {
    return null;
  }

  @Nullable
  @Override
  public Object getInformation()
  {
    return null;
  }

  @Override
  public void discard()
  {
    selectCommitAction.discard();
    tagDisposable.dispose();
  }

  /**
   * Action that selects the commit that belongs to the currently selected tag via the selectedCommitCallback
   */
  private class _SelectCommitAction extends AbstractAction implements IDiscardable
  {

    private final ObservableTreeSelectionModel observableSelectionModel;
    private final Disposable disposable;

    _SelectCommitAction(ObservableTreeSelectionModel pObservableSelectionModel)
    {
      super("Select associated commit");
      observableSelectionModel = pObservableSelectionModel;
      disposable = observableSelectionModel.getSelectedPaths()
          .subscribe(pTreePaths -> setEnabled(pTreePaths != null && pTreePaths.length == 1 && ((DefaultMutableTreeNode) pTreePaths[0].getLastPathComponent()).isLeaf()));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      selectedCommitCallback.accept(repository.blockingFirst().map(pRepo -> {
        try
        {
          TreePath[] selectedPaths = observableSelectionModel.getSelectionPaths();
          if (selectedPaths != null && selectedPaths.length == 1)
            return pRepo.getCommit(pathToTagMapping.get(_pathFromTreePath(selectedPaths[0])).getId());
          return null;
        }
        catch (AditoGitException pE)
        {
          return null;
        }
      }).orElse(null));
    }

    @Override
    public void discard()
    {
      disposable.dispose();
    }
  }
}
