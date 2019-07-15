package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IActionProvider
{

  /**
   * @param pRepository   Observable with the current Repository
   * @param pTargetBranch Observable that contains the currently selected branch
   * @return Action whose actionPerformed method merges the two branches
   */
  Action getMergeAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<IBranch>> pTargetBranch);

  /**
   * @param pRepository              Observable with the current Repository
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @param pMessageTemplate         String that will be the text that is pre-set as commit message, "" for no pre-set message
   * @return Action whose actionPerformed method commits the selected files to HEAD
   */
  Action getCommitAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable,
                         String pMessageTemplate);

  /**
   * @param pRepository              Observable with the current Repository
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method performs a diff on the selected files and shows the changes between working copy and HEAD
   */
  Action getDiffToHeadAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  /**
   * @param pRepository              Observable with the current Repository
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method adds the passed files to the .gitignore
   */
  Action getIgnoreAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  /**
   * @param pRepository              Observable with the current Repository
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method excludes a file (like git ignore, but does not show up in the .gitignore)
   */
  Action getExcludeAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  /**
   * @param pRepository Observable with the current Repository
   * @param pBranch     branch to check out
   * @return Action whose actionPerformed method checks out a Branch
   */
  Action getCheckoutAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<IBranch>> pBranch);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method creates a new branch (only locally, still have to push it for it to be visible on origin/remotes)
   */
  Action getNewBranchAction(Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository    Observable with the current Repository
   * @param pStartingPoint Observable with the commit that should be the origin/source of the newly created branch
   * @return Action whose actionPerformed method creates a new branch (only locally, still have to push it for it to be visible on origin/remotes)
   */
  Action getNewBranchAction(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pStartingPoint);

  /**
   * @param pRepository     Observable with the current Repository
   * @param pSelectedBranch Observable determining the currently selected branch (and thus the one to be deleted)
   * @return Action whose actionPerformed method deletes the currently selected branch
   */
  Action getDeleteBranchAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<IBranch>> pSelectedBranch);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method pulls all changes from origin/a remote to the HEAD and the working directory
   */
  Action getPullAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method pushes all changes from the HEAD to the origin/remote
   */
  Action getPushAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method opens a window with all branches found for the passed repository
   */
  Action getShowAllBranchesAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository Observable with the current Repository
   * @param pBranches   the branch/es for whom to display the commits in a window
   * @return Action whose actionPerformed method opens a window with a list of all commits that belong to the selected branch/es
   */
  Action getShowAllCommitsForBranchAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IBranch>>> pBranches);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method opens a window with a list of all commits that belong to the project
   */
  Action getShowAllCommitsAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository Observable with the current Repository
   * @param pFile       Observable of a List of files for which to find the affecting commits. List size should be 1 for the action to be enabled
   * @return Action whose actionPerformed method opens a window with a list of all commits that affected the passed file
   */
  Action getShowCommitsForFileAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<List<File>> pFile);

  /**
   * @param pRepository              Observable with the current Repository
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method reverts the selected files in the working dir to the state of HEAD (performs a checkout on the files)
   */
  Action getRevertWorkDirAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  /**
   * @param pRepository                Observable with the current Repository
   * @param pCommitedCommitsObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method resets the Index/Index + Head/Index + Head + working directory to the selected commit
   */
  Action getResetAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pCommitedCommitsObservable);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method opens a window with a list of all changed files of the working copy with the type of change
   */
  Action getShowStatusWindowAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository              Observable with the current Repository
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method tries to determine the causes of conflicting files and shows a dialog to resolve those conflicts
   */
  Action getResolveConflictsAction(@NotNull Observable<Optional<IRepository>> pRepository,
                                   @NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method opens a settings window for the current repository
   */
  Action getGitConfigAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository               Observable with the current Repository
   * @param pSelectedCommitObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method shows a dialog with which to enter the tag name in order to assign a tag to the selected commit
   */
  Action getAddTagAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable);

  /**
   * @param pRepository Observable with the current Repository
   * @param pTag        tag that should be deleted
   * @return Action whose actionPreformed method tries to delete the passed tag
   */
  Action getDeleteSpecificTagAction(@NotNull Observable<Optional<IRepository>> pRepository, ITag pTag);

  /**
   * @param pRepository    Observable with the current Repository
   * @param pTagObservable Observable of the tag that should be deleted
   * @return Action whose actionPreformed method tries to delete the passed tag
   */
  Action getDeleteTagAction(@NotNull Observable<Optional<IRepository>> pRepository, Observable<Optional<ITag>> pTagObservable);

  /**
   * @param pRepository               Observable with the current Repository
   * @param pSelectedCommitObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @param pParentCommit             to which parentCommit the diff should be made, important for merge-commits
   * @param pSelectedFile             File that is currently selected and should also be selected in the diff dialog. Can be optional.empty
   * @return Action whose actionPerformed method shows a dialog that shows the differences between the commit and its parent
   */
  Action getDiffCommitsAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                              @NotNull Observable<Optional<ICommit>> pParentCommit, @NotNull Observable<Optional<String>> pSelectedFile);

  /**
   * @param pRepository               Observable with the current Repository
   * @param pSelectedCommitObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @param pSelectedFile             File that is currently selected and should also be selected in the diff dialog. Can be optional.empty
   * @return Action whose actionPerformed method shows a dialog that shows the differences from the selected commit to HEAD
   */
  Action getDiffCommitToHeadAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                   @NotNull Observable<Optional<String>> pSelectedFile);

  /**
   * @param pSelectedFilesObservable Observable with the list of selected IFileChangeTypes. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method opens the currently selected files (those the observable returns)
   */
  Action getOpenFileAction(@NotNull Observable<Optional<List<IFileChangeType>>> pSelectedFilesObservable);

  /**
   * @param pSelectedFile Observable with the absolute path of the currently selected file
   * @return Action whose actionPerformed method opens the currently selected file
   */
  Action getOpenFileStringAction(@NotNull Observable<Optional<String>> pSelectedFile);

  /**
   * returns an action that executes the passed Runnable when the action is called
   *
   * @param pRefreshContentCallBack Runnable that updates the model with the latest information
   * @return Action whose actionPerformed method executes the passed Runnable
   */
  Action getRefreshContentAction(@NotNull Runnable pRefreshContentCallBack);

  /**
   * returns an action that manually refreshes the status of the current repository
   *
   * @param pRepository  Observable with the current Repository
   * @param pRefreshTree Runnable that triggers a refresh of the treeModel
   * @return Action whose actionPerformed method calls a method that manually refreshes the status of the current repository
   */
  Action getRefreshStatusAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Runnable pRefreshTree);

  /**
   * @param pRepository               Observable with the current Repository
   * @param pSelectedCommitObservable Observable with the list of selected ICommits. Obtainable by i.e. the {@link de.adito.git.gui.rxjava.ObservableListSelectionModel}
   * @return Action whose actionPerformed method cherry picks the selected commits on top of the current HEAD
   */
  Action getCherryPickAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method shows a dialog where the specifics of the stash command can be set and then stashes
   * the current changes
   */
  Action getStashChangesAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method shows a dialog where the specifics of the stash command can be set and then stashes
   * the current changes
   */
  Action getUnStashChangesAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * @param pRepository Observable with the current Repository
   * @param pCommitId   Observable that contains the commit id of the stash commit to delete. If empty/null the latest stashed commit is dropped
   *                    instead
   * @return Action whose actionPerformed method deletes the stashed commit specified in pCommitId
   */
  Action getDeleteStashedCommitAction(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<String>> pCommitId);

  /**
   * Action that performs a fetch operation
   *
   * @param pRepository Observable with the current Repository
   * @return Action whose actionPerformed method performs a fetch on the remote repository
   */
  Action getFetchAction(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * collapses the tree
   *
   * @param pTree JTree whose nodes should be collapsed
   * @return Action whose actionPerformed method collapses all nodes in the tree
   */
  Action getCollapseTreeAction(@NotNull JTree pTree);

  /**
   * expands the tree
   *
   * @param pTree JTree whose nodes should be expanded
   * @return Action whose actionPerformed method expands all nodes in the tree
   */
  Action getExpandTreeAction(@NotNull JTree pTree);

  /**
   * Action that displays an overview of the tags in the git repository
   *
   * @param pSelectedCommitCallback Consumer that selects the passed Commit
   * @param pRepository             Observable with the current Repository
   * @return Action whose actionPerformed method displays an overview of the tags in the git repository
   */
  Action getShowTagWindowAction(@NotNull Consumer<ICommit> pSelectedCommitCallback, @NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * swaps the treeView from a flat view (only leaves below root) to a hierarchical view
   *
   * @param pTree             Tree whose view should be updated
   * @param pChangeList       Observable with the list of changed files
   * @param pProjectDirectory root folder of the project
   * @return Action whose actionPerformed method swaps the treeView from a flat view (only leaves below root) to a hierarchical view
   */
  Action getSwitchTreeViewAction(@NotNull JTree pTree, @NotNull Observable<List<IFileChangeType>> pChangeList, @NotNull File pProjectDirectory);

  /**
   * swaps the treeView from a flat diff view (only leaves below root/commit nodes) to a hierarchical view
   *
   * @param pTree             Tree whose view should be updated
   * @param pChangeList       Observable with the list of changed files
   * @param pProjectDirectory root folder of the project
   * @return Action whose actionPerformed method swaps the treeView from a flat view (only leaves below root) to a hierarchical view
   */
  Action getSwitchDiffTreeViewAction(@NotNull JTree pTree, @NotNull Observable<List<IDiffInfo>> pChangeList, @NotNull File pProjectDirectory);
}
