package de.adito.git.gui.window.content;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.table.TableModel;

/**
 * @author m.kaspera 29.10.2018
 */
interface IWindowContentFactory {

    StatusWindowContent createStatusWindowContent(Observable<IRepository> pRepository);

    BranchListWindowContent createBranchListWindowContent(Observable<IRepository> pRepository);

    CommitHistoryWindowContent createCommitHistoryWindowContent(Observable<IRepository> pRepository, TableModel pTableModel, Runnable pLoadMoreCallback);

    StatusLineWindowContent createStatusLineWindowContent(Observable<IRepository> pRepository);

}
