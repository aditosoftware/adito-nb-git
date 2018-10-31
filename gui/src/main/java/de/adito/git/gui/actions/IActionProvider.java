package de.adito.git.gui.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.IFileChangeType;
import io.reactivex.Observable;

import javax.swing.*;
import java.util.List;

/**
 * @author m.kaspera 26.10.2018
 */
public interface IActionProvider {

    Action getMergeAction(Observable<IRepository> pRepository, String pCurrentBranch, String pTargetBranch);

    Action getCommitAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    Action getDiffAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    Action getIgnoreAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    Action getAddAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    Action getExcludeAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    Action getCheckoutAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranchList);

    Action getNewBranchAction(Observable<IRepository> pRepository);

    Action getPullAction(Observable<IRepository> pRepository, String pTargetId);

    Action getPushAction(Observable<IRepository> pRepository);

    Action getShowAllBranchesAction(Observable<IRepository> pRepository);

    Action getShowAllCommitsAction(Observable<IRepository> pRepository, Observable<List<IBranch>> pBranches);

    Action getRevertWorkDirAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

    Action getResetFilesAction(Observable<IRepository> pRepository, Observable<List<IFileChangeType>> pSelectedFilesObservable);

}
