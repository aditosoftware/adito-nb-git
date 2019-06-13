package de.adito.git.gui.tree.nodes;

import de.adito.git.api.data.ICommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;

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

  public FileChangeTypeNode(@NotNull FileChangeTypeNodeInfo pUserObject)
  {
    super(pUserObject);
  }

  public FileChangeTypeNode(@NotNull FileChangeTypeNodeInfo pUserObject, @Nullable ICommit pAssignedCommit)
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
  public void setInfo(@NotNull FileChangeTypeNodeInfo pUserObject)
  {
    setUserObject(pUserObject);
  }

  /**
   * @param pNode Node for which to find the suiting childNode
   * @param pFile the file that the childNode to find should be for
   * @return FileChangeTypeNode that is a child of pNode and has the file in the userObject, null if no matching node was found
   */
  @Nullable
  public static FileChangeTypeNode getChildNodeForFile(@NotNull FileChangeTypeNode pNode, @NotNull File pFile)
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
