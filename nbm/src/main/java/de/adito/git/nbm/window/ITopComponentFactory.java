package de.adito.git.nbm.window;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import io.reactivex.Observable;

import java.util.List;

/**
 * An interface to create all topComponents in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
interface ITopComponentFactory {

    AllBranchTopComponent createAllBranchTopComponent(Observable<IRepository> pRepository);

    CommitHistoryTopComponent createCommitHistoryTopComponent(Observable<IRepository> pRepository, List<ICommit> pCommits, String pDisplayableContext);

    StatusWindowTopComponent createStatusWindowTopComponent(Observable<IRepository> pRepository);

}
