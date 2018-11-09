package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.rxjava.ObservableTable;
import de.adito.git.gui.tableModels.DiffTableModel;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
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
    private final DiffPanel oldVersionPanel = new DiffPanel(EChangeSide.OLD);
    private final DiffPanel newVersionPanel = new DiffPanel(EChangeSide.NEW);
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

        // make left and right DiffPanel scroll at the same time/speed
        oldVersionPanel.coupleToScrollPane(newVersionPanel.getMainScrollPane());
        oldVersionPanel.getMainScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldVersionPanel, newVersionPanel);
        splitPane.setResizeWeight(0.5);

        // add table and DiffPanel to the Panel
        add(fileListTable, BorderLayout.EAST);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * @param fileDiff the IFileDiff that should be displayed in the Diff Panel
     */
    private void _updateDiffPanel(IFileDiff fileDiff) {
        List<IFileChangeChunk> changeChunkList = fileDiff.getFileChanges().getChangeChunks().blockingFirst();
        // clear text in textPanes
        oldVersionPanel.getTextPane().setText("");
        oldVersionPanel.getLineNumArea().setText("");
        newVersionPanel.getTextPane().setText("");
        newVersionPanel.getLineNumArea().setText("");
        // insert the text from the IFileDiffs
        try {
            TextHighlightUtil.insertChangeChunks(changeChunkList, oldVersionPanel.getTextPane(), newVersionPanel.getTextPane(), true);
            _writeLineNums(changeChunkList);
            revalidate();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * writes the lineNumbers for both panels
     *
     * @param pChangeChunkList List of IFileChangeChunks from which the line numbering is extracted
     */
    private void _writeLineNums(List<IFileChangeChunk> pChangeChunkList) {
        Color bgColor = new JLabel().getBackground();
        int oldVersionLineNum = 1;
        int newVersionLineNum = 1;
        int numNewLines;
        try {
            for (IFileChangeChunk changeChunk : pChangeChunkList) {
                numNewLines = changeChunk.getAEnd() - changeChunk.getAStart();
                TextHighlightUtil.appendText(oldVersionPanel.getLineNumArea().getStyledDocument(), _getLineNumString(oldVersionLineNum, numNewLines), changeChunk.getChangeType());
                oldVersionLineNum += numNewLines;
                numNewLines = changeChunk.getBEnd() - changeChunk.getBStart();
                // numbers on the right DiffPanel should be right-aligned
                TextHighlightUtil.appendText(
                        newVersionPanel.getLineNumArea().getStyledDocument(),
                        _getLineNumString(newVersionLineNum, numNewLines),
                        changeChunk.getChangeType().getDiffColor(),
                        StyleConstants.ALIGN_RIGHT);
                newVersionLineNum += numNewLines;
                // parity lines should only contain newlines anyway, so no filtering or counting newlines should be necessary
                TextHighlightUtil.appendText(oldVersionPanel.getLineNumArea().getStyledDocument(), changeChunk.getAParityLines(), bgColor);
                TextHighlightUtil.appendText(newVersionPanel.getLineNumArea().getStyledDocument(), changeChunk.getBParityLines(), bgColor);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param pStart number of the first line
     * @param pCount number of lines
     * @return String with the lineNumbers from pStart to pStart + pCount
     */
    private String _getLineNumString(int pStart, int pCount) {
        StringBuilder lineNumStringBuilder = new StringBuilder();
        for (int index = 0; index < pCount; index++) {
            lineNumStringBuilder.append(pStart + index).append("\n");
        }
        return lineNumStringBuilder.toString();
    }

    @Override
    public void discard() {
        disposable.dispose();
    }

    /**
     * Class to handle the basic layout of the two panels that display the differences between two files
     */
    private class DiffPanel extends JPanel {

        private final EChangeSide changeSide;
        private final JTextPane lineNumArea = new JTextPane();
        private final JTextPane textPane = new JTextPane();
        private final JScrollPane mainScrollPane = new JScrollPane();
        private final JScrollPane lineScrollPane = new JScrollPane();

        DiffPanel(EChangeSide pChangeSide) {
            changeSide = pChangeSide;
            _initGui();
        }

        private void _initGui() {
            setLayout(new BorderLayout());

            // set contentType to text/html. Because for whatever reason that's the only way the whole line gets marked, not just the text
            textPane.setContentType("text/html");
            lineNumArea.setContentType("text/html");

            // textPane should no be editable, but the text should still be normal
            textPane.setEditable(false);

            // text here should look disabled/grey and not be editable
            lineNumArea.setEnabled(false);

            // ScrollPane setup
            mainScrollPane.add(textPane);
            mainScrollPane.setViewportView(textPane);
            mainScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
            lineScrollPane.add(lineNumArea);
            lineScrollPane.setViewportView(lineNumArea);
            lineScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            lineScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            _coupleScrollPanes(mainScrollPane, lineScrollPane);

            // add the parts to the DiffPanel
            add(lineScrollPane, changeSide == EChangeSide.OLD ? BorderLayout.EAST : BorderLayout.WEST);
            add(mainScrollPane, BorderLayout.CENTER);

            // remove Borders from the parts and use the set Border to draw a border around the whole panel
            Border usedBorder = lineScrollPane.getBorder();
            lineScrollPane.setBorder(null);
            mainScrollPane.setBorder(null);
            setBorder(usedBorder);
        }

        /**
         * makes this DiffPanel's scrolling fixed to the scrolling of the passed JScrollPane
         *
         * @param pScrollPane JScrollPane that should determine the scrolling of the textPane and the lineNumbers of this DiffPanel
         */
        void coupleToScrollPane(JScrollPane pScrollPane) {
            _coupleScrollPanes(pScrollPane, mainScrollPane);
            _coupleScrollPanes(pScrollPane, lineScrollPane);
        }

        /**
         * @return the ScrollPane that controls the Scrolling of the main textPane
         */
        JScrollPane getMainScrollPane() {
            return mainScrollPane;
        }

        /**
         * @return the JTextPane that contains the diff-text
         */
        JTextPane getTextPane() {
            return textPane;
        }

        /**
         * @return JTextPane that contains only the line-numbering
         */
        JTextPane getLineNumArea() {
            return lineNumArea;
        }

        /**
         * Makes the masterScrollPane determine the scrolling behaviour/speed of the slaveScrollPane.
         * Both Panes are then  intertwined and cannot be scrolled independently
         *
         * @param masterScrollPane ScrollPane that will control scrolling behaviour. Is notified when the slave scrolls and does scroll then, too
         * @param slaveScrollPane  ScrollPane that is linked to the master. Scrolling in the master also means scrolling in the slave, and vice versa
         */
        private void _coupleScrollPanes(JScrollPane masterScrollPane, JScrollPane slaveScrollPane) {
            slaveScrollPane.getVerticalScrollBar().setModel(masterScrollPane.getVerticalScrollBar().getModel());
            slaveScrollPane.setWheelScrollingEnabled(false);
            slaveScrollPane.addMouseWheelListener(masterScrollPane::dispatchEvent);
        }
    }
}
