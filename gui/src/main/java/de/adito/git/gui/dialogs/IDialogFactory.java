package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import io.reactivex.rxjava3.core.Observable;
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

  MergeConflictDialog createMergeConflictDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor, Observable<Optional<IRepository>> pRepository,
                                                IMergeDetails pMergeDetails, @Assisted("onlyConflictingFlag") boolean pOnlyConflicting,
                                                @Assisted("autoResolveFlag") boolean pShowAutoResolve);

  MergeConflictResolutionDialog createMergeConflictResolutionDialog(IMergeData pMergeDiff, @Assisted("yoursOrigin") String pYoursOrigin,
                                                                    @Assisted("theirsOrigin") String pTheirsOrigin);

  CommitDialog createCommitDialog(@NotNull IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                  @NotNull Observable<Optional<IRepository>> pRepository, @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                  @NotNull String pMessageTemplate);

  DiffDialog createDiffDialog(@NotNull File pProjectDirectory, @NotNull List<IFileDiff> pDiffs, @Assisted("selectedFile") @Nullable String pSelectedFile,
                              @Assisted("leftHeader") @Nullable String pLeftHeader, @Assisted("rightHeader") @Nullable String pRightHeader,
                              @Assisted("acceptChange") boolean pAcceptChange, @Assisted("showFileTree") boolean pShowFileTree);

  NewBranchDialog createNewBranchDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor, Observable<Optional<IRepository>> pRepository);

  ResetDialog createResetDialog();

  PushDialog createPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  StashedCommitSelectionDialog createStashedCommitSelectionDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                                  Observable<Optional<IRepository>> pRepository, List<ICommit> pStashedCommits);

  PasswordPromptDialog createPasswordPromptDialog();

  GitConfigDialog createGitConfigDialog(Observable<Optional<IRepository>> pRepository);

  StashChangesDialog createStashChangesDialog();

  FileSelectionDialog createFileSelectionDialog(String pLabel, FileChooserProvider.FileSelectionMode pFileSelectionMode,
                                                @javax.annotation.Nullable FileFilter pFileFilter);

  RevertFilesDialog createRevertDialog(Observable<Optional<IRepository>> pRepositoryObs, List<IFileChangeType> pFilesToRevert, File pProjectDirectory);

  SshInfoPrompt createSshInfoPromptDialog(@Assisted("message") String pMessage, @javax.annotation.Nullable @Assisted("keyLocation") String pSshKeyLocation,
                                          @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor, @javax.annotation.Nullable char[] pPassphrase,
                                          @javax.annotation.Nullable IKeyStore pKeyStore);

  TagOverviewDialog createTagOverviewDialog(Consumer<ICommit> pSelectedCommitCallback, Observable<Optional<IRepository>> pRepository);

  StashChangesQuestionDialog createStashChangesQuestionDialog(Observable<Optional<IRepository>> pRepositoryObs, List<IFileChangeType> pFilesToRevert, File pProjectDir);
}
