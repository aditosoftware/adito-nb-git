package de.adito.git.gui.tree.models;

import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.exception.InterruptedRuntimeException;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * @author m.kaspera, 14.05.2019
 */
abstract class ObservingTreeModel<T> extends BaseObservingTreeModel<T>
{

  ObservingTreeModel(@NonNull File pProjectDirectory)
  {
    super(pProjectDirectory);
  }

  /**
   * calculates a HashMap with an inner HashMap from the list of FileChanges, the HashMap works similar to a tree: the file of the outer HashMap
   * is the parent, while the files from the inner HashMap are the children connected to that parent
   *
   * @param pList List of IFileChangeTypes
   * @return HashMap calculated from the list
   */
  @NonNull
  HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _calculateMap(@NonNull List<IFileChangeType> pList)
  {
    HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> groups = new HashMap<>();
    for (IFileChangeType changeType : pList)
    {
      if (Thread.currentThread().isInterrupted())
        throw new InterruptedRuntimeException();
      File file = changeType.getFile();
      while (file != null && !file.equals(projectDirectory.getParentFile()))
      {
        File parentFile = file.getParentFile();
        if (groups.containsKey(parentFile))
        {
          if (groups.get(parentFile).containsKey(file))
          {
            groups.get(parentFile).get(file).getMembers().add(changeType);
          }
          else
          {
            List<IFileChangeType> changeTypes = new ArrayList<>();
            changeTypes.add(changeType);
            groups.get(parentFile).put(file, new FileChangeTypeNodeInfo(file.getName(), file, changeTypes));
          }
        }
        else
        {
          HashMap<File, FileChangeTypeNodeInfo> innerMap = new HashMap<>();
          List<IFileChangeType> changeTypes = new ArrayList<>();
          changeTypes.add(changeType);
          innerMap.put(file, new FileChangeTypeNodeInfo(file.getName(), file, changeTypes));
          groups.put(parentFile, innerMap);
        }
        file = file.getParentFile();
      }
    }
    return groups;
  }

  /**
   * Removes all "redundant" nodes that only have one child by basically aggregating all "folder with only one subfolder"
   *
   * @param pMap          Map retrieved by calling _calculateMap with the fileChanges
   * @param pChangedFiles HashSet of all changed files, used to determine if a File is a file or directory
   * @param pStart        startNode
   * @return HashMap that has the nodes with only one node as child collapsed to the parentNode
   */
  @NonNull
  HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> _reduce(@NonNull HashMap<File, HashMap<File, FileChangeTypeNodeInfo>> pMap, @NonNull HashSet<File> pChangedFiles,
                                                               @NonNull File pStart)
  {
    //Set<File> iterableCopy = pMap.get(pStart) == null ? new HashSet<>() : new HashSet<>(pMap.get(pStart).keySet());
    //for (File pChildFile : iterableCopy)
    //{
    //  if (Thread.currentThread().isInterrupted())
    //    throw new InterruptedRuntimeException();
    //  if (pMap.containsKey(pChildFile) && pMap.get(pChildFile).keySet().size() == 1 && pChildFile.isDirectory() &&
    //      !pChangedFiles.contains(pMap.get(pChildFile).keySet().iterator().next()) && !pChildFile.equals(projectDirectory))
    //  {
    //    Map.Entry<File, FileChangeTypeNodeInfo> theSingleEntry = pMap.get(pChildFile).entrySet().iterator().next();
    //    File firstAvailableParent = _getFirstAvailableParent(pMap, pChildFile);
    //    if (firstAvailableParent != null)
    //    {
    //      pMap.get(firstAvailableParent).get(pChildFile).collapse(theSingleEntry.getValue());
    //      pMap.remove(pChildFile);
    //      FileChangeTypeNodeInfo removed = pMap.get(firstAvailableParent).remove(pChildFile);
    //      pMap.get(firstAvailableParent).put(theSingleEntry.getKey(), removed);
    //    }
    //    pChildFile = pStart;
    //  }
    //  if (pChildFile.isDirectory() && pMap.containsKey(pChildFile))
    //    pMap = _reduce(pMap, pChangedFiles, pChildFile);
    //}
    return pMap;
  }

  /**
   * @param pMap  Map containing
   * @param pFile file for which the nearest parent in the map should be found
   * @return the next parent file that is contained in the map
   */
  @Nullable
  private File _getFirstAvailableParent(@NonNull HashMap<File, ?> pMap, @NonNull File pFile)
  {
    File parent = pFile.getParentFile();
    while (pMap.get(parent) == null)
    {
      parent = parent.getParentFile();
      if (parent == null)
        return null;
    }
    return parent;
  }

}
