package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.FileStatusCellRenderer;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.CommitHistoryTreeListTableModel;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class to display all commits
 *
 * @author A.Arnold 01.10.2018
 */
class CommitHistoryWindowContent extends JPanel implements IDiscardable {

    private final static int SCROLL_SPEED_INCREMENT = 16;
    private final static int BRANCHING_AREA_PREF_WIDTH = 1600;
    private final static int DATE_COL_PREF_WIDTH = 250;
    private final static int AUTHOR_COL_PREF_WIDTH = 160;
    private final static double MAIN_SPLIT_PANE_SIZE_RATIO = 0.75;
    private final static double DETAIL_SPLIT_PANE_RATIO = 0.5;
    private final JTable commitTable = new JTable();
    private final IActionProvider actionProvider;
    private final Observable<Optional<IRepository>> repository;
    private final Observable<Optional<List<ICommit>>> selectionObservable;
    private final _ChangedFilesTableModel changedFilesTableModel;
    private Disposable disposable;
    private JPopupMenu popupMenu;

    @Inject
    CommitHistoryWindowContent(IActionProvider pActionProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                               @Assisted TableModel pTableModel, @Assisted Runnable pLoadMoreCallback) {
        ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(commitTable.getSelectionModel());
        commitTable.setSelectionModel(observableListSelectionModel);
        actionProvider = pActionProvider;
        repository = pRepository;
        commitTable.setModel(pTableModel);
        selectionObservable = observableListSelectionModel.selectedRows().map(selectedRows -> {
            List<ICommit> selectedCommits = new ArrayList<>();
            for (int selectedRow : selectedRows) {
                selectedCommits.add(((CommitHistoryTreeListItem) commitTable.getValueAt(selectedRow, 0)).getCommit());
            }
            return Optional.of(selectedCommits);
        });
        changedFilesTableModel = new _ChangedFilesTableModel(selectionObservable, repository);
        _initGUI(pLoadMoreCallback);
    }

