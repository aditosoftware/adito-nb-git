package de.adito.git.gui.dialogs;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import io.reactivex.Observable;

import java.util.*;

/**
 * @author m.kaspera 26.10.2018
 */
interface IDialogFactory
{

  MergeConflictDialog createMergeConflictDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                Observable<Optional<IRepository>> pRepository, List<IMergeDiff> pMergeConflictDiffs);

  MergeConflictResolutionDialog createMergeConflictResolutionDialog(IMergeDiff pMergeDiff);

  CommitDialog createCommitDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                  Observable<Optional<IRepository>> pRepository, Observable<Optional<List<IFileChangeType>>> pFilesToCommit);

  DiffDialog createDiffDialog(List<IFileDiff> pDiffs);

  NewBranchDialog createNewBranchDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor, Observable<Optional<IRepository>> pRepository);

  ResetDialog createResetDialog();

  PushDialog createPushDialog(Observable<Optional<IRepository>> pRepository, List<ICommit> pCommitList);

  StashedCommitSelectionDialog createStashedCommitSelectionDialog(IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                                                  Observable<Optional<IRepository>> pRepository, List<ICommit> pStashedCommits);

  PasswordPromptDialog createPasswordPromptDialog();

  UserPromptDialog createUserPromptDialog();

  YesNoDialog createYesNoDialog(String pMessage);

  GitConfigDialog createGitConfigDialog(Observable<Optional<IRepository>> pRepository);
}
