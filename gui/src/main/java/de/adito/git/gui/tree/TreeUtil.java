package de.adito.git.gui.tree;

import lombok.NonNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for methods commonly used with trees
 *
 * @author m.kaspera, 06.06.2019
 */
public class TreeUtil
{

  /**
   * Expands the nodes of the tree, but checks if the thread was interrupted and breaks the loop if it was interrupted
   *
   * @param pTree JTree to be expanded
   */
  public static void expandTreeInterruptible(JTree pTree)
  {
    for (int i = 0; i < pTree.getRowCount(); i++)
    {
      if (Thread.currentThread().isInterrupted())
        break;
      pTree.expandRow(i);
    }
  }

  /**
   * Backwards extracts the full name of a tag from the TreePath its leaf node has
   *
   * @param pTreePath TreePath of the selected node
   * @return full name of the tag of the selected node
   */
  @NonNull
  public static Path pathFromTreePath(@NonNull TreePath pTreePath)
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

}
