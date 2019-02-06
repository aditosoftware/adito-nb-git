package de.adito.git.gui.window.content;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.Optional;

/**
 * @author a.arnold, 31.10.2018
 */
public interface IWindowContentProvider
{

  JComponent createStatusWindowContent(Observable<Optional<IRepository>> pRepository);

  JComponent createBranchListWindowContent(Observable<Optional<IRepository>> pRepository);

  JComponent createCommitHistoryWindowContent(Observable<Optional<IRepository>> pRepository, TableModel pTableModel, Runnable pLoadMoreCallback,
                                              Runnable pRefreshContent);

  JComponent createStatusLineWindowContent(Observable<Optional<IRepository>> pRepository);

}
