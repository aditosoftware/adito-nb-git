package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.FileStatusCellRenderer;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Commit window
 *
 * @author m.kaspera 04.10.2018
 */
class CommitDialog extends JPanel {

    private final Runnable enableOk;
    private final Runnable disableOK;
    private final JTable fileStatusTable = new JTable();
    private final _CommitTableModel commitTableModel;
    private final JEditorPane messagePane = new JEditorPane();
    private final JCheckBox amendCheckBox = new JCheckBox("amend commit");

    @Inject
    public CommitDialog(@Assisted("enable") Runnable pEnableOk, @Assisted("disable") Runnable pDisableOK,
                        @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IFileChangeType>>> pFilesToCommit) {
        enableOk = pEnableOk;
        disableOK = pDisableOK;
        Observable<IFileStatus> statusObservable = pRepository.flatMap(pRepo -> pRepo.orElseThrow(() -> new RuntimeException("no valid repository found")).getStatus());
        Observable<List<_SelectedFileChangeType>> filesToCommitObservable = Observable.combineLatest(
                statusObservable, pFilesToCommit, (pStatusObservable, pSelectedFiles) -> pStatusObservable.getUncommitted()
                        .stream()
                        .map(pUncommitted -> new _SelectedFileChangeType(pSelectedFiles.orElse(Collections.emptyList()).contains(pUncommitted), pUncommitted)).collect(Collectors.toList()));
        commitTableModel = new _CommitTableModel(filesToCommitObservable);
        fileStatusTable.setModel(commitTableModel);
        _initGui();
    }

    String getMessageText() {
        return messagePane.getText();
    }

    Supplier<List<IFileChangeType>> getFilesToCommit() {
        return () -> commitTableModel.fileList
                .stream()
                .filter(pSelectedFileChangeType -> pSelectedFileChangeType.isSelected)
                .map(_SelectedFileChangeType::getChangeType)
                .collect(Collectors.toList());
    }

    /**
     * initialise GUI elements
     */
    private void _initGui() {
        setPreferredSize(new Dimension(1200, 800));
        setLayout(new BorderLayout());
        fileStatusTable.setSelectionModel(new ObservableListSelectionModel(fileStatusTable.getSelectionModel()));
        fileStatusTable.getColumnModel().getColumn(commitTableModel.findColumn(_CommitTableModel.IS_SELECTED_COLUMN_NAME)).setMaxWidth(25);
        fileStatusTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Hide the status column from view, but leave the data (retrieved via table.getModel.getValueAt)
        fileStatusTable.getColumnModel().removeColumn(fileStatusTable.getColumn(_CommitTableModel.CHANGE_TYPE_COLUMN_NAME));
        // Set Renderer for cells so they are colored according to their EChangeType
        for (int index = 1; index < fileStatusTable.getColumnModel().getColumnCount(); index++) {
            _setColumnSize(index);
            fileStatusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }
        // Size for the Table with the list of files to commit
        JScrollPane tableScrollPane = new JScrollPane(fileStatusTable);
        // EditorPane for the Commit message
        messagePane.setMinimumSize(new Dimension(200, 200));
        messagePane.setPreferredSize(new Dimension(450, 750));
        messagePane.setBorder(tableScrollPane.getBorder());
        // Listener for enabling/disabling the OK button
        messagePane.getDocument().addDocumentListener(new _EmptyDocumentListener());
        JPanel messageOptionsPanel = new JPanel(new BorderLayout());
        messageOptionsPanel.add(messagePane, BorderLayout.CENTER);
        messageOptionsPanel.add(amendCheckBox, BorderLayout.SOUTH);
        messageOptionsPanel.setBorder(tableScrollPane.getBorder());
        // Splitpane so the user can choose how big each element should be
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, messageOptionsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void _setColumnSize(int columnNum) {
        FontMetrics fontMetrics = fileStatusTable.getFontMetrics(fileStatusTable.getFont());
        int currentWidth, maxWidth = 0;
        for (int index = 0; index < fileStatusTable.getModel().getRowCount(); index++) {
            currentWidth = fontMetrics.stringWidth(fileStatusTable.getModel().getValueAt(index, columnNum).toString());
            if (currentWidth > maxWidth)
                maxWidth = currentWidth;
        }
        fileStatusTable.getColumnModel().getColumn(columnNum).setPreferredWidth(maxWidth);
    }

    private class _SelectedFileChangeType {

        private final IFileChangeType changeType;
        private boolean isSelected;

        _SelectedFileChangeType(boolean pIsSelected, IFileChangeType pChangeType) {
            isSelected = pIsSelected;
            changeType = pChangeType;
        }

        public IFileChangeType getChangeType() {
            return changeType;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean pIsSelected) {
            isSelected = pIsSelected;
        }
    }

    /**
     * TableModel for the Table, has the list of files to commit. Similar to {@link StatusTableModel}
     */
    private static class _CommitTableModel extends AbstractTableModel implements IDiscardable {

        final static String IS_SELECTED_COLUMN_NAME = "commit file";
        final static String FILE_NAME_COLUMN_NAME = StatusTableModel.FILE_NAME_COLUMN_NAME;
        final static String FILE_PATH_COLUMN_NAME = StatusTableModel.FILE_PATH_COLUMN_NAME;
        final static String CHANGE_TYPE_COLUMN_NAME = StatusTableModel.CHANGE_TYPE_COLUMN_NAME;
        final static String[] columnNames = {IS_SELECTED_COLUMN_NAME, FILE_NAME_COLUMN_NAME, FILE_PATH_COLUMN_NAME, CHANGE_TYPE_COLUMN_NAME};

        private Disposable disposable;
        private List<_SelectedFileChangeType> fileList;

        _CommitTableModel(Observable<List<_SelectedFileChangeType>> pSelectedFileChangeTypes) {
            disposable = pSelectedFileChangeTypes.subscribe(pFilesToCommit -> fileList = pFilesToCommit);
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
            return column == 0 ? "" : columnNames[column];
        }

        @Override
        public int getRowCount() {
            return fileList.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == findColumn(IS_SELECTED_COLUMN_NAME))
                return Boolean.class;
            else if (columnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
                return String.class;
            else
                return String.class;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == findColumn(IS_SELECTED_COLUMN_NAME) && aValue instanceof Boolean)
                fileList.get(rowIndex).setSelected((Boolean) aValue);
            else
                super.setValueAt(aValue, rowIndex, columnIndex);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == findColumn(IS_SELECTED_COLUMN_NAME)) {
                return fileList.get(rowIndex).isSelected;
            } else if (columnIndex == findColumn(FILE_NAME_COLUMN_NAME)) {
                return fileList.get(rowIndex).getChangeType().getFile().getName();
            } else if (columnIndex == findColumn(FILE_PATH_COLUMN_NAME)) {
                return fileList.get(rowIndex).getChangeType().getFile().getPath();
            } else if (columnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME)) {
                return fileList.get(rowIndex).getChangeType().getChangeType();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == findColumn(IS_SELECTED_COLUMN_NAME);
        }

        @Override
        public void discard() {
            disposable.dispose();
        }
    }

    /**
     * Listen to document changes, disable the OK button if there is no text written by the user
     */
    private class _EmptyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (e.getDocument().getLength() == 0) {
                disableOK.run();
            } else {
                enableOk.run();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (e.getDocument().getLength() == 0) {
                disableOK.run();
            } else {
                enableOk.run();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (e.getDocument().getLength() == 0) {
                disableOK.run();
            } else {
                enableOk.run();
            }
        }
    }

}


