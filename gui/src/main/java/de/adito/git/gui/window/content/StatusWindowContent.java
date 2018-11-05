package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.FileStatusCellRenderer;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.rxjava.ObservableTable;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

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
class StatusWindowContent extends JPanel implements IDiscardable {

    private final Observable<IFileStatus> status;
    private final Observable<IRepository> repository;
    private IActionProvider actionProvider;
    private final Observable<List<IFileChangeType>> selectionObservable;
    private final ObservableTable statusTable = new ObservableTable();
    private Disposable disposable;
    private JPopupMenu popupMenu;

    @Inject
    StatusWindowContent(IActionProvider pActionProvider, @Assisted Observable<IRepository> pRepository) {
        repository = pRepository;
        actionProvider = pActionProvider;
        status = repository
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
        statusTable.setModel(new StatusTableModel(status));
        statusTable.getColumnModel().getColumn(0).setMinWidth(150);
        statusTable.getColumnModel().getColumn(1).setMinWidth(250);
        statusTable.getColumnModel().getColumn(2).setMinWidth(50);
        statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // CellRenderer so the files are colored according to their EChangeType
        for (int index = 0; index < statusTable.getColumnModel().getColumnCount(); index++) {
            statusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
        }

        disposable = repository.subscribe(pRepository -> {
            popupMenu = new JPopupMenu();
            popupMenu.add(actionProvider.getCommitAction(repository, selectionObservable));
            popupMenu.add(actionProvider.getAddAction(repository, selectionObservable));
            popupMenu.add(actionProvider.getIgnoreAction(repository, selectionObservable));
            popupMenu.add(actionProvider.getExcludeAction(repository, selectionObservable));
            popupMenu.addSeparator();
            popupMenu.add(actionProvider.getDiffAction(repository, selectionObservable));
            popupMenu.addSeparator();
            popupMenu.add(actionProvider.getRevertWorkDirAction(repository, selectionObservable));
            popupMenu.add(actionProvider.getResetFilesAction(repository, selectionObservable));
        });

        statusTable.addMouseListener(new _PopupMouseListener());
        add(statusTable, BorderLayout.CENTER);
    }

    @Override
    public void discard() {
        ((StatusTableModel)statusTable.getModel()).discard();
        disposable.dispose();
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
