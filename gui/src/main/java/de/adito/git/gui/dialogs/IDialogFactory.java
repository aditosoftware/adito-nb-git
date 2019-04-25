package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 26.10.2018
 */
interface IDialogFactory
{

  MergeConflictDialog createMergeConflictDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs);

  MergeConflictResolutionDialog createMergeConflictResolutionDialog(IMergeDiff pMergeDiff);

  CommitDialog createCommitDialog(@NotNull IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                  @NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                  @NotNull String pMessageTemplate);

  DiffDialog createDiffDialog(@NotNull List<IFileDiff> pDiffs, @Nullable String pSelectedFile, @Assisted("acceptChange") boolean pAcceptChange,
                              @Assisted("showFileTable") boolean pShowFileTable);

  NewBranchDialog createNewBranchDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor, Observable<Optional<IRepository>> pRepository);

  ResetDialog createResetDialog();

  PushDialog createPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  StashedCommitSelectionDialog createStashedCommitSelectionDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                                  Observable<Optional<IRepository>> pRepository, List<ICommit> pStashedCommits);

  PasswordPromptDialog createPasswordPromptDialog();

  UserPromptDialog createUserPromptDialog(@Nullable String pDefault);

  YesNoDialog createYesNoDialog(String pMessage);

  DeleteBranchDialog createDeleteBranchDialog();

  GitConfigDialog createGitConfigDialog(Observable<Optional<IRepository>> pRepository);

  StashChangesDialog createStashChangesDialog();

  FileSelectionDialog createFileSelectionDialog();

  RevertFilesDialog createRevertDialog(List<IFileChangeType> pFilesToRevert, File pProjectDirectory);

  SshInfoPrompt createSshInfoPromptDialog(@Assisted("message") String pMessage, @javax.annotation.Nullable @Assisted("keyLocation") String pSshKeyLocation,
                                          @javax.annotation.Nullable char[] pPassphrase, @javax.annotation.Nullable IKeyStore pKeyStore);
}
