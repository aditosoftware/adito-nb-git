package de.adito.git.nbm.window;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.table.TableModel;

/**
 * An interface to create all topComponents in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
interface ITopComponentFactory {

    AllBranchTopComponent createAllBranchTopComponent(Observable<IRepository> pRepository);

    CommitHistoryTopComponent createCommitHistoryTopComponent(Observable<IRepository> pRepository, TableModel tableModel, Runnable loadMoreCallback, String pDisplayableContext);

    StatusWindowTopComponent createStatusWindowTopComponent(Observable<IRepository> pRepository);

}
