package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  CommitDialog createCommitDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                  Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                                  String pMessageTemplate);

  DiffDialog createDiffDialog(@NotNull List<IFileDiff> pDiffs, @Nullable String pSelectedFile, @Assisted("acceptChange") boolean pAcceptChange,
                              @Assisted("showFileTable") boolean pShowFileTable);

  NewBranchDialog createNewBranchDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor, Observable<Optional<IRepository>> pRepository);

  ResetDialog createResetDialog();

  PushDialog createPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  StashedCommitSelectionDialog createStashedCommitSelectionDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                                  Observable<Optional<IRepository>> pRepository, List<ICommit> pStashedCommits);

  PasswordPromptDialog createPasswordPromptDialog();

  UserPromptDialog createUserPromptDialog();

  YesNoDialog createYesNoDialog(String pMessage);

  GitConfigDialog createGitConfigDialog(Observable<Optional<IRepository>> pRepository);

  StashChangesDialog createStashChangesDialog();

  FileSelectionDialog createFileSelectionDialog();
}
