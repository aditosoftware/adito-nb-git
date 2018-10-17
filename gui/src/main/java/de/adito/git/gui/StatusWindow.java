package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.actions.*;
import de.adito.git.gui.rxjava.ObservableTable;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
public class StatusWindow extends JPanel implements IDiscardable {

    private final Observable<IFileStatus> status;
    private final Observable<IRepository> repository;
    private final Observable<List<IFileChangeType>> selectionObservable;
    private final ObservableTable statusTable = new ObservableTable();
    private IDialogDisplayer dialogDisplayer;
    private JPopupMenu popupMenu;

    public StatusWindow(IDialogDisplayer pDialogDisplayer, RepositoryProvider repository) {
        dialogDisplayer = pDialogDisplayer;
        this.repository = repository.getRepositoryImpl();
        status = repository.getRepositoryImpl()
                .flatMap(IRepository::getStatus);
        selectionObservable = Observable.combineLatest(statusTable.selectedRows(), status, (pSelected, pStatus) -> {
            if (pSelected == null || pStatus == null)
                return Collections.emptyList();
            List<IFileChangeType> uncommittedListCached = pStatus.getUncommitted();
            return Stream.of(pSelected)
                    .map(uncommittedListCached::get)
                    .collect(Collectors.toList());
        });
        _initGui();
    }

    private void _initGui() {
        setLayout(new BorderLayout());
//        statusTable = new _ObservableTable();
        statusTable.setModel(new StatusTableModel(status));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // CellRenderer so the files are colored according to their EChangeType
        for (int index = 0; index < statusTable.getColumnModel().getColumnCount(); index++) {
            statusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }

        repository.subscribe(pRepository -> {
            popupMenu = new JPopupMenu();
            CommitAction commitAction = new CommitAction(dialogDisplayer, pRepository, selectionObservable);
            AddAction addAction = new AddAction(pRepository, selectionObservable);
            DiffAction diffAction = new DiffAction(dialogDisplayer, pRepository, selectionObservable);
            IgnoreAction ignoreAction = new IgnoreAction(pRepository, selectionObservable);
            ExcludeAction excludeAction = new ExcludeAction(pRepository, selectionObservable);
            popupMenu.add(commitAction);
            popupMenu.add(addAction);
            popupMenu.add(diffAction);
            popupMenu.add(ignoreAction);
            popupMenu.add(excludeAction);
        });

        statusTable.addMouseListener(new _PopupMouseListener());
        add(statusTable, BorderLayout.CENTER);
    }

    @Override
    public void discard() {
        ((StatusTableModel)statusTable.getModel()).discard();
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
