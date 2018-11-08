package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.rxjava.ObservableTable;
import de.adito.git.gui.tableModels.DiffTableModel;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.List;

/**
 * Window that displays the list of changes found during a diff
 *
 * @author m.kaspera 05.10.2018
 */
class DiffDialog extends JPanel implements IDiscardable {

    private final static int SCROLL_SPEED_INCREMENT = 16;
    private final ObservableTable fileListTable = new ObservableTable();
    private final JTextPane oldVersionPane = new JTextPane();
    private final JTextPane newVersionPane = new JTextPane();
    private Disposable disposable;
    private List<IFileDiff> diffs;

    @Inject
    public DiffDialog(@Assisted List<IFileDiff> pDiffs) {
        this.diffs = pDiffs;
        _initGui();
    }

    /**
     * sets up the GUI
     */
    private void _initGui() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1600, 900));
        // set contentType to text/html. Because for whatever reason that's the only way the whole line gets marked, not just the text
        oldVersionPane.setContentType("text/html");
        newVersionPane.setContentType("text/html");

        // Table on which to select which IFileDiff is displayed in the DiffPanel
        fileListTable.setModel(new DiffTableModel(diffs));
        fileListTable.setMinimumSize(new Dimension(200, 600));
        fileListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // display the first entry as default
        if (diffs.size() >= 1)
            _updateDiffPanel(diffs.get(0));
        // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
        disposable = fileListTable.selectedRows().subscribe(pSelectedRows -> {
            if (pSelectedRows != null && pSelectedRows.length == 1)
                _updateDiffPanel(diffs.get(pSelectedRows[0]));
        });

        // DiffPanel that displays the selected IFileDiff
        JPanel oldPanel = new JPanel(new BorderLayout());
        JPanel newPanel = new JPanel(new BorderLayout());
        oldPanel.add(oldVersionPane, BorderLayout.CENTER);
        newPanel.add(newVersionPane, BorderLayout.CENTER);
        JScrollPane oldVersionScrollPane = new JScrollPane(oldPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane newVersionScrollPane = new JScrollPane(newPanel);
        newVersionScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        // synchronize the two scrollPanes
        oldVersionScrollPane.getVerticalScrollBar().setModel(newVersionScrollPane.getVerticalScrollBar().getModel());
        oldVersionScrollPane.setWheelScrollingEnabled(false);
        oldVersionScrollPane.addMouseWheelListener(newVersionScrollPane::dispatchEvent);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldVersionScrollPane, newVersionScrollPane);
        splitPane.setResizeWeight(0.5);

        // add table and DiffPanel to the Panel
        add(fileListTable, BorderLayout.EAST);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * @param fileDiff the IFileDiff that should be displayed in the Diff Panel
     */
    private void _updateDiffPanel(IFileDiff fileDiff) {
        // clear text in textPanes
        oldVersionPane.setText("");
        newVersionPane.setText("");
        // insert the text from the IFileDiffs
        try {
            TextHighlightUtil.insertChangeChunks(fileDiff.getFileChanges().getChangeChunks().blockingFirst(), oldVersionPane, newVersionPane, true);
            revalidate();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}
