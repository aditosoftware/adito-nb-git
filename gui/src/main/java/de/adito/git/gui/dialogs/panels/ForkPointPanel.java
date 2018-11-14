package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.List;

/**
 * Panel sitting in the middle of the two mergePanels
 * Displays the text of the fork-point initially, and the takes all
 * the accepted changes from the two branches/accepts text input
 * The text in this Pane will be the accepted solution if the user presses OK
 * in the dialog window
 *
 * @author m.kaspera 12.11.2018
 */
public class ForkPointPanel extends ChangeDisplayPanel implements IDiscardable {

    private final JTextPane textPane = super._createNonWrappingTextPane();
    private final JTextPane leftLineNumbering = new JTextPane();
    private final JTextPane rightLineNumbering = new JTextPane();
    private final JScrollPane mainScrollPane = new JScrollPane();
    private final JScrollPane leftLineNumberingScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final JScrollPane rightLineNumberingScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final _PaneDocumentListener paneDocumentListener = new _PaneDocumentListener();
    private final Disposable disposable;
    private final IMergeDiff mergeDiff;
    private int caretPosition = 0;

    public ForkPointPanel(IMergeDiff pMergeDiff) {
        super(EChangeSide.OLD);
        mergeDiff = pMergeDiff;
        _initGui();
        disposable = Observable.zip(
                mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks(),
                mergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.THEIRS).getFileChanges().getChangeChunks(), _ListPair::new)
                .subscribe(this::_refreshContent);
    }

    private void _initGui() {
        setLayout(new BorderLayout());
        add(leftLineNumberingScrollPane, BorderLayout.WEST);
        add(mainScrollPane, BorderLayout.CENTER);
        add(rightLineNumberingScrollPane, BorderLayout.EAST);

        mainScrollPane.add(textPane);
        mainScrollPane.setViewportView(textPane);
        textPane.getDocument().addDocumentListener(paneDocumentListener);
        // set the two line numbering panels to inactive/not writable
        leftLineNumbering.setEnabled(false);
        rightLineNumbering.setEnabled(false);
        leftLineNumberingScrollPane.add(leftLineNumbering);
        leftLineNumberingScrollPane.setViewportView(leftLineNumbering);
        rightLineNumberingScrollPane.add(rightLineNumbering);
        rightLineNumberingScrollPane.setViewportView(rightLineNumbering);
        // remove the border from the two line number panes
        leftLineNumberingScrollPane.setBorder(null);
        rightLineNumberingScrollPane.setBorder(null);
        super.coupleScrollPanes(mainScrollPane, leftLineNumberingScrollPane);
        super.coupleScrollPanes(mainScrollPane, rightLineNumberingScrollPane);

        Border usedBorder = mainScrollPane.getBorder();
        mainScrollPane.setBorder(null);
        setBorder(usedBorder);
    }

    private void _refreshContent(_ListPair pChangeChunkLists) {
        paneDocumentListener.disable();
        caretPosition = textPane.getCaretPosition();
        TextHighlightUtil.insertColoredText(textPane, pChangeChunkLists.yourVersion, pChangeChunkLists.theirVersion, EChangeSide.OLD, false);
        TextHighlightUtil.insertColoredLineNumbers(leftLineNumbering, pChangeChunkLists.yourVersion, EChangeSide.OLD, false);
        TextHighlightUtil.insertColoredLineNumbers(rightLineNumbering, pChangeChunkLists.theirVersion, EChangeSide.OLD, false);
        textPane.setCaretPosition(caretPosition);
        paneDocumentListener.enable();
    }

    @Override
    public void discard() {
        disposable.dispose();
    }

    /**
     * DocumentListener to check for user-input in the fork-point version of the merge conflict
     * Can be manually dis-/enabled if text is input by the code, this is done by calling the
     * methods named disable/enable
     */
    private class _PaneDocumentListener implements DocumentListener {

        private boolean isActive = true;

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (isActive) {
                try {
                    // get the information about what text and where before the invokeLater(), else the information can be outdated
                    final String insertedText = e.getDocument().getText(e.getOffset(), e.getLength());
                    final int insertOffset = e.getOffset();
                    SwingUtilities.invokeLater(() -> {
                        caretPosition = textPane.getCaretPosition() + insertedText.length();
                        mergeDiff.insertText(insertedText, insertedText.length(), insertOffset, true);
                    });
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (isActive) {
                caretPosition = textPane.getCaretPosition();
                SwingUtilities.invokeLater(() -> mergeDiff.insertText("", e.getLength(), e.getOffset(), false));
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }

        /**
         * Enables the processing of events for this listener
         */
        void enable() {
            isActive = true;
        }

        /**
         * Disables the processing of events for this listener. Acts as if the listener wouldn't be here if disabled
         */
        void disable() {
            isActive = false;
        }
    }

    private static class _ListPair {
        List<IFileChangeChunk> yourVersion;
        List<IFileChangeChunk> theirVersion;

        _ListPair(List<IFileChangeChunk> yourVersion, List<IFileChangeChunk> theirVersion) {
            this.yourVersion = yourVersion;
            this.theirVersion = theirVersion;
        }
    }
}
