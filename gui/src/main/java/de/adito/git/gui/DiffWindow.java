package de.adito.git.gui;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileDiff;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.List;

/**
 * Window that displays the list of changes found during a diff
 *
 * @author m.kaspera 05.10.2018
 */
public class DiffWindow extends JPanel {

    private List<IFileDiff> diffs;

    public DiffWindow(List<IFileDiff> pDiffs) {

        this.diffs = pDiffs;
            _initGui();
        }

        /**
         * sets up the GUI
         */
        private void _initGui () {
            setLayout(new BorderLayout());
            JTextPane oldVersionPane = new JTextPane();
            oldVersionPane.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
            JTextPane newVersionPane = new JTextPane();
            newVersionPane.setSelectionColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));

            try {
                for (IFileDiff fileDiff : diffs) {
                    _insertText(oldVersionPane.getStyledDocument(), fileDiff.getFilePath(EChangeSide.OLD) + "\n\n", Color.WHITE);
                    _insertText(newVersionPane.getStyledDocument(), fileDiff.getFilePath(EChangeSide.NEW) + "\n\n", Color.WHITE);
                    _insertChangeChunks(fileDiff.getFileChanges().getChangeChunks(), oldVersionPane, newVersionPane);
                    _insertText(oldVersionPane.getStyledDocument(), "\n\n---------------------------------------------------------------------------\n\n", Color.WHITE);
                    _insertText(newVersionPane.getStyledDocument(), "\n\n---------------------------------------------------------------------------\n\n", Color.WHITE);
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
            add(scrollPane, BorderLayout.CENTER);
        }

        /**
         * @param changeChunks List with IFileChangeChunk that describe the changes
         * @param aTextPane    JTextPane for the "old" side of the diff
         * @param bTextPane    JTextPane for the "new" side of the diff
         * @throws BadLocationException if the message is getting inserted out of bounds
         */
        private void _insertChangeChunks (List < IFileChangeChunk > changeChunks, JTextPane aTextPane, JTextPane
        bTextPane) throws BadLocationException {
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
                _insertText(aDocument, changeChunk.getALines(), aBGColor);
                _insertText(aDocument, changeChunk.getAParityLines(), parityBGColor);
                _insertText(bDocument, changeChunk.getBLines(), bBGColor);
                _insertText(bDocument, changeChunk.getBParityLines(), parityBGColor);
            }
        }

        /**
         * @param document StyledDocument of the JTextPane for which to insert the message
         * @param message  String that should be inserted into the JTextPane
         * @param color    The Background color for the Text getting inserted
         * @throws BadLocationException if the message is getting inserted out of bounds
         */
        private void _insertText (StyledDocument document, String message, Color color) throws BadLocationException {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setBackground(attr, color);
            int offset = document.getLength();
            document.insertString(offset, message, attr);
        }

    }
