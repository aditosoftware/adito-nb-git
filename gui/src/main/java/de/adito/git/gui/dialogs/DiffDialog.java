package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.EChangeSide;
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

    private final String backGroundTextColor = "nb.output.backgorund";
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
//        oldVersionPane.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
//        newVersionPane.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
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
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldPanel, newPanel);
        splitPane.setResizeWeight(0.5);
        JScrollPane scrollPane = new JScrollPane(splitPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);

        // add table and DiffPanel to the Panel
        add(fileListTable, BorderLayout.EAST);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * @param fileDiff the IFileDiff that should be displayed in the Diff Panel
     */
    private void _updateDiffPanel(IFileDiff fileDiff) {
        try {
            // clear text in textPanes
            oldVersionPane.setText("");
            newVersionPane.setText("");
            // insert the text from the IFileDiffs
            TextHighlightUtil.insertText(oldVersionPane.getStyledDocument(), fileDiff.getFilePath(EChangeSide.OLD) + "\n\n", UIManager.getColor(backGroundTextColor));
            TextHighlightUtil.insertText(newVersionPane.getStyledDocument(), fileDiff.getFilePath(EChangeSide.NEW) + "\n\n", UIManager.getColor(backGroundTextColor));
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
