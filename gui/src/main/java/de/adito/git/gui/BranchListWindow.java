package de.adito.git.gui;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.actions.ShowAllCommitsAction;
import de.adito.git.gui.tableModels.BranchListTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * class to display the branches of a repository
 *
 * @author A.Arnold 28.09.2018
 */
public class BranchListWindow extends JPanel implements IBranchListWindow {
    private IRepository repository;

    /**
     * BranchListWindow gives the GUI all branches in two lists back. The two lists are the local and the remote refs.
     *
     * @param pRepositoryProvider the repository for checking all branches
     * @throws Exception If the branches can't check there it throws an Exception.
     */
    @Inject
    BranchListWindow(RepositoryProvider pRepositoryProvider) throws Exception {
        repository = pRepositoryProvider.getRepositoryImpl();
        _initGui();
    }

    /**
     * Initialise the GUI for the BranchWindow
     */
    private void _initGui() throws Exception {
        setLayout(new BorderLayout());
        List<IBranch> branches = repository.getBranches();

        //the local Status Table
        JTable localStatusTable = new JTable(new BranchListTableModel(branches, EBranchType.LOCAL));
        localStatusTable.getTableHeader().setReorderingAllowed(false);
        localStatusTable.addMouseListener(new _PopupStarter(localStatusTable));
        localStatusTable.removeColumn(localStatusTable.getColumn("branchID"));
        JScrollPane localScrollPane = new JScrollPane(localStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the remote Status Table
        JTable remoteStatusTable = new JTable(new BranchListTableModel(branches, EBranchType.REMOTE));
        remoteStatusTable.getTableHeader().setReorderingAllowed(false);
        remoteStatusTable.addMouseListener(new _PopupStarter(remoteStatusTable));
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

        _PopupStarter(JTable pTable) {
            table = pTable;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                List<IBranch> branches = new ArrayList<>();
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String branchName = null;
                    int[] selectedRows = table.getSelectedRows();

                    for (int selectedRow : selectedRows) {
                        try {
                            branches.add(repository.getBranch((String) table.getModel().getValueAt(row, 0)));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(new ShowAllCommitsAction(repository, branches));
                    popupMenu.show(table, e.getX(), e.getY());
                } else {
                    table.clearSelection();
                }
            }
        }
    }
}
