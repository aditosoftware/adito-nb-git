package de.adito.git.impl.data;

import de.adito.git.api.data.ITrackingRefUpdate;
import de.adito.git.impl.EnumMappings;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Implementation of ITrackingRefUpdate, basically a wrapper around the JGit TrackingRefUpdate
 *
 * @author m.kaspera, 12.11.2019
 */
public class TrackingRefUpdate implements ITrackingRefUpdate
{

  private final String remoteName;
  private final String localName;
  private final String oldId;
  private final String newId;
  private final ResultType resultType;

  public TrackingRefUpdate(String pRemoteName, String pLocalName, String pOldId, String pNewId, ResultType pResultType)
  {
    remoteName = pRemoteName;
    localName = pLocalName;
    oldId = pOldId;
    newId = pNewId;
    resultType = pResultType;
  }

  /**
   * @param pTrackingRefUpdate JGit TrackingRefUpdate that should be wrapped
   */
  public TrackingRefUpdate(org.eclipse.jgit.transport.TrackingRefUpdate pTrackingRefUpdate)
  {
    remoteName = pTrackingRefUpdate.getRemoteName();
    localName = pTrackingRefUpdate.getLocalName();
    oldId = ObjectId.toString(pTrackingRefUpdate.getOldObjectId());
    newId = ObjectId.toString(pTrackingRefUpdate.getNewObjectId());
    resultType = EnumMappings.mapTrackingRefResultType(pTrackingRefUpdate.getResult());
  }

  @Override
  public String getRemoteName()
  {
    return remoteName;
  }

  @Override
  public String getLocalName()
  {
    return localName;
  }

  @Override
  public String getOldId()
  {
    return oldId;
  }

  @Override
  public String getNewId()
  {
    return newId;
  }

  @Override
  public ResultType getResult()
  {
    return resultType;
  }

  @Override
  public String toString()
  {
    return localName + " | " + remoteName + ": " + oldId + " -> " + newId + " Result: " + resultType;
  }
}
