package data;

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.IndexDiff;
import wrappers.EStageState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author m.kaspera 21.09.2018
 */
public class FileStatusImpl implements IFileStatus {

    private Status status;

    public boolean isClean() {
        return status.isClean();
    }

    public boolean hasUncommittedChanges() {
        return status.hasUncommittedChanges();
    }

    public Set<String> getAdded() {
        return status.getAdded();
    }

    public Set<String> getChanged() {
        return status.getChanged();
    }

    public Set<String> getRemoved() {
        return status.getRemoved();
    }

    public Set<String> getMissing() {
        return status.getMissing();
    }

    public Set<String> getModified() {
        return status.getModified();
    }

    public Set<String> getUntracked() {
        return status.getUntracked();
    }

    public Set<String> getUntrackedFolders() {
        return status.getUntrackedFolders();
    }

    public Set<String> getConflicting() {
        return status.getConflicting();
    }

    public Map<String, EStageState> getConflictingStageState() {
        Map<String, EStageState> conflictingStageState = new HashMap<String, EStageState>();
        Map<String, IndexDiff.StageState> jgitConflictingStageState = status.getConflictingStageState();
        for(String fileName : jgitConflictingStageState.keySet()){
            conflictingStageState.put(fileName, EStageState.fromStageState(jgitConflictingStageState.get(fileName)));
        }
        return conflictingStageState;
    }

    public Set<String> getIgnoredNotInIndex() {
        return status.getIgnoredNotInIndex();
    }

    public Set<String> getUncommittedChanges() {
        return status.getUncommittedChanges();
    }
}
