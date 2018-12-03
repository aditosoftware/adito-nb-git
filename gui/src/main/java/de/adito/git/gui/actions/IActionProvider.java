package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IActionProvider {

    /**
     * @param pRepository   Observable with the current Repository
     * @param pTargetBranch Observable that contains the currently selected branch
     * @return Action whose actionPerformed method merges the two branches
     */
    Action getMergeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pTargetBranch);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method commits the selected files to HEAD
     */
    Action getCommitAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method performs a diff on the selected files and shows the changes between working copy and HEAD
     */
    Action getDiffAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method adds the passed files to the .gitignore
     */
    Action getIgnoreAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method adds a file/changes in a file to the staging. Also {@link de.adito.git.gui.actions.ResetFilesAction}
     */
    Action getAddAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method excludes a file (like git ignore, but does not show up in the .gitignore)
     */
    Action getExcludeAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository Observable with the current Repository
     * @param pBranch     branch to check out
     * @return Action whose actionPerformed method checks out a Branch
     */
    Action getCheckoutAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<IBranch>> pBranch);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method creates a new branch (only locally, still have to push it for it to be visible on origin/remotes)
     */
    Action getNewBranchAction(Observable<Optional<IRepository>> pRepository);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method pulls all changes from origin/a remote to the HEAD and the working directory
     */
    Action getPullAction(Observable<Optional<IRepository>> pRepository);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method pushes all changes from the HEAD to the origin/remote
     */
    Action getPushAction(Observable<Optional<IRepository>> pRepository);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method opens a window with all branches found for the passed repository
     */
    Action getShowAllBranchesAction(Observable<Optional<IRepository>> pRepository);

    /**
     * @param pRepository Observable with the current Repository
     * @param pBranches   the branch/es for whom to display the commits in a window
     * @return Action whose actionPerformed method opens a window with a list of all commits that belong to the selected branch/es
     */
    Action getShowAllCommitsForBranchAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IBranch>>> pBranches);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method opens a window with a list of all commits that belong to the project
     */
    Action getShowAllCommitsAction(Observable<Optional<IRepository>> pRepository);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method reverts the selected files in the working directory to the state of HEAD. (performs a checkout on the files)
     */
    Action getRevertWorkDirAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository              Observable with the current Repository
     * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method resets the selected files from staging (i.e. the opposite of add)
     */
    Action getResetFilesAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

    /**
     * @param pRepository                Observable with the current Repository
     * @param pCommitedCommitsObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
     * @return Action whose actionPerformed method resets the Index/Index + Head/Index + Head + working directory to the selected commit
     */
    Action getResetAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pCommitedCommitsObservable);

    /**
     * @param pRepository Observable with the current Repository
     * @return Action whose actionPerformed method opens a window with a list of all changed files of the working copy with the type of change
     */
    Action getShowStatusWindowAction(Observable<Optional<IRepository>> pRepository);

}
