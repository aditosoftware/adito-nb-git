package de.adito.git.gui.window.content;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.table.TableModel;
import java.util.Optional;

/**
 * @author m.kaspera 29.10.2018
 */
interface IWindowContentFactory {

    StatusWindowContent createStatusWindowContent(Observable<Optional<IRepository>> pRepository);

    BranchListWindowContent createBranchListWindowContent(Observable<Optional<IRepository>> pRepository);

    CommitHistoryWindowContent createCommitHistoryWindowContent(Observable<Optional<IRepository>> pRepository, TableModel pTableModel, Runnable pLoadMoreCallback);

    StatusLineWindowContent createStatusLineWindowContent(Observable<Optional<IRepository>> pRepository);

}
