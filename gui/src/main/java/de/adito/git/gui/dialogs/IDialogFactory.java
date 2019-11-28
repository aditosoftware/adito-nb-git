package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author m.kaspera 26.10.2018
 */
interface IDialogFactory
{

  MergeConflictDialog createMergeConflictDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs, boolean pOnlyConflicting);

  MergeConflictResolutionDialog createMergeConflictResolutionDialog(IMergeDiff pMergeDiff);

  CommitDialog createCommitDialog(@NotNull IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                  @NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                  @NotNull String pMessageTemplate);

  DiffDialog createDiffDialog(@NotNull File pProjectDirectory, @NotNull List<IFileDiff> pDiffs, @Nullable String pSelectedFile,
                              @Assisted("acceptChange") boolean pAcceptChange, @Assisted("showFileTree") boolean pShowFileTree);

  NewBranchDialog createNewBranchDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor, Observable<Optional<IRepository>> pRepository);

  ResetDialog createResetDialog();

  PushDialog createPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  StashedCommitSelectionDialog createStashedCommitSelectionDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                                  Observable<Optional<IRepository>> pRepository, List<ICommit> pStashedCommits);

  PasswordPromptDialog createPasswordPromptDialog();

  UserPromptDialog createUserPromptDialog(@Nullable String pDefault);

  NotificationDialog createNotificationDialog(String pMessage);

  DeleteBranchDialog createDeleteBranchDialog();

  GitConfigDialog createGitConfigDialog(Observable<Optional<IRepository>> pRepository);

  StashChangesDialog createStashChangesDialog();

  FileSelectionDialog createFileSelectionDialog(String pLabel, FileChooserProvider.FileSelectionMode pFileSelectionMode,
                                                @javax.annotation.Nullable FileFilter pFileFilter);

  RevertFilesDialog createRevertDialog(List<IFileChangeType> pFilesToRevert, File pProjectDirectory);

  SshInfoPrompt createSshInfoPromptDialog(@Assisted("message") String pMessage, @javax.annotation.Nullable @Assisted("keyLocation") String pSshKeyLocation,
                                          @javax.annotation.Nullable char[] pPassphrase, @javax.annotation.Nullable IKeyStore pKeyStore);

  CheckboxPrompt createCheckboxPrompt(@Assisted("message") String pMessage, @Assisted("checkbox") String pCheckboxText);

  TagOverviewDialog createTagOverviewDialog(Consumer<ICommit> pSelectedCommitCallback, Observable<Optional<IRepository>> pRepository);

  ComboBoxDialog<Object> createComboBoxDialog(String pMessage, List<Object> pOptions);

  StashChangesQuestionDialog createStashChangesQuestionDialog(List<IFileChangeType> pFilesToRevert, File pProjectDir);
}
