package de.adito.git.wrappers;

import de.adito.git.api.data.EStageState;
import de.adito.git.api.IFileStatus;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.IndexDiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
