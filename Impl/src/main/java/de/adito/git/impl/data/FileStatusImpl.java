package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.EStageState;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.IndexDiff;

import java.io.File;
import java.util.*;

/**
 * @author m.kaspera 21.09.2018
 */
public class FileStatusImpl implements IFileStatus {

    private Status status;

    public FileStatusImpl(Status pStatus){
        status = pStatus;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public boolean isClean() {
        return status.isClean();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public boolean hasUncommittedChanges() {
        return status.hasUncommittedChanges();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getAdded() {
        return status.getAdded();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getChanged() {
        return status.getChanged();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getRemoved() {
        return status.getRemoved();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getMissing() {
        return status.getMissing();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getModified() {
        return status.getModified();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getUntracked() {
        return status.getUntracked();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getUntrackedFolders() {
        return status.getUntrackedFolders();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getConflicting() {
        return status.getConflicting();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Map<String, EStageState> getConflictingStageState() {
        Map<String, EStageState> conflictingStageState = new HashMap<>();
        Map<String, IndexDiff.StageState> jgitConflictingStageState = status.getConflictingStageState();
        for(String fileName : jgitConflictingStageState.keySet()){
            conflictingStageState.put(fileName, _fromStageState(jgitConflictingStageState.get(fileName)));
        }
        return conflictingStageState;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getIgnoredNotInIndex() {
        return status.getIgnoredNotInIndex();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public Set<String> getUncommittedChanges() {
        return status.getUncommittedChanges();
    }

    /**
     *
     * {@inheritDoc}
     */
    public List<IFileChangeType> getUncommitted() {
        HashMap<String, EChangeType> fileChangeTypes = new HashMap<>();
        status.getChanged().forEach(changed -> fileChangeTypes.put(changed, EChangeType.CHANGED));
        status.getModified().forEach(modified -> fileChangeTypes.put(modified, EChangeType.MODIFY));
        status.getAdded().forEach(added -> fileChangeTypes.put(added, EChangeType.ADD));
        status.getUntracked().forEach(untracked -> fileChangeTypes.put(untracked, EChangeType.NEW));
        status.getRemoved().forEach(removed -> fileChangeTypes.put(removed, EChangeType.DELETE));
        status.getMissing().forEach(missing -> fileChangeTypes.put(missing, EChangeType.MISSING));
        status.getConflicting().forEach(conflicting -> fileChangeTypes.put(conflicting, EChangeType.CONFLICTING));
        return _toFileChangeTypes(fileChangeTypes);
    }

    private List<IFileChangeType> _toFileChangeTypes(HashMap<String, EChangeType> fileChanges){
        List<IFileChangeType> fileChangeTypes = new ArrayList<>();
        for(String filename: fileChanges.keySet()){
            fileChangeTypes.add(new FileChangeTypeImpl(new File(filename), fileChanges.get(filename)));
        }
        return fileChangeTypes;
    }

    /**
     * @param stageState IndexDiff.StageState to "wrap"
     * @return "wrapped" IndexDiff.StageState
     */
    private static EStageState _fromStageState(IndexDiff.StageState stageState){
        switch (stageState) {
            case BOTH_DELETED: // 0b001
                return EStageState.BOTH_DELETED;
            case ADDED_BY_US: // 0b010
                return EStageState.ADDED_BY_US;
            case DELETED_BY_THEM: // 0b011
                return EStageState.DELETED_BY_THEM;
            case ADDED_BY_THEM: // 0b100
                return EStageState.ADDED_BY_THEM;
            case DELETED_BY_US: // 0b101
                return EStageState.DELETED_BY_US;
            case BOTH_ADDED: // 0b110
                return EStageState.BOTH_ADDED;
            case BOTH_MODIFIED: // 0b111
                return EStageState.BOTH_MODIFIED;
            default:
                return null;
        }
    }
}
