package de.adito.git.gui;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.impl.RepositoryImpl;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CommitListWindow extends JPanel {
    private IRepository repository;
    private List<ICommit> commits;
    private ICommit commit;
    private Iterable<ICommit> allCommits;

    public CommitListWindow(IRepository pRepository) throws Exception {
        repository = pRepository;
        _initGUI();
    }

    private void _initGUI() throws Exception {
        setLayout(new BorderLayout());
        List<ICommit> commitList = repository.getAllCommits();

        JTable commitTable = new JTable(new CommitListTableModel(commitList));
        commitTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane commitScrollPane = new JScrollPane(commitTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        commitScrollPane.setPreferredSize(new Dimension(800, 300));
        add(commitScrollPane, BorderLayout.CENTER);

    }
}
