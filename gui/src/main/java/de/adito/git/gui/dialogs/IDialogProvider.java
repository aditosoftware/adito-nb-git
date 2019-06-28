package de.adito.git.gui.dialogs;

import com.google.common.collect.Multimap;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.dialogs.results.StashChangesResult;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IDialogProvider
{

  /**
   * Shows a dialog that shows a list of conflicting files and has options to solve those conflict
   *
   * @param pRepository         Observable with the current Repository
   * @param pMergeConflictDiffs List of IMergeDiffs detailing the conflicting files
   * @return DialogResult with information such as "has the user pressed OK?"
   */
  @NotNull
  DialogResult showMergeConflictDialog(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull List<IMergeDiff> pMergeConflictDiffs);

  /**
   * Shows a dialog with a three-way merge based on the information from pMergeDiff
   *
   * @param pMergeDiff MergeDiff describing the changes to the file by both conflict sides
   * @return DialogResult with information such as "has the user pressed OK?"
   */
  @NotNull
  DialogResult showMergeConflictResolutionDialog(@NotNull IMergeDiff pMergeDiff);

  /**
   * Shows a dialog which show which changes happened to a file, based on the IFileDiffs
   *
   * @param pFileDiffs     List with all IFileDiffs that can potentially be displayed
   * @param pSelectedFile  file whose diff should be shown when the dialog opens
   * @param pAcceptChange  true if changes can be moved from the left to the right side
   * @param pShowFileTable true if the table with pFileDiffs should be shown. This enables the user to choose which file from pFileDiffs to display
   * @return DialogResult with information such as "has the user pressed OK?"
   */
  @NotNull
  DialogResult showDiffDialog(@NotNull List<IFileDiff> pFileDiffs, @Nullable String pSelectedFile, boolean pAcceptChange, boolean pShowFileTable);

  /**
   * Shows a dialog where the user can choose files to be commited and enter a commit message
   *
   * @param pRepository      Observable with the current Repository
   * @param pFilesToCommit   List of potential files to commit
   * @param pMessageTemplate message that should be set as the commit message at the start
   * @return DialogResult with information such as "has the user pressed OK?", which files the user selected to be commited and if the commit should be amended
   */
  @NotNull
  DialogResult<CommitDialog, CommitDialogResult> showCommitDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                  @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                                                  @NotNull String pMessageTemplate);

  /**
   * Shows a dialog with which the user can create a new branch
   *
   * @param pRepository Observable with the current Repository
   * @return DialogResult with information such as "has the user pressed OK?" and the name of the branch that should be created
   */
  @NotNull
  DialogResult showNewBranchDialog(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * Shows a dialog where the user can select the reset type
   *
   * @return DialogResult with information such as "has the user pressed OK?" and the EResetType the user selected
   */
  @NotNull
  DialogResult<ResetDialog, EResetType> showResetDialog();

  /**
   * Shows a dialog with the commits to be pushed and an option to select if tags should be pushed as well
   *
   * @param pRepository Observable with the current Repository
   * @param pCommitList List of commits that will be pushed
   * @return DialogResult with information such as "has the user pressed OK?", the information is a boolean that shows if tags should be pushed as well
   */
  @NotNull
  DialogResult<PushDialog, Boolean> showPushDialog(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull List<ICommit> pCommitList);

  /**
   * Shows a dialog where the user can select a stashed commit, or delete stashed commits
   *
   * @param pRepository     Observable with the current Repository
   * @param pStashedCommits List of currently stashed commits
   * @return DialogResult with information such as "has the user pressed OK?" and the ID of the stashed commit the user has selected
   */
  @NotNull
  DialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                                      @NotNull List<ICommit> pStashedCommits);

  /**
   * Shows a dialog that allows the user to enter a password
   *
   * @param pMessage Message to display for the user (should contain information about the use of the password)
   * @return DialogResult with information such as "has the user pressed OK?" and a char array containing the entered password. Null this ASAP
   */
  @NotNull
  DialogResult<PasswordPromptDialog, char[]> showPasswordPromptDialog(@NotNull String pMessage);

  /**
   * Shows a basic prompt dialog with a message for the user and an input field
   *
   * @param pMessage Message to display
   * @param pDefaultValue default value for the input field
   * @return DialogResult with information such as "has the user pressed OK?" and the text the user entered
   */
  @NotNull
  DialogResult showUserPromptDialog(@NotNull String pMessage, @Nullable String pDefaultValue);

  /**
   * Shows a basic yes no dialog with the specified message as information for the user
   *
   * @param pMessage message to display for the user
   * @return DialogResult with information about the choice of the user
   */
  @NotNull
  DialogResult showYesNoDialog(@NotNull String pMessage);

  /**
   * Shows a dialog that presents the user the list of files to be reverted as tree
   *
   * @param pFilesToRevert List of files that are selected to be reverted
   * @param pProjectDir top-level directory of the project
   * @return DialogResult with information such as "has the user pressed OK?"
   */
  @NotNull
  DialogResult showRevertDialog(@NotNull List<IFileChangeType> pFilesToRevert, @NotNull File pProjectDir);

  /**
   * Shows a dialog that asks the user to confirm the deletion of branch
   *
   * @param pBranchName name of the branch to be deleted
   * @return DialogResult with information such as "has the user pressed OK?"
   */
  @NotNull
  DialogResult<DeleteBranchDialog, Boolean> showDeleteBranchDialog(@NotNull String pBranchName);

  /**
   * Shows a dialog with some information for the user and a file chooser
   *
   * @param pMessage Message to inform the user about the purpose of the file to select
   * @return DialogResult with information such as "has the user pressed OK?" and the selected file as getMessage
   */
  @NotNull
  DialogResult showFileSelectionDialog(String pMessage);

  /**
   * Shows a dialog with settings that affect the git plugin
   *
   * @param pRepository Observable with the current Repository
   * @return DialogResult with information such as "has the user pressed OK?" and a MultiMap with the chosen settings as information
   */
  @NotNull
  DialogResult<GitConfigDialog, Multimap<String, Object>> showGitConfigDialog(@NotNull Observable<Optional<IRepository>> pRepository);

  /**
   * Shows a dialog that allows the user to set a commit message to be used for the stash commit
   *
   * @return DialogResult with information such as "has the user pressed OK?" and the "commit message" for the stash commit
   */
  @NotNull
  DialogResult<StashChangesDialog, StashChangesResult> showStashChangesDialog();

  /**
   * Shows a dialog for choosing a ssh key/passphrase combination
   *
   * @param pMessage        Message to display to the user
   * @param pSshKeyLocation Location of the ssh key if known, null otherwise
   * @param pPassphrase     passphrase if known for the key, null otherwise
   * @param pKeyStore       KeyStore used to store and retrieve passphrases for the ssh keys
   * @return DialogResult with information such as "has the user pressed OK?", ssh key location as message and the passphrase as char array in the information
   */
  @NotNull
  DialogResult<SshInfoPrompt, char[]> showSshInfoPromptDialog(@NotNull String pMessage, @Nullable String pSshKeyLocation, @Nullable char[] pPassphrase,
                                                              @NotNull IKeyStore pKeyStore);

  /**
   * Shows a dialog with an overview over all tags of the current repository
   *
   * @param pSelectecCommitCallback Consumer that selects the passed ICommit in the log
   * @param pRepository             Observable with the current Repository
   * @return DialogResult with information such as "has the user pressed OK?"
   */
  @NotNull
  DialogResult<TagOverviewDialog, Object> showTagOverviewDialog(@NotNull Consumer<ICommit> pSelectecCommitCallback,
                                                                @NotNull Observable<Optional<IRepository>> pRepository);
}
