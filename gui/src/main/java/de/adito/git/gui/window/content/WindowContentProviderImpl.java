package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * @author a.arnold, 31.10.2018
 */
public class WindowContentProviderImpl implements IWindowContentProvider {

    private final IWindowContentFactory windowContentFactory;

    @Inject
    public WindowContentProviderImpl(IWindowContentFactory pWindowContentFactory) {
        windowContentFactory = pWindowContentFactory;
    }

    @Override
    public JComponent createStatusWindowContent(Observable<IRepository> pRepository) {
        return windowContentFactory.createStatusWindowContent(pRepository);
    }

    @Override
    public JComponent createBranchListWindowContent(Observable<IRepository> pRepository) {
        return windowContentFactory.createBranchListWindowContent(pRepository);
    }

    @Override
    public JComponent createCommitHistoryWindowContent(Observable<IRepository> pRepository, TableModel pTableModel, Runnable pLoadMoreCallback) {
        return windowContentFactory.createCommitHistoryWindowContent(pRepository, pTableModel, pLoadMoreCallback);
    }

    @Override
    public JComponent createStatusLineWindowContent(Observable<IRepository> pRepository){
        return windowContentFactory.createStatusLineWindowContent(pRepository);
    }

}
