package de.adito.git.gui.tree.nodes;

import de.adito.git.api.data.diff.IFileChangeType;
import lombok.NonNull;

import java.io.File;
import java.util.List;

/**
 * Contains information about which IFileChangeTypes are part of the folder the node if for(pNodeFile)
 *
 * @author m.kaspera, 22.02.2019
 */
public class FileChangeTypeNodeInfo
{

  private List<IFileChangeType> members;
  private File nodeFile;
  private String nodeDescription;

  /**
   * @param pNodeDescription String with the name of the nodeFile, can also contain the name of several nodeFiles if the nodes were collapsed
   * @param pNodeFile        Folder or file that the node represents
   * @param pMembers         List of IFileChangeTypes that are children of pNodeFile
   */
  public FileChangeTypeNodeInfo(@NonNull String pNodeDescription, @NonNull File pNodeFile, @NonNull List<IFileChangeType> pMembers)
  {
    nodeDescription = pNodeDescription;
    nodeFile = pNodeFile;
    members = pMembers;
  }

  @NonNull
  public File getNodeFile()
  {
    return nodeFile;
  }

  @NonNull
  public List<IFileChangeType> getMembers()
  {
    return members;
  }

  @NonNull
  public String getNodeDescription()
  {
    return nodeDescription;
  }

  /**
   * appends the nodeDescription of pOtherNodeInfo and a "/" to this nodeDescription and sets the nodeFile to the nodeFile of pOtherNodeInfo
   *
   * @param pOtherNodeInfo FileChangeTypeNodeInfo of the node that should be merged with the current one, called from the node with the parent file
   *                       on the node that contains the child
   */
  public void collapse(@NonNull FileChangeTypeNodeInfo pOtherNodeInfo)
  {
    nodeDescription = nodeDescription + " / " + pOtherNodeInfo.getNodeDescription();
    nodeFile = pOtherNodeInfo.getNodeFile();
  }

  public void setMembers(@NonNull List<IFileChangeType> pMembers)
  {
    members = pMembers;
  }
}
