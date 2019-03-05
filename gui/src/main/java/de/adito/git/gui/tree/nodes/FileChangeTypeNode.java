package de.adito.git.gui.tree.nodes;

import de.adito.git.api.data.IFileChangeType;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a directory with the information which of the changed files are parts of the directory being in the FileChangeTypeNodeInfo, which is
 * stored as the userObject of the DefaultMutableTreeNode this class is based on
 *
 * @author m.kaspera, 25.02.2019
 */
public class FileChangeTypeNode extends DefaultMutableTreeNode implements ICollapseAbleTreeNode
{

  public FileChangeTypeNode(FileChangeTypeNodeInfo pUserObject)
  {
    super(pUserObject);
  }

  /**
   * @return FileChangeTypeNodeInfo stored in the userObject
   */
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
   * Updates this node and all the children with the new list of IFileChangeTypes, by checking if this node is affected in any way and then calling
   * the updateNode method on all its children.
   * Checks for new entries, deleted entries, potentially collapses (or expands) the tree if necessary
   *
   * @param pMembers new list of members, can be the same as before or have more/less entries
   * @param pModel   TreeModel so the model can be notified if this node changed
   */
  public void updateNode(List<IFileChangeType> pMembers, DefaultTreeModel pModel)
  {
    getInfo().setMembers(pMembers);
    File[] childFiles = getInfo().getNodeFile().listFiles();
    List<File> existingChildFiles = new ArrayList<>();
    children.forEach(pNode -> existingChildFiles.add(((FileChangeTypeNode) pNode).getInfo().getNodeFile()));
    if (childFiles != null)
    {
      for (File childFile : childFiles)
      {
        List<IFileChangeType> childMembers = _getChildMembers(pMembers, childFile);
        Optional<File> matchingFileOpt = existingChildFiles
            .stream()
            .filter(pExistingChildFile -> _isChildMember(pExistingChildFile, childFile)).findFirst();
        FileChangeTypeNode childNode = _getChildNodeForFile(this, childFile);
        if (childNode != null)
        {
          if (matchingFileOpt.isPresent())
          {
            if (_getChildMembers(pMembers, matchingFileOpt.get()).equals(childMembers))
            {
              if (childMembers.isEmpty())
              {
                remove(childNode);
                pModel.reload(this);
              }
              else if (childFile.isDirectory())
                childNode.updateNode(childMembers, pModel);
            }
            else
            {
              int indexNow = getIndex(childNode);
              remove(childNode);
              FileChangeTypeNode treeNode = new FileChangeTypeNode(new FileChangeTypeNodeInfo(childFile.getName(), childFile, childMembers));
              insert(treeNode, indexNow);
              calculateChildren(treeNode, childMembers, childFile);
              pModel.reload(this);
            }
          }
        }
        else if (!childMembers.isEmpty())
        {
          FileChangeTypeNode treeNode = new FileChangeTypeNode(new FileChangeTypeNodeInfo(childFile.getName(), childFile, childMembers));
          insert(treeNode, 0);
          calculateChildren(treeNode, childMembers, childFile);
          pModel.reload(this);
        }
      }
    }
  }

  @Override
  public boolean isCollapseAble()
  {
    return parent != null
        && children != null
        && children.size() == 1
        && getInfo().getNodeFile().isDirectory()
        && ((FileChangeTypeNode) children.get(0)).getInfo().getNodeFile().isDirectory();
  }

  @Override
  public void tryCollapse(DefaultTreeModel pModel)
  {
    if (isCollapseAble())
    {
      _doCollapse(this);
      pModel.reload(this);
    }
    if (!isLeaf())
    {
      for (TreeNode treeNode : children)
      {
        ((FileChangeTypeNode) treeNode).tryCollapse(pModel);
      }
    }
  }

  /**
   * creates a tree with pThis as root by recursively creating the children
   *
   * @param pThis     FileChangeTypeNode for which to calculate the children
   * @param pMembers  IFileChangeTypes whose files are children of the parent of pThis
   * @param pNodeFile File that pThis represents
   */
  public static void calculateChildren(FileChangeTypeNode pThis, List<IFileChangeType> pMembers, File pNodeFile)
  {
    File[] subDirs = pNodeFile.listFiles();
    if (subDirs != null)
    {
      for (File childFile : subDirs)
      {
        List<IFileChangeType> childMembers = _getChildMembers(pMembers, childFile);
        if (!childMembers.isEmpty())
        {
          FileChangeTypeNode treeNode = new FileChangeTypeNode(new FileChangeTypeNodeInfo(childFile.getName(), childFile, childMembers));
          pThis.insert(treeNode, pThis.getChildCount());
          calculateChildren(treeNode, childMembers, childFile);
        }
      }
      if (pThis.isCollapseAble())
      {
        _doCollapse(pThis);
      }
    }
  }

  /**
   * @param pNode FileChangeTypeNode that should be collapsed. This will remove all children of pNode and re-calculate them
   */
  private static void _doCollapse(FileChangeTypeNode pNode)
  {
    pNode.getInfo().collapse(((FileChangeTypeNode) pNode.getChildAt(0)).getInfo());
    pNode.removeAllChildren();
    calculateChildren(pNode, pNode.getInfo().getMembers(), pNode.getInfo().getNodeFile());
  }

  /**
   * @param pMembers     potential candidates for IFileChangeTypes whose file is located in the childFolder or any of its subdirectories
   * @param pChildFolder File for which the members should be calculated from the list of potential members
   * @return List of IFileChangeTypes whose file is a child of pChildFolder
   */
  private static List<IFileChangeType> _getChildMembers(List<IFileChangeType> pMembers, File pChildFolder)
  {
    List<IFileChangeType> childMembers = new ArrayList<>();
    for (IFileChangeType member : pMembers)
    {
      if (_isChildMember(member.getFile(), pChildFolder))
        childMembers.add(member);
    }
    return childMembers;
  }

  /**
   * Calculates if pPotentialMember is a child of pFolder by going up the directory tree from pPotentialMember and checking if pFolder
   * is equal to the current directory
   *
   * @param pPotentialMember File to check if it is a child of pFolder
   * @param pFolder          File that potentially contains pPotentialMember
   * @return whether or not pPotentialMember is a child of pFolder
   */
  private static boolean _isChildMember(File pPotentialMember, File pFolder)
  {
    if (pPotentialMember == null || !pPotentialMember.exists() || pFolder == null)
      return false;
    else if (pPotentialMember.equals(pFolder))
    {
      return true;
    }
    else
    {
      File parentFile = pPotentialMember.getParentFile();
      while (parentFile != null && parentFile.exists())
      {
        if (parentFile.equals(pFolder))
          return true;
        parentFile = parentFile.getParentFile();
      }
      return false;
    }
  }

  /**
   * @param pNode Node for which to find the suiting childNode
   * @param pFile the file that the childNode to find should be for
   * @return FileChangeTypeNode that is a child of pNode and has the file in the userObject, null if no matching node was found
   */
  @Nullable
  private static FileChangeTypeNode _getChildNodeForFile(FileChangeTypeNode pNode, File pFile)
  {
    for (TreeNode childNode : pNode.children)
    {
      if (_isChildMember(((FileChangeTypeNode) childNode).getInfo().getNodeFile(), pFile))
      {
        return (FileChangeTypeNode) childNode;
      }
    }
    return null;
  }
}
