package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IActionProvider {

    /**
     * @param pRepository    Observable with the current Repository
     * @param pTargetBranch  Observable with list of size 1 that contains the currently selected branch
     * @return Action whose actionPerformed method merges the two branches
     */
    Action getMergeAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pTargetBranch);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method commits the selected files to HEAD
     */
    Action getCommitAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method performs a diff on the selected files and shows the changes between working copy and HEAD
     */
    Action getDiffAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method adds the passed files to the .gitignore
     */
    Action getIgnoreAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method adds a file/changes in a file to the staging. Also {@link de.adito.git.gui.actions.ResetFilesAction}
     */
    Action getAddAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method excludes a file (like git ignore, but does not show up in the .gitignore)
     */
    Action getExcludeAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pBranchList List of branches to check out, should only contain one entry
     * @return Action whose actionPerformed method checks out a Branch
     */
    Action getCheckoutAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranchList);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method creates a new branch (only locally, still have to push it for it to be visible on origin/remotes)
     */
    Action getNewBranchAction(Observable<IRepository> pRepository);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pTargetId the id for the remote/branch to pull from
     * @return Action whose actionPerformed method pulls all changes from origin/a remote to the HEAD and the working directory
     */
    Action getPullAction(Observable<IRepository> pRepository, String pTargetId);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method pushes all changes from the HEAD to the origin/remote
     */
    Action getPushAction(Observable<IRepository> pRepository);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method opens a window with all branches found for the passed repository
     */
    Action getShowAllBranchesAction(Observable<IRepository> pRepository);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pBranches the branch/es for whom to display the commits in a window
     * @return Action whose actionPerformed method opens a window with a list of all commits that belong to the selected branch/es
     */
    Action getShowAllCommitsAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranches);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method reverts the selected files in the working directory to the state of HEAD. (performs a checkout on the files)
     */
    Action getRevertWorkDirAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     *
     * @param pRepository Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method resets the selected files from staging (i.e. the opposite of add)
     */
    Action getResetFilesAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    /**
     * @param pRepository Observable with the current Repository
     * @param pCommitedCommitsObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableTable}
     * @return Action whose actionPerformed method resets the Index/Index + Head/Index + Head + working directory to the selected commit
     */
    Action getResetAction(Observable<IRepository> pRepository, Observable<List<ICommit>> pCommitedCommitsObservable);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method opens a window with a list of all changed files of the working copy with the type of change
     */
    Action getShowStatusWindowAction(Observable<IRepository> pRepository);

}
