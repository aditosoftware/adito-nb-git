package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.tableModels.CommitListTableModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Class to display all commits
 *
 * @author A.Arnold 01.10.2018
 */
public class CommitHistoryWindow extends JPanel {
    private List<ICommit> commitList;

    /**
     * CommitListWindow gives the GUI all Commits from the branch back.
     *
     * @param pRepository the repository where the branch is in.
     * @param pBranch     The branch to check the commits.
     * @throws Exception The repository can't call the RemoteServer
     */
    public CommitHistoryWindow(IRepository pRepository, IBranch pBranch) throws Exception {
        this(pRepository, pRepository.getCommits(pBranch));
    }

    /**
     * CommitListWindow gives the GUI all Commits for one File back.
     *
     * @param pRepository The repository where the file is in.
     * @param pFile       The file to check the commits
     * @throws Exception The repository can't call the RemoteServer
     */
    public CommitHistoryWindow(IRepository pRepository, File pFile) throws Exception {
        this(pRepository, pRepository.getCommits(pFile));
    }

    /**
     * CommitListWindow gives the commit for one identifier back.
     *
     * @param pRepository The repository where the identifier is in.
     * @param pIdentifier The identifier to check the commit.
     * @throws Exception The repository can't call the RemoteServer
     */
    public CommitHistoryWindow(IRepository pRepository, String pIdentifier) throws Exception {
        this(pRepository, Collections.singletonList(pRepository.getCommit(pIdentifier)));
    }

    public CommitHistoryWindow(IRepository pRepository, List<ICommit> pCommits) {
        _initGUI();
        commitList = pCommits;
        JTable commitTable = _createTable();
    }

    private void _initGUI() {
        setLayout(new BorderLayout());
    }

    private JTable _createTable() {
        JTable commitTable = new JTable(new CommitListTableModel(commitList));
        commitTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        commitTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane commitScrollPane = new JScrollPane(commitTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        commitScrollPane.setPreferredSize(new Dimension(800, 300));
        add(commitScrollPane, BorderLayout.CENTER);
        return commitTable;
    }


}
