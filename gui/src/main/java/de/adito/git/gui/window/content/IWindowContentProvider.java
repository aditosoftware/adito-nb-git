package de.adito.git.gui.window.content;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * @author a.arnold, 31.10.2018
 */
public interface IWindowContentProvider {

    JComponent createStatusWindowContent(Observable<IRepository> pRepository);

    JComponent createBranchListWindowContent(Observable<IRepository> pRepository);

    JComponent createCommitHistoryWindowContent(Observable<IRepository> pRepository, TableModel pTableModel, Runnable pLoadMoreCallback);

    JComponent createStatusLineWindowContent(Observable<IRepository> pRepository);

}
