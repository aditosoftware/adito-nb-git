package de.adito.git.api.data;

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.IndexDiff;
import de.adito.git.wrappers.EStageState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author m.kaspera 21.09.2018
 */
public class FileStatusImpl implements IFileStatus {

    private Status status;

    FileStatusImpl(Status pStatus){
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
        Map<String, EStageState> conflictingStageState = new HashMap<String, EStageState>();
        Map<String, IndexDiff.StageState> jgitConflictingStageState = status.getConflictingStageState();
        for(String fileName : jgitConflictingStageState.keySet()){
            conflictingStageState.put(fileName, EStageState.fromStageState(jgitConflictingStageState.get(fileName)));
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
}
