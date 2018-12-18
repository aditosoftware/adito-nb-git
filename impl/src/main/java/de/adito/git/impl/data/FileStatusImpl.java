package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.IndexDiff;

import java.io.File;
import java.text.Collator;
import java.util.*;

/**
 * @author m.kaspera 21.09.2018
 */
public class FileStatusImpl implements IFileStatus
{

  private Status status;
  private File gitDirectory;
  private List<IFileChangeType> uncommittedFiles;

  public FileStatusImpl(Status pStatus, File pGitDirectory)
  {
    status = pStatus;
    gitDirectory = pGitDirectory;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isClean()
  {
    return status.isClean();
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasUncommittedChanges()
  {
    return status.hasUncommittedChanges();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getAdded()
  {
    return status.getAdded();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getChanged()
  {
    return status.getChanged();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getRemoved()
  {
    return status.getRemoved();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getMissing()
  {
    return status.getMissing();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getModified()
  {
    return status.getModified();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getUntracked()
  {
    return status.getUntracked();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getUntrackedFolders()
  {
    return status.getUntrackedFolders();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getConflicting()
  {
    return status.getConflicting();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, EStageState> getConflictingStageState()
  {
    Map<String, EStageState> conflictingStageState = new HashMap<>();
    Map<String, IndexDiff.StageState> jgitConflictingStageState = status.getConflictingStageState();
    for (Map.Entry<String, IndexDiff.StageState> conflictingStageStateEntry : jgitConflictingStageState.entrySet())
    {
      conflictingStageState.put(conflictingStageStateEntry.getKey(), _fromStageState(conflictingStageStateEntry.getValue()));
    }
    return conflictingStageState;
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getIgnoredNotInIndex()
  {
    return status.getIgnoredNotInIndex();
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getUncommittedChanges()
  {
    return status.getUncommittedChanges();
  }

  /**
   * {@inheritDoc}
   */
  public List<IFileChangeType> getUncommitted()
  {
    if (uncommittedFiles == null)
    {
        /*
            can't use a stream with distinct() here since
            1) need to put the EChangeType in, according to which list we retrieved
            2) the ordering is important, if a File is conflicting the EChangeType should read
                conflicting in the end, not changed. distinct() only guarantees that on ordered streams
         */
      HashMap<String, EChangeType> fileChangeTypes = new HashMap<>();
      status.getChanged().forEach(changed -> fileChangeTypes.put(changed, EChangeType.CHANGED));
      status.getModified().forEach(modified -> fileChangeTypes.put(modified, EChangeType.MODIFY));
      status.getAdded().forEach(added -> fileChangeTypes.put(added, EChangeType.ADD));
      status.getUntracked().forEach(unTracked -> fileChangeTypes.put(unTracked, EChangeType.NEW));
      status.getRemoved().forEach(removed -> fileChangeTypes.put(removed, EChangeType.DELETE));
      status.getMissing().forEach(missing -> fileChangeTypes.put(missing, EChangeType.MISSING));
      status.getConflicting().forEach(conflicting -> fileChangeTypes.put(conflicting, EChangeType.CONFLICTING));
      uncommittedFiles = _toFileChangeTypes(fileChangeTypes);
    }
    return uncommittedFiles;
  }

  private List<IFileChangeType> _toFileChangeTypes(HashMap<String, EChangeType> pFileChanges)
  {
    List<IFileChangeType> fileChangeTypes = new ArrayList<>();
    for (Map.Entry<String, EChangeType> fileChangeEntry : pFileChanges.entrySet())
    {
      fileChangeTypes.add(new FileChangeTypeImpl(new File(gitDirectory.getParent(), fileChangeEntry.getKey()), fileChangeEntry.getValue()));
    }
    fileChangeTypes.sort(Comparator.comparing(pChangeType -> pChangeType.getFile().getName(), Collator.getInstance()));
    return fileChangeTypes;
  }

  /**
   * @param pStageState IndexDiff.StageState to "wrap"
   * @return "wrapped" IndexDiff.StageState
   */
  private static EStageState _fromStageState(IndexDiff.StageState pStageState)
  {
    EStageState returnValue;
    switch (pStageState)
    {
      case BOTH_DELETED: // 0b001
        returnValue = EStageState.BOTH_DELETED;
        break;
      case ADDED_BY_US: // 0b010
        returnValue = EStageState.ADDED_BY_US;
        break;
      case DELETED_BY_THEM: // 0b011
        returnValue = EStageState.DELETED_BY_THEM;
        break;
      case ADDED_BY_THEM: // 0b100
        returnValue = EStageState.ADDED_BY_THEM;
        break;
      case DELETED_BY_US: // 0b101
        returnValue = EStageState.DELETED_BY_US;
        break;
      case BOTH_ADDED: // 0b110
        returnValue = EStageState.BOTH_ADDED;
        break;
      case BOTH_MODIFIED: // 0b111
        returnValue = EStageState.BOTH_MODIFIED;
        break;
      default:
        returnValue = null;
    }
    return returnValue;
  }

  /**
   * checks whether or not the lists of pOtherStatus and this status are the same/contain the same elements
   *
   * @param pOtherStatus The IFileStatus whose uncommitted files should be compared
   * @return whether or not the lists of pOtherStatus and this status are the same
   */
  private boolean _compareUncommittedLists(IFileStatus pOtherStatus)
  {
    List<IFileChangeType> thisUncommitted = getUncommitted();
    List<IFileChangeType> thatUncommitted = pOtherStatus.getUncommitted();
    if (thisUncommitted.size() != thatUncommitted.size())
      return false;
    for (IFileChangeType changeType : thisUncommitted)
    {
      boolean containedValue = false;
      for (IFileChangeType otherChangeType : thatUncommitted)
      {
        if (changeType.getFile().equals(otherChangeType.getFile()))
        {
          if (changeType.getChangeType() != otherChangeType.getChangeType())
            return false;
          else
          {
            containedValue = true;
            break;
          }
        }
      }
      if (!containedValue)
        return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    FileStatusImpl that = (FileStatusImpl) pO;
    return gitDirectory.equals(that.gitDirectory) && hasUncommittedChanges() == that.hasUncommittedChanges()
        && _compareUncommittedLists(that);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(gitDirectory, getUncommitted());
  }
}
