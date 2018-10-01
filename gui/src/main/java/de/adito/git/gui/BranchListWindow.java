package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * class to display the branches of a repository
 *
 * @author A.Arnold 28.09.2018
 */
public class BranchListWindow extends JPanel {
    private IRepository repository;

    /**
     * BranchListWindow gives the GUI all branches in two lists back. The two lists are the local and the remote refs.
     * @param pRepository the repository for checking all branches
     */
    public BranchListWindow(IRepository pRepository) throws Exception {
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
        JScrollPane localScrollPane = new JScrollPane(localStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the remote Status Table
        JTable remoteStatusTable = new JTable(new BranchListTableModel(branches, EBranchType.REMOTE));
        remoteStatusTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane remoteScrollPane = new JScrollPane(remoteStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //the mainPanel
        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, localScrollPane, remoteScrollPane);
        mainPanel.setResizeWeight(0.5);
        mainPanel.setPreferredSize(new Dimension(400, 300));
        add(mainPanel, BorderLayout.CENTER);
    }
}
