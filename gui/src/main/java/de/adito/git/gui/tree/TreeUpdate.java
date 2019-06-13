package de.adito.git.gui.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.MutableTreeNode;

/**
 * @author m.kaspera, 12.06.2019
 */
public class TreeUpdate
{

  private final TYPE type;
  private final MutableTreeNode parent;
  private final MutableTreeNode node;
  private final int index;

  /**
   * INSERT: insert the specified node under the specified parent
   * REMOVE: remove the specified node from the tree
   * ROOT: set the specified node as root of the tree
   */
  public enum TYPE
  {
    INSERT,
    REMOVE,
    ROOT
  }

  private TreeUpdate(@NotNull TYPE pType, @NotNull MutableTreeNode pNode, @Nullable MutableTreeNode pParent, int pIndex)
  {

    type = pType;
    parent = pParent;
    node = pNode;
    index = pIndex;
  }

  /**
   * @param pNode   Node that should be inserted
   * @param pParent Node who should be the parent of pNode
   * @param pIndex  index that pNode should be inserted at
   * @return TreeUpdate with the specified attributes
   */
  public static TreeUpdate createInsert(@NotNull MutableTreeNode pNode, @NotNull MutableTreeNode pParent, int pIndex)
  {
    return new TreeUpdate(TYPE.INSERT, pNode, pParent, pIndex);
  }

  /**
   * @param pNode Node that should be removed
   * @return TreeUpdate with the specified attributes
   */
  public static TreeUpdate createRemove(@NotNull MutableTreeNode pNode)
  {
    return new TreeUpdate(TYPE.REMOVE, pNode, null, -1);
  }

  /**
   * @param pNode Node that should become the new root node
   * @return TreeUpdate with the specified attributes
   */
  public static TreeUpdate createRoot(@NotNull MutableTreeNode pNode)
  {
    return new TreeUpdate(TYPE.ROOT, pNode, null, -1);
  }

  /**
   * @return TYPE of the update
   */
  @NotNull
  public TYPE getType()
  {
    return type;
  }

  /**
   * @return Node that should be the parent if TYPE is INSERT, null otherwise
   */
  public MutableTreeNode getParent()
  {
    return parent;
  }

  /**
   * @return the node that is affected by the operation
   */
  @NotNull
  public MutableTreeNode getNode()
  {
    return node;
  }

  /**
   * @return index that the node should be inserted at
   */
  public int getIndex()
  {
    return index;
  }

}
