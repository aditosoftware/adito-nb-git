package de.adito.git.gui.quicksearch;

import de.adito.git.api.IQuickSearch;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;

/**
 * Implementation of the Callback of QuickSearch for Trees
 *
 * @author m.kaspera, 19.02.2019
 */
public class QuickSearchTreeCallbackImpl implements IQuickSearch.ICallback
{

  private TreePath lastFoundBuffer = null;

  private final JTree tree;
  private String searchString = null;
  private int searchResultIndex = 0;

  public QuickSearchTreeCallbackImpl(JTree pTree)
  {
    tree = pTree;
  }

  @Override
  public String findMaxPrefix(String pPrefix)
  {
    return null;
  }

  @Override
  public void quickSearchCancelled()
  {
    searchResultIndex = 0;
    searchString = null;
  }

  @Override
  public void quickSearchConfirmed()
  {
    _SearchResult searchResult = _findNthOccurrence(searchString, searchResultIndex, null, (DefaultMutableTreeNode) tree.getModel().getRoot());
    tree.scrollPathToVisible(searchResult.getValidPath());
    tree.getSelectionModel().setSelectionPath(searchResult.getValidPath());
  }

  @Override
  public void quickSearchUpdate(String pSearchText)
  {
    searchResultIndex = 0;
    searchString = pSearchText;
    _SearchResult searchResult = _findNthOccurrence(searchString, searchResultIndex, null, (DefaultMutableTreeNode) tree.getModel().getRoot());
    tree.scrollPathToVisible(searchResult.getValidPath());
    tree.getSelectionModel().setSelectionPath(searchResult.getValidPath());
  }

  @Override
  public void showNextSelection(boolean pForward)
  {
    if (pForward)
    {
      searchResultIndex++;
    }
    else
      searchResultIndex--;
    _SearchResult searchResult = _findNthOccurrence(searchString, searchResultIndex, null, (DefaultMutableTreeNode) tree.getModel().getRoot());
    tree.scrollPathToVisible(searchResult.getValidPath());
    tree.getSelectionModel().setSelectionPath(searchResult.getValidPath());
  }

  /**
   * finds the n-th node that contains the searchString in the tree, only considers leafs for search hits
   *
   * @param pSearchString the String to look for
   * @param pN            the n in "find n-th occurrence"
   * @param pPath         TreePath that describes the path from root down to pNode, inclusive the root node and exclusive the pNode
   * @param pNode         current Node
   */
  private _SearchResult _findNthOccurrence(String pSearchString, int pN, TreePath pPath, DefaultMutableTreeNode pNode)
  {
    // keeps track of how many occurrences have been found so far
    int foundOccurrences = 0;
    for (TreeNode childNode : Collections.list(pNode.children()))
    {
      TreePath childPath = pPath == null ? new TreePath(pNode) : pPath.pathByAddingChild(pNode);
      if (((FileChangeTypeNodeInfo) ((DefaultMutableTreeNode) childNode).getUserObject()).getNodeDescription().contains(pSearchString))
      {
        foundOccurrences++;
        lastFoundBuffer = childPath.pathByAddingChild(childNode);
        if (pN == 0)
          return new _SearchResult(foundOccurrences, childPath.pathByAddingChild(childNode));
        else
          pN--;
      }
      if (!childNode.isLeaf())
      {
        _SearchResult searchResult = _findNthOccurrence(pSearchString, pN, childPath, (DefaultMutableTreeNode) childNode);
        foundOccurrences += searchResult.getFoundOccurrences();
        if ((pN == 0 || pN - foundOccurrences == -1) && searchResult.getValidPath() != null)
          return new _SearchResult(foundOccurrences, searchResult.getValidPath());
        pN -= searchResult.getFoundOccurrences();
      }
    }
    if (pPath == null)
    {
      if (pN >= 0 && foundOccurrences > 0)
      {
        searchResultIndex = 0;
        return _findNthOccurrence(pSearchString, searchResultIndex, null, pNode);
      }
      if (pN < 0)
      {
        searchResultIndex = foundOccurrences - 1;
        return new _SearchResult(foundOccurrences, lastFoundBuffer);
      }
      lastFoundBuffer = null;
    }
    return new _SearchResult(foundOccurrences, null);
  }

  /**
   * Simple class to hold a TreePath and an integer indicating the number of matches found
   */
  private static class _SearchResult
  {

    private final int foundOccurrences;
    private final TreePath validPath;

    _SearchResult(int pFoundOccurrences, TreePath pValidPath)
    {
      foundOccurrences = pFoundOccurrences;
      validPath = pValidPath;
    }

    TreePath getValidPath()
    {
      return validPath;
    }

    int getFoundOccurrences()
    {
      return foundOccurrences;
    }
  }


}
