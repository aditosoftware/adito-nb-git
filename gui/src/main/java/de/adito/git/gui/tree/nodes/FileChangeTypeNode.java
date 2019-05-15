package de.adito.git.gui.tree.nodes;

import de.adito.git.api.data.ICommit;
import de.adito.git.impl.util.Util;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a directory with the information which of the changed files are parts of the directory being in the FileChangeTypeNodeInfo, which is
 * stored as the userObject of the DefaultMutableTreeNode this class is based on
 *
 * @author m.kaspera, 25.02.2019
 */
public class FileChangeTypeNode extends DefaultMutableTreeNode
{

  @Nullable
  private ICommit assignedCommit;

  public FileChangeTypeNode(FileChangeTypeNodeInfo pUserObject)
  {
    super(pUserObject);
  }

  public FileChangeTypeNode(FileChangeTypeNodeInfo pUserObject, @Nullable ICommit pAssignedCommit)
  {
    super(pUserObject);
    assignedCommit = pAssignedCommit;
  }

  /**
   * @return FileChangeTypeNodeInfo stored in the userObject
   */
  @Nullable
  public FileChangeTypeNodeInfo getInfo()
  {
    return (FileChangeTypeNodeInfo) getUserObject();
  }

  /**
   * @param pUserObject FileChangeTypeNodeInfo to be stored in the userObject of the DefaultMutableTreeNode
   */
  public void setInfo(FileChangeTypeNodeInfo pUserObject)
  {
    setUserObject(pUserObject);
  }

  /**
   * sorts the children of the node by the ordering imposed by the comparator, the calls the sort function of the children
   *
   * @param pComparator Comparator deciding the order in which the children are arranged
   */
  public void sort(Comparator<TreeNode> pComparator, DefaultTreeModel pModel)
  {
    if (children != null)
    {
      if (!Util.isSorted(children, pComparator))
      {
        _sort(pComparator, pModel);
      }
      for (TreeNode node : children)
      {
        ((FileChangeTypeNode) node).sort(pComparator, pModel);
      }
    }
  }

  /**
   * @param pNode Node for which to find the suiting childNode
   * @param pFile the file that the childNode to find should be for
   * @return FileChangeTypeNode that is a child of pNode and has the file in the userObject, null if no matching node was found
   */
  @Nullable
  public static FileChangeTypeNode getChildNodeForFile(FileChangeTypeNode pNode, File pFile)
  {
    if (pNode.children != null)
    {
      for (TreeNode childNode : pNode.children)
      {
        FileChangeTypeNodeInfo childNodeInfo = ((FileChangeTypeNode) childNode).getInfo();
        if (childNodeInfo != null && pFile.equals(childNodeInfo.getNodeFile()))
        {
          return (FileChangeTypeNode) childNode;
        }
      }
    }
    return null;
  }

  /**
   * sort the children of this node only
   *
   * @param pComparator Comparator that determines the order of the children
   * @param pModel      TreeModel of the tree this node belongs to, to remove and insert the childNodes so that the specific events are triggered and
   *                    the ui is updated
   */
  private void _sort(Comparator<TreeNode> pComparator, DefaultTreeModel pModel)
  {
    List<TreeNode> childrenCopy = new ArrayList<>(children);
    childrenCopy.sort(pComparator);
    for (int index = 0; index < childrenCopy.size(); index++)
    {
      pModel.removeNodeFromParent((FileChangeTypeNode) childrenCopy.get(index));
      pModel.insertNodeInto((FileChangeTypeNode) childrenCopy.get(index), this, index);
    }
  }

  @Nullable
  public ICommit getAssignedCommit()
  {
    return assignedCommit;
  }

  public void setAssignedCommit(@Nullable ICommit pAssignedCommit)
  {
    assignedCommit = pAssignedCommit;
  }
}
