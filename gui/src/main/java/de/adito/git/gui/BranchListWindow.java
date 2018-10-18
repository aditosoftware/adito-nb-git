package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.actions.CheckoutAction;
import de.adito.git.gui.actions.ShowAllCommitsAction;
import de.adito.git.gui.rxjava.ObservableTable;
import de.adito.git.gui.tableModels.BranchListTableModel;
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
 * class to display the branches of a repository
 *
 * @author A.Arnold 28.09.2018
 */
public class BranchListWindow extends JPanel {
    private Observable<IRepository> repository;
    private ObservableTable localStatusTable = new ObservableTable();
    private ObservableTable remoteStatusTable = new ObservableTable();


    /**
     * BranchListWindow gives the GUI all branches in two lists back. The two lists are the local and the remote refs.
     *
     * @param pRepository the repository for checking all branches
     */
    public BranchListWindow(Observable<IRepository> pRepository) {
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

        Observable<List<IBranch>> localSelectionObservable = Observable.combineLatest(localStatusTable.selectedRows(), branchObservable, (pSelected, pBranches) -> {
            if (pSelected == null || pBranches == null)
                return Collections.emptyList();
            return Stream.of(pSelected)
                    .map(pBranches::get)
                    .filter(pBranch -> pBranch.getType() == EBranchType.LOCAL)
                    .collect(Collectors.toList());
        });
        Observable<List<IBranch>> remoteSelectionObservable = Observable.combineLatest(remoteStatusTable.selectedRows(), branchObservable, (pSelected, pBranches) -> {
            if (pSelected == null || pBranches == null)
                return Collections.emptyList();
            return Stream.of(pSelected)
                    .map(pBranches::get)
                    .filter(pBranch -> pBranch.getType() == EBranchType.REMOTE)
                    .collect(Collectors.toList());
        });

        localStatusTable.setModel(new BranchListTableModel(branchObservable, EBranchType.LOCAL));
        localStatusTable.getTableHeader().setReorderingAllowed(false);
        localStatusTable.addMouseListener(new _PopupStarter(localSelectionObservable, localStatusTable));
        localStatusTable.removeColumn(localStatusTable.getColumn("branchID"));
        JScrollPane localScrollPane = new JScrollPane(localStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the remote Status Table
        remoteStatusTable.setModel(new BranchListTableModel(branchObservable, EBranchType.REMOTE));
        remoteStatusTable.getTableHeader().setReorderingAllowed(false);
        remoteStatusTable.addMouseListener(new _PopupStarter(remoteSelectionObservable, remoteStatusTable));
        remoteStatusTable.removeColumn(remoteStatusTable.getColumn("branchID"));
        JScrollPane remoteScrollPane = new JScrollPane(remoteStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the mainPanel & tabbedPanel
        this.setPreferredSize(new Dimension(800, 300));
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, localScrollPane, remoteScrollPane);
        mainPanel.setResizeWeight(0.5);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * A private helper class for the Mousehandler and click options
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
            if (SwingUtilities.isRightMouseButton(e)) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(new ShowAllCommitsAction(repository, branchList));
                    popupMenu.add(new CheckoutAction(repository, branchList));
                    popupMenu.show(table, e.getX(), e.getY());
                } else {
                    table.clearSelection();
                }
            }
        }
    }
}
