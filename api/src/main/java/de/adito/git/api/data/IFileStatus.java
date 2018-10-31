package de.adito.git.api.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author m.kaspera 20.09.2018
 */
public interface IFileStatus {

    /**
     *
     * @return {@code true} if no differences exist between the working-tree, the index,
     *          and the current HEAD, {@code false} if differences do exist
     */
    boolean isClean();

    /**
     *
     * @return {@code true} if any tracked file is changed
     */
    boolean hasUncommittedChanges();

    /**
     *
     * @return list of files added to the index, not in HEAD
     *          (e.g. what you get if you call 'git add ...' on a newly created file)
     */
    Set<String> getAdded();

    /**
     *
     * @return list of files changed from HEAD to index
     *          (e.g. what you get if you modify an existing file and call 'git add ...' on it)
     */
    Set<String> getChanged();

    /**
     *
     * @return list of files removed from index, but in HEAD
     *          (e.g. what you get if you call 'git rm ...' on a existing file)
     */
    Set<String> getRemoved();

    /**
     *
     * @return list of files in index, but not filesystem
     *          (e.g. what you get if you call 'rm ...' on a existing file)
     */
    Set<String> getMissing();

    /**
     *
     * @return list of files modified on disk relative to the index
     *          (e.g. what you get if you modify an existing file without adding it to the index)
     */
    Set<String> getModified();

    /**
     *
     * @return list of files that are not ignored, and not in the index.
     *          (e.g. what you get if you create a new file without adding it to the index)
     */
    Map<String, EChangeType> getUntracked();

    /**
     *
     * @return Set an Ordnern die nicht ignoriert wurden und sich nicht im Index befinden
     */
    Set<String> getUntrackedFolders();

    /**
     *
     * @return set of files that are in conflict.
     *          (e.g what you get if you modify file that was modified by someone else in the meantime)
     */
    Set<String> getConflicting();

    /**
     *
     * @return Map die Files im Konfliktzustand auf ihre {@link EStageState} mappt
     */
    Map<String, EStageState> getConflictingStageState();

    /**
     *
     * @return set of files and folders that are ignored and not in the index.
     */
    Set<String> getIgnoredNotInIndex();

    /**
     *
     * @return set of files and folders that are known to the repo and changed
     * 	 *         either in the index or in the working tree.
     */
    Set<String> getUncommittedChanges();

    /**
     *
     * @return List<IFileChangeType> containing all the files that were changed
     *          in comparison to HEAD, with the type of change that happened
     */
    List<IFileChangeType> getUncommitted();
}
