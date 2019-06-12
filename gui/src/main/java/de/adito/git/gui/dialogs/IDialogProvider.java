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

  DialogResult showMergeConflictDialog(Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs);

  DialogResult showMergeConflictResolutionDialog(IMergeDiff pMergeDiff);

  DialogResult showDiffDialog(@NotNull List<IFileDiff> pFileDiffs, @Nullable String pSelectedFile, boolean pAcceptChange, boolean pShowFileTable);

  DialogResult<CommitDialog, CommitDialogResult> showCommitDialog(@NotNull Observable<Optional<IRepository>> pRepository,
                                                                  @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                                                  @NotNull String pMessageTemplate);

  DialogResult showNewBranchDialog(Observable<Optional<IRepository>> pRepository);

  DialogResult<ResetDialog, EResetType> showResetDialog();

  DialogResult<PushDialog, Boolean> showPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  DialogResult<StashedCommitSelectionDialog, String> showStashedCommitSelectionDialog(Observable<Optional<IRepository>> pRepository,
                                                                                      List<ICommit> pStashedCommits);

  DialogResult<PasswordPromptDialog, char[]> showPasswordPromptDialog(String pMessage);

  DialogResult showUserPromptDialog(@NotNull String pMessage, @Nullable String pDefaultValue);

  DialogResult showYesNoDialog(@NotNull String pMessage);

  DialogResult showRevertDialog(@NotNull List<IFileChangeType> pFilesToRevert, @NotNull File pProjectDir);

  DialogResult<DeleteBranchDialog, Boolean> showDeleteBranchDialog(String pBranchName);

  DialogResult showFileSelectionDialog(String pMessage);

  DialogResult<GitConfigDialog, Multimap<String, Object>> showGitConfigDialog(Observable<Optional<IRepository>> pRepository);

  DialogResult<StashChangesDialog, StashChangesResult> showStashChangesDialog();

  DialogResult<SshInfoPrompt, char[]> showSshInfoPromptDialog(String pMessage, String pSshKeyLocation, char[] pPassphrase, IKeyStore pKeyStore);

  DialogResult<TagOverviewDialog, Object> showTagOverviewDialog(Consumer<ICommit> pSelectecCommitCallback, Observable<Optional<IRepository>> pRepository);
}
