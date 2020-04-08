package de.adito.git.impl;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.EPushResult;
import de.adito.git.api.data.ITrackingRefUpdate;
import de.adito.git.api.data.diff.EChangeType;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Translates the JGit enums to the API enums and back
 *
 * @author m.kaspera 24.09.2018
 */
public class EnumMappings
{

  /**
   * @param pStatus RemoteRefUpdate.Status to map to PushResult
   * @return PushResult corresponding to pStatus
   */
  @NotNull
  public static EPushResult toPushResult(@NotNull RemoteRefUpdate.Status pStatus)
  {
    EPushResult result;
    switch (pStatus)
    {
      case UP_TO_DATE:
        result = EPushResult.UP_TO_DATE;
        break;
      case OK:
        result = EPushResult.OK;
        break;
      case NON_EXISTING:
        result = EPushResult.NON_EXISTING;
        break;
      case REJECTED_NODELETE:
        result = EPushResult.REJECTED_NO_DELETE;
        break;
      case REJECTED_NONFASTFORWARD:
        result = EPushResult.REJECTED_NON_FAST_FORWARD;
        break;
      case REJECTED_REMOTE_CHANGED:
        result = EPushResult.REJECTED_REMOTE_CHANGED;
        break;
      default:
        result = EPushResult.REJECTED_OTHER_REASON;
    }
    return result;
  }

  /**
   * @param pChangeType {@link org.eclipse.jgit.diff.DiffEntry.ChangeType} to translate to an {@link EChangeType}
   * @return translated {@link EChangeType}
   */
  @NotNull
  public static EChangeType toEChangeType(@NotNull DiffEntry.ChangeType pChangeType)
  {
    switch (pChangeType)
    {
      case MODIFY:
        return EChangeType.MODIFY;
      case ADD:
        return EChangeType.ADD;
      case DELETE:
        return EChangeType.DELETE;
      case RENAME:
        return EChangeType.RENAME;
      case COPY:
        return EChangeType.COPY;
      default:
        return EChangeType.SAME;
    }
  }

  /**
   * @param pChangeType {@link org.eclipse.jgit.diff.DiffEntry.ChangeType} to translate to an {@link EChangeType}
   * @return translated {@link EChangeType}
   */
  static EChangeType toEChangeType(@NotNull Edit.Type pChangeType)
  {
    switch (pChangeType)
    {
      case INSERT:
        return EChangeType.ADD;
      case DELETE:
        return EChangeType.DELETE;
      case REPLACE:
        return EChangeType.MODIFY;
      case EMPTY:
        return EChangeType.SAME;
      default:
        return null;
    }
  }

  /**
   * @param pFileMode {@link FileMode} to translate to {@link EFileType}
   * @return translated {@link EFileType}
   */
  public static EFileType toEFileType(@NotNull FileMode pFileMode)
  {
    switch (pFileMode.getBits())
    {
      case FileMode.TYPE_TREE:
        return EFileType.TREE;
      case FileMode.TYPE_MISSING:
        return EFileType.MISSING;
      case FileMode.TYPE_GITLINK:
        return EFileType.GITLINK;
      case FileMode.TYPE_SYMLINK:
        return EFileType.SYMLINK;
      // {@see org.eclipse.jgit.lib.FileMode}
      case 0100644:
        return EFileType.FILE;
      // {@see org.eclipse.jgit.lib.FileMode}
      case 0100755:
        return EFileType.EXECUTABLE_FILE;
      default:
        return null;
    }
  }

