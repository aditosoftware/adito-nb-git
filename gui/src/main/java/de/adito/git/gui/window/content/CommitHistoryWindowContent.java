package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.CommitHistoryTreeListItem;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.CommitListTableModel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to display all commits
 *
 * @author A.Arnold 01.10.2018
 */
class CommitHistoryWindowContent extends JPanel {

    private final static int SCROLL_SPEED_INCREMENT = 16;
    private final JTable commitTable = new JTable();
    private final IActionProvider actionProvider;
    private final Observable<IRepository> repository;
    private final Observable<List<ICommit>> selectionObservable;
    private JPopupMenu popupMenu;

    @Inject
    CommitHistoryWindowContent(IActionProvider pActionProvider, @Assisted Observable<IRepository> pRepository, @Assisted List<ICommit> pCommits) {
        ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(commitTable.getSelectionModel());
        commitTable.setSelectionModel(observableListSelectionModel);
        actionProvider = pActionProvider;
        repository = pRepository;
        selectionObservable = observableListSelectionModel.selectedRows().map(selectedRows -> {
            List<ICommit> selectedCommits = new ArrayList<>();
            for (int selectedRow : selectedRows) {
                selectedCommits.add(pCommits.get(selectedRow));
            }
            return selectedCommits;
        });
        _initGUI(pCommits);
    }

    private void _initGUI(List<ICommit> pCommits) {
        setLayout(new BorderLayout());
        commitTable.setModel(new CommitListTableModel(repository.blockingFirst().getCommitHistoryTreeList(pCommits)));
        commitTable.setDefaultRenderer(CommitHistoryTreeListItem.class, new CommitHistoryTreeListItemRenderer());
        commitTable.getColumnModel().getColumn(0).setMinWidth(CommitHistoryTreeListItem.getMaxWidth() * 20 + 10);
        commitTable.getColumnModel().getColumn(0).setMaxWidth(CommitHistoryTreeListItem.getMaxWidth() * 20 + 10);
        commitTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        commitTable.getTableHeader().setReorderingAllowed(false);

        popupMenu = new JPopupMenu();
        popupMenu.add(actionProvider.getResetAction(repository, selectionObservable));

        commitTable.addMouseListener(new PopupMouseListener(popupMenu));

        JScrollPane commitScrollPane = new JScrollPane(commitTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        commitScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        commitScrollPane.setPreferredSize(new Dimension(800, 300));
        add(commitScrollPane, BorderLayout.CENTER);
    }
}
