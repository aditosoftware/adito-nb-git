package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.actions.*;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel {

    private Observable<IFileStatus> status;
    private Observable<IRepository> repository;
    private IDialogDisplayer dialogDisplayer;
    private JTable statusTable;
    private JPopupMenu popupMenu;

    public StatusWindow(IDialogDisplayer pDialogDisplayer, RepositoryProvider repository) {
        dialogDisplayer = pDialogDisplayer;
        this.repository = repository.getRepositoryImpl();
        repository.getRepositoryImpl().subscribe(pRepo -> {
            status = pRepo.getStatus();
        });

//        repository.getRepositoryImpl()
//                .flatMap(IRepository::status)
//                .subscribe(pStatus -> {
//                    System.out.println(pStatus);
//                })

//        repository.getRepositoryImpl()
//                .map(IRepository::status)
//                .map(StatusTableModel::new)
//                .subscribe(pModel -> statusTable.setModel(pModel));

        _initGui();
    }

    private void _initGui() {
        setLayout(new BorderLayout());
        statusTable = new JTable();
        status.map(StatusTableModel::new).subscribe(pModel -> statusTable.setModel(pModel));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // CellRenderer so the files are colored according to their EChangeType
        for (int index = 0; index < statusTable.getColumnModel().getColumnCount(); index++) {
            statusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }

        _SelectionSupplier selectionSupplier = new _SelectionSupplier();
        repository.subscribe(pRepository -> {
            popupMenu = new JPopupMenu();
            CommitAction commitAction = new CommitAction(dialogDisplayer, pRepository, selectionSupplier);
            AddAction addAction = new AddAction(pRepository, selectionSupplier);
            DiffAction diffAction = new DiffAction(dialogDisplayer, pRepository, selectionSupplier);
            IgnoreAction ignoreAction = new IgnoreAction(pRepository, selectionSupplier);
            ExcludeAction excludeAction = new ExcludeAction(pRepository, selectionSupplier);
            popupMenu.add(commitAction);
            popupMenu.add(addAction);
            popupMenu.add(diffAction);
            popupMenu.add(ignoreAction);
            popupMenu.add(excludeAction);
        });

        statusTable.addMouseListener(new _PopupMouseListener());
        add(statusTable, BorderLayout.CENTER);
    }

    private class _SelectionSupplier implements Supplier<List<IFileChangeType>> {
        @Override
        public List<IFileChangeType> get() {
            List<IFileChangeType> selectedFileChangeTypes = new ArrayList<>();
            for (int rowNum : statusTable.getSelectedRows()) {
                status.subscribe(status -> {
                    if (rowNum < status.getUncommitted().size())
                        selectedFileChangeTypes.add(status.getUncommitted().get(rowNum));
                });
            }
            return selectedFileChangeTypes;
        }
    }

    /**
     * Listener that displays the popup menu on right-click and notifies the actions which rows are selected
     */
    private class _PopupMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                JTable source = (JTable) e.getSource();
                int row = source.rowAtPoint(e.getPoint());
                int column = source.columnAtPoint(e.getPoint());

                // if the row the user right-clicked on is not selected -> set it selected
                if (!source.isRowSelected(row))
                    source.changeSelection(row, column, false, false);

                if (popupMenu != null) {
                    for (Component component : popupMenu.getComponents())
                        component.setEnabled(component.isEnabled());

                    popupMenu.show(statusTable, e.getX(), e.getY());
                }

            }
        }
    }

}
