package de.adito.git.gui;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.List;

/**
 * @author m.kaspera 22.10.2018
 */
class TextHighlightUtil {


    /**
     * @param changeChunks List with IFileChangeChunk that describe the changes
     * @param aTextPane    JTextPane for the "old" side of the diff
     * @param bTextPane    JTextPane for the "new" side of the diff
     * @param insertParityStrings if true, the parity strings are inserted into the textPanes as well
     * @throws BadLocationException if the message is getting inserted out of bounds
     */
    static void insertChangeChunks(List<IFileChangeChunk> changeChunks, JTextPane aTextPane,
                                   JTextPane bTextPane, boolean insertParityStrings) throws BadLocationException {
        StyledDocument aDocument = aTextPane.getStyledDocument();
        StyledDocument bDocument = bTextPane.getStyledDocument();
        Color aBGColor;
        Color bBGColor;
        Color parityBGColor = Color.GRAY;
        for (IFileChangeChunk changeChunk : changeChunks) {
            // Background color according to the EChangeType
            if (changeChunk.getChangeType() == EChangeType.ADD) {
                aBGColor = Color.WHITE;
                bBGColor = Color.GREEN;
            } else if (changeChunk.getChangeType() == EChangeType.DELETE) {
                aBGColor = Color.RED;
                bBGColor = Color.WHITE;
            } else if (changeChunk.getChangeType() == EChangeType.MODIFY) {
                aBGColor = Color.RED;
                bBGColor = Color.GREEN;
            } else {
                aBGColor = Color.WHITE;
                bBGColor = Color.WHITE;
            }
            TextHighlightUtil.insertText(aDocument, changeChunk.getALines(), aBGColor);
            TextHighlightUtil.insertText(bDocument, changeChunk.getBLines(), bBGColor);
            if(insertParityStrings) {
                TextHighlightUtil.insertText(aDocument, changeChunk.getAParityLines(), parityBGColor);
                TextHighlightUtil.insertText(bDocument, changeChunk.getBParityLines(), parityBGColor);
            }
        }
    }

    /**
     * @param document StyledDocument of the JTextPane for which to insert the message
     * @param message  String that should be inserted into the JTextPane
     * @param changeType ChangeType, determines the background color of the line/text
     * @throws BadLocationException if the message is getting inserted out of bounds
     */
    static void insertText(StyledDocument document, String message, EChangeType changeType) throws BadLocationException {
        if (changeType == EChangeType.ADD) {
            insertText(document, message, Color.GREEN);
        } else if (changeType == EChangeType.DELETE) {
            insertText(document, message, Color.RED);
        } else if (changeType == EChangeType.MODIFY) {
            insertText(document, message, Color.GREEN);
        } else {
            insertText(document, message, Color.WHITE);
        }
    }

    /**
     * @param document StyledDocument of the JTextPane for which to insert the message
     * @param message  String that should be inserted into the JTextPane
     * @param color    The Background color for the Text getting inserted
     * @throws BadLocationException if the message is getting inserted out of bounds
     */
    static void insertText(StyledDocument document, String message, Color color) throws BadLocationException {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBackground(attr, color);
        int offset = document.getLength();
        document.insertString(offset, message, attr);
    }

}
