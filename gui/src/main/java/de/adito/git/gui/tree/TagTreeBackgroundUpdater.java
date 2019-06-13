package de.adito.git.gui.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author m.kaspera, 12.06.2019
 */
public class TagTreeBackgroundUpdater extends SwingWorker<List<TreeUpdate>, TreeUpdate>
{

  private final DefaultTreeModel treeModel;
  private final List<Path> members;
  private final Action[] performAfter;

  public TagTreeBackgroundUpdater(DefaultTreeModel pTreeModel, List<Path> pMembers, Action... pPerformAfter)
  {
    super();
    treeModel = pTreeModel;
    members = pMembers;
    performAfter = pPerformAfter;
  }

  @Override
  @NotNull
  protected List<TreeUpdate> doInBackground()
  {
    return _updateTree((DefaultMutableTreeNode) treeModel.getRoot(), treeModel, Paths.get(""), members);
  }

  @Override
  protected void done()
  {
    if (isCancelled())
      return;
    List<TreeUpdate> treeUpdates;
    try
    {
      treeUpdates = get();
    }
    catch (InterruptedException | ExecutionException pE)
    {
      Thread.currentThread().interrupt();
      throw new RuntimeException(pE);
    }
    for (TreeUpdate update : treeUpdates)
    {
      if (update.getType() == TreeUpdate.TYPE.INSERT)
      {
        treeModel.insertNodeInto(update.getNode(), update.getParent(), update.getIndex());
      }
      else
      {
        treeModel.removeNodeFromParent(update.getNode());
      }
    }
    if (isCancelled())
      return;
    for (Action action : performAfter)
    {
      action.actionPerformed(null);
    }
  }

  /**
   * update the tree, either with initial values or changed values. Does not change nodes if it does not have to be
   *
   * @param pParent    parent node, can be null (e.g. no root yet)
   * @param pTreeModel treeModel used to insert/update/remove nodes
   * @param pPath      path to the current node (empty if root)
   * @param pMembers   paths leading to this node, can also contain the path to the node itself
   */
  @NotNull
  private List<TreeUpdate> _updateTree(@Nullable DefaultMutableTreeNode pParent, @NotNull DefaultTreeModel pTreeModel, @NotNull Path pPath, @NotNull List<Path> pMembers)
  {
    List<TreeUpdate> updates = new ArrayList<>();
    if (pParent != null)
    {
      List<Path> childPaths = new ArrayList<>();
      for (int childIndex = pParent.getChildCount() - 1; childIndex >= 0; childIndex--)
      {
        Path childPath = TreeUtil._pathFromTreePath(new TreePath(pTreeModel.getPathToRoot(pParent.getChildAt(childIndex))));
        if (pMembers.stream().noneMatch(pMemPath -> pMemPath.startsWith(childPath)))
        {
          updates.add(TreeUpdate.createRemove((MutableTreeNode) pParent.getChildAt(childIndex)));
        }
        else
        {
          childPaths.add(childPath);
          List<Path> childMembers = new ArrayList<>();
          // pMembers will have the matching childMembers removed. At first this probably seems strange, since we'd probably want them in the for loop down below.
          // However, the removed childMembers were already matched to a node here, so we do no longer have to check them below (it's actually an advantage,
          // less work below)
          _getChildMembers(pPath, pMembers, childMembers);
          updates.addAll(_updateTree((DefaultMutableTreeNode) pParent.getChildAt(childIndex), pTreeModel, childPath, childMembers));
        }
      }
      for (Path member : pMembers)
      {
        if (childPaths.stream().noneMatch(member::startsWith) && !member.equals(pPath))
        {
          List<Path> childrenMembers = new ArrayList<>();
          Path childPath = _getChildMembers(pPath, pMembers, childrenMembers);
          updates.addAll(_createTreeNodes(pParent, pTreeModel, childPath, pMembers));
        }
      }
    }
    else
    {
      updates.addAll(_createTreeNodes(null, pTreeModel, pPath, pMembers));
    }
    return updates;
  }

  /**
   * Inserts nodes into the tree
   *
   * @param pParent    parent node, can be null
   * @param pTreeModel treeModel to call for inserting the nodes
   * @param pPath      path to the current node
   * @param pMembers   paths that are children of the current node (or the current path itself)
   * @return list of updates perform on the treeModel
   */
  @NotNull
  private List<TreeUpdate> _createTreeNodes(@Nullable DefaultMutableTreeNode pParent, @NotNull DefaultTreeModel pTreeModel, @NotNull Path pPath,
                                            @NotNull List<Path> pMembers)
  {
    List<TreeUpdate> updates = new ArrayList<>();
    DefaultMutableTreeNode newNode;
    if (pParent == null)
    {
      newNode = new DefaultMutableTreeNode("Tags");
      pTreeModel.setRoot(newNode);
    }
    else
    {
      newNode = new DefaultMutableTreeNode(pPath.getFileName());
      updates.add(TreeUpdate.createInsert(newNode, pParent, 0));
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
        updates.addAll(_createTreeNodes(newNode, pTreeModel, childPath, childrenMembers));
      }
    }
    return updates;
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
  @NotNull
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
}
