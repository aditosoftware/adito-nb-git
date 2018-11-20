package de.adito.git.api.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An empty File object
 *
 * @author a.arnold, 06.11.2018
 */
public class EmptyFile implements IFileStatus {
    @Override
    public boolean isClean() {
        throw new RuntimeException();
    }

    @Override
    public boolean hasUncommittedChanges() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getAdded() {
        throw new RuntimeException();

    }

    @Override
    public Set<String> getChanged() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getRemoved() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getMissing() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getModified() {
        throw new RuntimeException();
    }

    @Override
    public List<IFileChangeType> getUntracked() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getUntrackedFolders() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getConflicting() {
        throw new RuntimeException();
    }

    @Override
    public Map<String, EStageState> getConflictingStageState() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getIgnoredNotInIndex() {
        throw new RuntimeException();
    }

    @Override
    public Set<String> getUncommittedChanges() {
        throw new RuntimeException();
    }

    @Override
    public List<IFileChangeType> getUncommitted() {
        throw new RuntimeException();
    }
}
