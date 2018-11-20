package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.BranchListTableModel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class to display the branches of a repository
 *
 * @author A.Arnold 28.09.2018
 */
class BranchListWindowContent extends JPanel {
    private final static int SCROLL_SPEED_INCREMENT = 16;
    private final IActionProvider actionProvider;
    private final Observable<IRepository> repository;
    private final JTable localStatusTable = new JTable();
    private final JTable remoteStatusTable = new JTable();
    private final ObservableListSelectionModel localSelectionModel;
    private final ObservableListSelectionModel remoteSelectionModel;


    /**
     * BranchListWindowContent gives the GUI all branches in two lists back. The two lists are the local and the remote refs.
     *
     * @param pRepository the repository for checking all branches
     */
    @Inject
    BranchListWindowContent(IActionProvider pActionProvider, @Assisted Observable<IRepository> pRepository) {
        localSelectionModel = new ObservableListSelectionModel(localStatusTable.getSelectionModel());
        localStatusTable.setSelectionModel(localSelectionModel);
        remoteSelectionModel = new ObservableListSelectionModel(remoteStatusTable.getSelectionModel());
        remoteStatusTable.setSelectionModel(remoteSelectionModel);
        actionProvider = pActionProvider;
        repository = pRepository;
        _initGui();
    }

    /**
     * Initialise the GUI for the BranchWindow
     */
    private void _initGui() {
        setLayout(new BorderLayout());
        Observable<List<IBranch>> branchObservable = repository.flatMap(IRepository::getBranches);

        //the local Status Table
        Observable<List<IBranch>> localSelectionObservable = Observable.combineLatest(localSelectionModel.selectedRows(), branchObservable, (pSelected, pBranches) -> {
            if (pSelected == null || pBranches == null)
                return Collections.emptyList();
            return Stream.of(pSelected)
                    .map(pBranches::get)
                    .filter(pBranch -> pBranch.getType() == EBranchType.LOCAL)
                    .collect(Collectors.toList());
        });

        localStatusTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        localStatusTable.setModel(new BranchListTableModel(branchObservable, EBranchType.LOCAL));
        localStatusTable.getTableHeader().setReorderingAllowed(false);
        localStatusTable.addMouseListener(new _PopupStarter(localSelectionObservable, localStatusTable));
        localStatusTable.removeColumn(localStatusTable.getColumn("branchID"));
        JScrollPane localScrollPane = new JScrollPane(localStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        localScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);

        //the remote Status Table
        Observable<List<IBranch>> remoteSelectionObservable = Observable.combineLatest(remoteSelectionModel.selectedRows(), branchObservable, (pSelected, pBranches) -> {
            if (pSelected == null || pBranches == null)
                return Collections.emptyList();
            return Stream.of(pSelected)
                    .map(pBranches::get)
                    .filter(pBranch -> pBranch.getType() == EBranchType.REMOTE)
                    .collect(Collectors.toList());
        });
        remoteStatusTable.setModel(new BranchListTableModel(branchObservable, EBranchType.REMOTE));
        remoteStatusTable.getTableHeader().setReorderingAllowed(false);
        remoteStatusTable.addMouseListener(new _PopupStarter(remoteSelectionObservable, remoteStatusTable));
        remoteStatusTable.removeColumn(remoteStatusTable.getColumn("branchID"));
        JScrollPane remoteScrollPane = new JScrollPane(remoteStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        remoteScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);

        //the mainPanel & tabbedPanel
        this.setPreferredSize(new Dimension(800, 300));
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, localScrollPane, remoteScrollPane);
        mainPanel.setResizeWeight(0.5);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * A private helper class for the mouse handler and click options
     */
    private class _PopupStarter extends MouseAdapter {
        JTable table;
        Observable<List<IBranch>> branchList;

        _PopupStarter(Observable<List<IBranch>> pBranchList, JTable pTable) {
            branchList = pBranchList;
            table = pTable;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    Function<List<IBranch>, Optional<IBranch>> mapping = pBranches -> {
                        if (pBranches.isEmpty()) {
                            return Optional.empty();
                        } else return Optional.of(pBranches.get(0));
                    };
                    popupMenu.add(actionProvider.getShowAllCommitsAction(repository, branchList));
                    popupMenu.add(actionProvider.getCheckoutAction(repository, branchList.map(mapping::apply)));
                    popupMenu.add(actionProvider.getMergeAction(repository, branchList));
                    popupMenu.show(table, e.getX(), e.getY());
                } else {
                    table.clearSelection();
                }
            }
        }
    }
}