    private void _initGUI(Runnable pLoadMoreCallback) {
        setLayout(new BorderLayout());
        commitTable.setDefaultRenderer(CommitHistoryTreeListItem.class, new CommitHistoryTreeListItemRenderer());

        popupMenu = new JPopupMenu();
        popupMenu.add(actionProvider.getResetAction(repository, selectionObservable));

        commitTable.addMouseListener(new PopupMouseListener(popupMenu));

        // cannot set preferred width of only last columns, so have to set a width for the first one as well
        // since the total width is not know the width for the first one has to be a guess that works for most screens
        // and makes it so the last two columns get approx. the desired space (less if guess is too high, more if guess is too low)
        commitTable.getColumnModel()
                .getColumn(CommitHistoryTreeListTableModel.getColumnIndex(CommitHistoryTreeListTableModel.BRANCHING_COL_NAME))
                .setPreferredWidth(BRANCHING_AREA_PREF_WIDTH);
        commitTable.getColumnModel()
                .getColumn(CommitHistoryTreeListTableModel.getColumnIndex(CommitHistoryTreeListTableModel.DATE_COL_NAME))
                .setPreferredWidth(DATE_COL_PREF_WIDTH);
        commitTable.getColumnModel()
                .getColumn(CommitHistoryTreeListTableModel.getColumnIndex(CommitHistoryTreeListTableModel.AUTHOR_COL_NAME))
                .setPreferredWidth(AUTHOR_COL_PREF_WIDTH);

        JScrollPane commitScrollPane = new JScrollPane(commitTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Listener on the vertical scrollbar to check if the user has reached the bottom. If that is the case, load the next batch of commits into the list
        commitScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            // check if the scrollBar is still being dragged
            if (!e.getValueIsAdjusting()) {
                JScrollBar scrollBar = (JScrollBar) e.getAdjustable();
                int extent = scrollBar.getModel().getExtent();
                int maximum = scrollBar.getModel().getMaximum();
                if (extent + e.getValue() == maximum) {
                    pLoadMoreCallback.run();
                }
            }
        });
        commitScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, commitScrollPane, _initDetailPanel());
        mainSplitPane.setResizeWeight(MAIN_SPLIT_PANE_SIZE_RATIO);
        add(mainSplitPane);
    }

    /**
     * DetailPanel shows the changed files and the short message, author, commit date and full message of the
     * currently selected commit to the right of the branching window of the commitHistory.
     * If more than one commit is selected, the first selected commit in the list is chosen
     *
     * @return JSplitPane with the components of the DetailPanel laid out and initialised
     */
    private JComponent _initDetailPanel() {
        /*
        ------------------------------
        | -------------------------- |
        | |  Table with scrollPane | |
        | -------------------------- |
        |         SplitPane          |
        | -------------------------- |
        | |  TextArea with detail  | |
        | |  message in scrollPane | |
        | -------------------------- |
        ------------------------------
         */
        JTextArea messageTextArea = new JTextArea();
        JTable changedFilesTable = new JTable(changedFilesTableModel);
        changedFilesTable.getColumnModel().removeColumn(changedFilesTable.getColumn(StatusTableModel.CHANGE_TYPE_COLUMN_NAME));
        changedFilesTable.getColumnModel()
                .getColumn(changedFilesTableModel.findColumn(_ChangedFilesTableModel.FILE_NAME_COLUMN_NAME))
                .setCellRenderer(new FileStatusCellRenderer());
        changedFilesTable.getColumnModel()
                .getColumn(changedFilesTableModel.findColumn(_ChangedFilesTableModel.FILE_PATH_COLUMN_NAME))
                .setCellRenderer(new FileStatusCellRenderer());
        disposable = selectionObservable.subscribe(pCommits -> pCommits.ifPresent(iCommits -> messageTextArea.setText(_getDescriptionText(iCommits))));
        JScrollPane messageTextScrollPane = new JScrollPane(messageTextArea);
        JScrollPane changedFilesScrollPane = new JScrollPane(changedFilesTable);
        JSplitPane detailPanelSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, changedFilesScrollPane, messageTextScrollPane);
        detailPanelSplitPane.setResizeWeight(DETAIL_SPLIT_PANE_RATIO);
        return detailPanelSplitPane;
    }

    @Override
    public void discard() {
        disposable.dispose();
    }

    private String _getDescriptionText(List<ICommit> pCommits) {
        if (pCommits.isEmpty())
            return "";
        return pCommits.get(0).getShortMessage() +
                "\n\nAuthor: " +
                pCommits.get(0).getAuthor() +
                "\n\nDate: " +
                pCommits.get(0).getTime().toString() +
                "\n\nDetailed message:\n" +
                pCommits.get(0).getMessage();
    }

    /**
     * TableModel for displaying a the list of changed files with their name and absolute path
     *
     * @author M.Kaspera 03.12.2018
     */
    private static class _ChangedFilesTableModel extends AbstractTableModel implements IDiscardable {

        final static String FILE_NAME_COLUMN_NAME = StatusTableModel.FILE_NAME_COLUMN_NAME;
        final static String FILE_PATH_COLUMN_NAME = StatusTableModel.FILE_PATH_COLUMN_NAME;
        final static String CHANGE_TYPE_COLUMN_NAME = StatusTableModel.CHANGE_TYPE_COLUMN_NAME;
        final static String[] columnNames = {FILE_NAME_COLUMN_NAME, FILE_PATH_COLUMN_NAME, CHANGE_TYPE_COLUMN_NAME};
        private Disposable disposable;
        private List<IFileChangeType> changedFiles = new ArrayList<>();

        _ChangedFilesTableModel(Observable<Optional<List<ICommit>>> pSelectedCommits, Observable<Optional<IRepository>> pRepo) {
            Optional<IRepository> currentRepo = pRepo.blockingFirst();
            disposable = pSelectedCommits.subscribe(pSelectedCommitsOpt -> {
                if (pSelectedCommitsOpt.isPresent() && pSelectedCommitsOpt.get().size() > 0 && currentRepo.isPresent())
                    changedFiles = currentRepo.get().getCommittedFiles(pSelectedCommitsOpt.get().get(0).getId());
                else
                    changedFiles = Collections.emptyList();
                fireTableDataChanged();
            });
        }

        @Override
        public int findColumn(String columnName) {
            for (int index = 0; index < columnName.length(); index++) {
                if (columnNames[index].equals(columnName)) {
                    return index;
                }
            }
            return -1;
        }

        @Override
        public String getColumnName(int column) {
            if (column == findColumn(FILE_NAME_COLUMN_NAME))
                return FILE_NAME_COLUMN_NAME;
            if (column == findColumn(FILE_PATH_COLUMN_NAME))
                return FILE_PATH_COLUMN_NAME;
            if (column == findColumn(CHANGE_TYPE_COLUMN_NAME))
                return CHANGE_TYPE_COLUMN_NAME;
            else return "";
        }

        @Override
        public int getRowCount() {
            return changedFiles.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == findColumn(FILE_NAME_COLUMN_NAME))
                return changedFiles.get(rowIndex).getFile().getName();
            if (columnIndex == findColumn(FILE_PATH_COLUMN_NAME))
                return changedFiles.get(rowIndex).getFile().getAbsolutePath();
            if (columnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
                return changedFiles.get(rowIndex).getChangeType();
            else return changedFiles.get(rowIndex);
        }

        @Override
        public void discard() {
            disposable.dispose();
        }
    }
}
