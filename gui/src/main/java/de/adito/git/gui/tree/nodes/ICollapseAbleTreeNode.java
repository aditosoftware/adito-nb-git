package de.adito.git.gui.tree.nodes;

import javax.swing.tree.DefaultTreeModel;

/**
 * Interface defining the methods for a node that can be collapsed (meaning it is a node that has only one non-leaf child and the two nodes, or
 * possibly several next nodes, can be made into one node)
 *
 * @author m.kaspera, 19.02.2019
 */
public interface ICollapseAbleTreeNode
{

  /**
   * check if this node can be collapsed (i.e. has only one non-leaf child)
   *
   * @return whether this node can be collapsed
   */
  boolean isCollapseAble();

  /**
   * check if the node is collapseAble and, if that is the case, merge this node with it's only non-leaf child. Call should also collapse all children
   * child's children and so forth if they are collapseAble
   *
   * @param pModel TreeModel of the tree the node is a part of, used to tell the model that this node changed
   */
  void tryCollapse(DefaultTreeModel pModel);

}
