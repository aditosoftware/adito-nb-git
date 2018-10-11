package de.adito.git.gui;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.actions.*;
import de.adito.git.gui.tableModels.StatusTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel implements IStatusWindow {

    private IFileStatus status;
    private IRepository repository;
    private IDialogDisplayer dialogDisplayer;
    private JTable statusTable;

    @Inject
    public StatusWindow(IDialogDisplayer pDialogDisplayer, RepositoryProvider repository) {
        dialogDisplayer = pDialogDisplayer;
        this.repository = repository.getRepositoryImpl();
        status = this.repository.status();
        _initGui();
    }

    private void _initGui() {
        setLayout(new BorderLayout());
        statusTable = new JTable(new StatusTableModel(status));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // CellRenderer so the files are colored according to their EChangeType
        for (int index = 0; index < statusTable.getColumnModel().getColumnCount(); index++) {
            statusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }

        _initPopupMenu(statusTable);
        add(statusTable, BorderLayout.CENTER);
    }

    /**
     * @param pStatusTable JTable for which to set up the popup menu
     */
    private void _initPopupMenu(JTable pStatusTable) {
        List<AbstractTableAction> actionList = new ArrayList<>();
        actionList.add(new CommitAction(dialogDisplayer, statusTable, repository, status));
        actionList.add(new AddAction(statusTable, repository, status));
        actionList.add(new IgnoreAction(statusTable, repository, status));
        actionList.add(new ExcludeAction(statusTable, repository, status));
        TablePopupMenu tablePopupMenu = new TablePopupMenu(pStatusTable, actionList);
        tablePopupMenu.activateMouseListener();

    }

}