  /**
   * maps from the JGit State to our State
   *
   * @param pRepositoryState JGit repositoryState to map to our State object
   * @return State object
   */
  public static IRepository.State mapRepositoryState(RepositoryState pRepositoryState)
  {
    IRepository.State returnValue = null;
    switch (pRepositoryState)
    {
      case CHERRY_PICKING:
        returnValue = IRepository.State.CHERRY_PICKING;
        break;
      case CHERRY_PICKING_RESOLVED:
        returnValue = IRepository.State.CHERRY_PICKING_RESOLVED;
        break;
      case APPLY:
        returnValue = IRepository.State.APPLY;
        break;
      case BARE:
        returnValue = IRepository.State.BARE;
        break;
      case BISECTING:
        returnValue = IRepository.State.BISECTING;
        break;
      case MERGING:
        returnValue = IRepository.State.MERGING;
        break;
      case MERGING_RESOLVED:
        returnValue = IRepository.State.MERGING_RESOLVED;
        break;
      case REBASING_MERGE:
        returnValue = IRepository.State.REBASING_MERGE;
        break;
      case REBASING:
        returnValue = IRepository.State.REBASING;
        break;
      case REBASING_INTERACTIVE:
        returnValue = IRepository.State.REBASING_INTERACTIVE;
        break;
      case REBASING_REBASING:
        returnValue = IRepository.State.REBASING_REBASING;
        break;
      case REVERTING:
        returnValue = IRepository.State.REVERTING;
        break;
      case REVERTING_RESOLVED:
        returnValue = IRepository.State.REVERTING_RESOLVED;
        break;
      case SAFE:
        returnValue = IRepository.State.SAFE;
        break;
    }
    return returnValue;
  }

  /**
   * maps JGit RefUpdate.Result to the ITrackingRefUpdate.ResultType of this API
   *
   * @param pResult JGit Result to convert
   * @return corresponding ResultType of the API
   */
  public static ITrackingRefUpdate.ResultType mapTrackingRefResultType(RefUpdate.Result pResult)
  {
    ITrackingRefUpdate.ResultType resultType;
    switch (pResult)
    {
      case LOCK_FAILURE:
        resultType = ITrackingRefUpdate.ResultType.LOCK_FAILURE;
        break;
      case NEW:
        resultType = ITrackingRefUpdate.ResultType.NEW;
        break;
      case FORCED:
        resultType = ITrackingRefUpdate.ResultType.FORCED;
        break;
      case RENAMED:
        resultType = ITrackingRefUpdate.ResultType.RENAMED;
        break;
      case REJECTED:
        resultType = ITrackingRefUpdate.ResultType.REJECTED;
        break;
      case NO_CHANGE:
        resultType = ITrackingRefUpdate.ResultType.NO_CHANGE;
        break;
      case IO_FAILURE:
        resultType = ITrackingRefUpdate.ResultType.IO_FAILURE;
        break;
      case FAST_FORWARD:
        resultType = ITrackingRefUpdate.ResultType.FAST_FORWARD;
        break;
      case NOT_ATTEMPTED:
        resultType = ITrackingRefUpdate.ResultType.NOT_ATTEMPTED;
        break;
      case REJECTED_OTHER_REASON:
        resultType = ITrackingRefUpdate.ResultType.REJECTED_OTHER_REASON;
        break;
      case REJECTED_CURRENT_BRANCH:
        resultType = ITrackingRefUpdate.ResultType.REJECTED_CURRENT_BRANCH;
        break;
      case REJECTED_MISSING_OBJECT:
        resultType = ITrackingRefUpdate.ResultType.REJECTED_MISSING_OBJECT;
        break;
      default:
        resultType = null;
    }
    return resultType;
  }

  /**
   * Converts a Edit Type to an EChangeType
   *
   * @param pType type to convert
   * @return EChangeType that corresponds to the given Edit.Type
   */
  @Nullable
  public static de.adito.git.api.data.diff.EChangeType toChangeType(@NotNull Edit.Type pType)
  {
    de.adito.git.api.data.diff.EChangeType returnValue;
    switch (pType)
    {
      case REPLACE:
        returnValue = de.adito.git.api.data.diff.EChangeType.MODIFY;
        break;
      case INSERT:
        returnValue = de.adito.git.api.data.diff.EChangeType.ADD;
        break;
      case DELETE:
        returnValue = de.adito.git.api.data.diff.EChangeType.DELETE;
        break;
      default:
        returnValue = null;
    }
    return returnValue;
  }

}
