package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.gui.actions.ShowAllCommitsAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

/**
 * class to display the branches of a repository
 *
 * @author A.Arnold 28.09.2018
 */
public class BranchListWindow extends JPanel {
    private IRepository repository;
    private JTabbedPane tabbedPane = new JTabbedPane();


    /**
     * BranchListWindow gives the GUI all branches in two lists back. The two lists are the local and the remote refs.
     * @param pRepository the repository for checking all branches
     * @param tabbedPane
     * @throws Exception If the branches can't check there it throws an Exception.
     */
    public BranchListWindow(IRepository pRepository, JTabbedPane tabbedPane) throws Exception {
        repository = pRepository;
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
        localStatusTable.addMouseListener(new _popupStarter(localStatusTable));
        localStatusTable.removeColumn(localStatusTable.getColumn("branchID"));
        JScrollPane localScrollPane = new JScrollPane(localStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the remote Status Table
        JTable remoteStatusTable = new JTable(new BranchListTableModel(branches, EBranchType.REMOTE));
        remoteStatusTable.getTableHeader().setReorderingAllowed(false);
        remoteStatusTable.addMouseListener(new _popupStarter(remoteStatusTable));
        remoteStatusTable.removeColumn(remoteStatusTable.getColumn("branchID"));
        JScrollPane remoteScrollPane = new JScrollPane(remoteStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the mainPanel & tabbedPanel
        tabbedPane.setPreferredSize(new Dimension(800,300));
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, localScrollPane, remoteScrollPane);
        mainPanel.setResizeWeight(0.5);
        tabbedPane.addTab("Branch List", mainPanel);
        add(tabbedPane, BorderLayout.CENTER);


    }

    private class _popupStarter implements MouseListener {
        JTable table;

        _popupStarter(JTable pTable){
            table = pTable;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    JPopupMenu popupMenu = new JPopupMenu();

                    popupMenu.add(new ShowAllCommitsAction(repository, (JTable) e.getSource(), tabbedPane));

                    popupMenu.show(table, e.getX(), e.getY());
                } else {
                    table.clearSelection();
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
