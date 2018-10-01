package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * class to diplay the branches of a repository
 *
 * @author A.Arnold 28.09.2018
 */
public class BranchListWindow extends JPanel {
    private IRepository repository;

    public BranchListWindow(IRepository pRepository) {
        repository = pRepository;
        _initGui();
    }

    private void _initGui() {
        setLayout(new BorderLayout());
        List<IBranch> branches = repository.getBranches();



        //local Status Table
        JTable localStatusTable = new JTable(new BranchListTableModel(branches, EBranchType.LOCAL));
        JScrollPane localScrollPane = new JScrollPane(localStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        localStatusTable.getTableHeader().setReorderingAllowed(false);

        //remote Status Table
        JTable remoteStatusTable = new JTable(new BranchListTableModel(branches, EBranchType.REMOTE));
        JScrollPane remoteScrollPane = new JScrollPane(remoteStatusTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        remoteStatusTable.getTableHeader().setReorderingAllowed(false);


//        statusTable.getColumnModel().getColumn(0).setMinWidth(300);
//        add(statusTable, BorderLayout.CENTER);

        JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, localScrollPane, remoteScrollPane);
        mainPanel.setResizeWeight(0.5);
        mainPanel.setPreferredSize(new Dimension(400, 300));
        add(mainPanel, BorderLayout.CENTER);
    }
}
