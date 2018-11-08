package de.adito.git.gui;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

/**
 * @author m.kaspera 22.10.2018
 */
public class TextHighlightUtil {

    /**
     * @param changeChunks        List with IFileChangeChunk that describe the changes
     * @param aTextPane           JTextPane for the "old" side of the diff
     * @param bTextPane           JTextPane for the "new" side of the diff
     * @param insertParityStrings if true, the parity strings are inserted into the textPanes as well
     * @throws BadLocationException if the message is getting inserted out of bounds
     */
    public static void insertChangeChunks(List<IFileChangeChunk> changeChunks, JTextPane aTextPane,
                                          JTextPane bTextPane, boolean insertParityStrings) throws BadLocationException {
        StyledDocument aDocument = aTextPane.getStyledDocument();
        StyledDocument bDocument = bTextPane.getStyledDocument();
        for (IFileChangeChunk changeChunk : changeChunks) {
            // Background color according to the EChangeType
            TextHighlightUtil.insertText(aDocument, changeChunk.getALines(), changeChunk.getChangeType().getDiffColor());
            TextHighlightUtil.insertText(bDocument, changeChunk.getBLines(), changeChunk.getChangeType().getDiffColor());
            if (insertParityStrings) {
                TextHighlightUtil.insertText(aDocument, changeChunk.getAParityLines(), changeChunk.getChangeType().getDiffColor());
                TextHighlightUtil.insertText(bDocument, changeChunk.getBParityLines(), changeChunk.getChangeType().getDiffColor());
            }
        }
    }

    /**
     * @param changeChunks        List of IFileChangeChunks whose text should be inserted
     * @param oldTextPane         textPane for the A/Old side of the IFileChangeChunks
     * @param newTextPane         textPane for the B/New side of the IFileChangeChunks
     * @param insertParityStrings if true, the parity strings are inserted into the textPanes
     */
    public static void insertChangeChunkStrings(List<IFileChangeChunk> changeChunks, JTextPane oldTextPane, JTextPane newTextPane, boolean insertParityStrings) {
        StringBuilder oldString = new StringBuilder();
        StringBuilder newString = new StringBuilder();
        changeChunks.forEach(changeChunk -> {
            oldString.append(changeChunk.getALines());
            newString.append(changeChunk.getBLines());
            if (insertParityStrings) {
                oldString.append(changeChunk.getAParityLines());
                newString.append(changeChunk.getBParityLines());
            }
        });
        oldTextPane.setText(oldString.toString());
        newTextPane.setText(newString.toString());
    }

    /**
     * @param document   StyledDocument of the JTextPane for which to insert the message
     * @param message    String that should be inserted into the JTextPane
     * @param changeType ChangeType, determines the background color of the line/text
     * @throws BadLocationException if the message is getting inserted out of bounds
     */
    public static void insertText(StyledDocument document, String message, EChangeType changeType) throws BadLocationException {
        insertText(document, message, changeType.getDiffColor());
    }

    /**
     * @param document StyledDocument of the JTextPane for which to insert the message
     * @param message  String that should be inserted into the JTextPane
     * @param color    The Background color for the Text getting inserted
     * @throws BadLocationException if the message is getting inserted out of bounds
     */
    public static void insertText(@NotNull StyledDocument document, @NotNull String message, @Nullable Color color) throws BadLocationException {
        if (color == null)
            color = new JLabel().getBackground();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBackground(attr, color);
        int offset = document.getLength();
        document.insertString(offset, message, attr);
    }

    /**
     * @param oldVersionPane textPane for the A/Old side of the IFileChangeChunks
     * @param newVersionPane textPane for the B/New side of the IFileChangeChunks
     * @param changeChunks   List of FileChangeChunks that determine the coloring of the textPanes
     * @throws BadLocationException if coloring outside the bounds is attempted
     */
    @Deprecated
    public static void colorDiffText(JTextPane oldVersionPane, JTextPane newVersionPane, List<IFileChangeChunk> changeChunks) throws BadLocationException {
        LineHighlighterDelegate oldVersionHighlighter = new LineHighlighterDelegate((BasicTextUI.BasicHighlighter) oldVersionPane.getHighlighter());
        LineHighlighterDelegate newVersionHighlighter = new LineHighlighterDelegate((BasicTextUI.BasicHighlighter) newVersionPane.getHighlighter());
        oldVersionPane.setHighlighter(oldVersionHighlighter);
        newVersionPane.setHighlighter(newVersionHighlighter);
        int currentOldIndex = 0;
        int currentNewIndex = 0;
        int currentOldLen;
        int currentNewLen;
        for (IFileChangeChunk changeChunk : changeChunks) {
            currentOldLen = changeChunk.getALines().length() + changeChunk.getAParityLines().length();
            currentNewLen = changeChunk.getBLines().length() + changeChunk.getBParityLines().length();
            if (changeChunk.getChangeType() != EChangeType.SAME) {
                DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(changeChunk.getChangeType().getDiffColor()) {
                    @Override
                    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
                        bounds.getBounds().width = c.getWidth();
                        super.paint(g, offs0, offs1, bounds, c);
                    }
                };
                oldVersionHighlighter.addBackgroundHighlight(currentOldIndex, currentOldIndex + currentOldLen, painter);
                newVersionHighlighter.addBackgroundHighlight(currentNewIndex, currentNewIndex + currentNewLen, painter);
            }
            currentOldIndex += currentOldLen;
            currentNewIndex += currentNewLen;
        }

    }

}
