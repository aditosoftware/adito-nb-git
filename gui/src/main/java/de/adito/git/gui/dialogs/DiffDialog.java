package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.TextHighlightUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.List;

/**
 * Window that displays the list of changes found during a diff
 *
 * @author m.kaspera 05.10.2018
 */
class DiffDialog extends JPanel {

    private final static int SCROLL_SPEED_INCREMENT = 16;
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
        JTextPane oldVersionPane = new JTextPane();
        oldVersionPane.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
        JTextPane newVersionPane = new JTextPane();
        newVersionPane.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));

        try {
            for (IFileDiff fileDiff : diffs) {
                TextHighlightUtil.insertText(oldVersionPane.getStyledDocument(), fileDiff.getFilePath(EChangeSide.OLD) + "\n\n", Color.WHITE);
                TextHighlightUtil.insertText(newVersionPane.getStyledDocument(), fileDiff.getFilePath(EChangeSide.NEW) + "\n\n", Color.WHITE);
                TextHighlightUtil.insertChangeChunks(fileDiff.getFileChanges().getChangeChunks().blockingFirst(), oldVersionPane, newVersionPane, true);
                TextHighlightUtil.insertText(oldVersionPane.getStyledDocument(), "\n\n---------------------------------------------------------------------------\n\n", Color.WHITE);
                TextHighlightUtil.insertText(newVersionPane.getStyledDocument(), "\n\n---------------------------------------------------------------------------\n\n", Color.WHITE);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        JPanel oldPanel = new JPanel(new BorderLayout());
        oldPanel.add(oldVersionPane, BorderLayout.CENTER);
        JPanel newPanel = new JPanel(new BorderLayout());
        newPanel.add(newVersionPane, BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldPanel, newPanel);
        splitPane.setResizeWeight(0.5);
        JScrollPane scrollPane = new JScrollPane(splitPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        add(scrollPane, BorderLayout.CENTER);
    }

}
