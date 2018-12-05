package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.*;
import de.adito.git.gui.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

import static de.adito.git.gui.Constants.SCROLL_SPEED_INCREMENT;

/**
 * Class to handle the basic layout of the two panels that display the differences between two files
 *
 * @author m.kaspera 12.11.2018
 */
public class DiffPanel extends ChangeDisplayPanel implements IDiscardable {

    private final JTextPane lineNumbering = new JTextPane();
    private final JTextPane textPane = super._createNonWrappingTextPane();
    private final JScrollPane mainScrollPane = new JScrollPane();
    private final JScrollPane lineNumberingScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final String lineOrientation;
    private final EChangeSide changeSide;
    private final boolean useParityLines;
    private Disposable disposable;

    /**
     * @param pLineOrientation The side on which the lineNumbers should be (BorderLayout.EAST or BorderLayout.WEST)
     * @param pChangeSide which side of the IFileChangeDiff should be used for retrieving the lines
     * @param pChangeChunkList Observable for a list of IFileChangeChunks which always contains the IFileChangeChunks that should currently be displayed
     * @param pUseParityLines if parity lines from the IFileChangeChunks should be used
     */
    public DiffPanel(String pLineOrientation, EChangeSide pChangeSide, Observable<List<IFileChangeChunk>> pChangeChunkList, boolean pUseParityLines) {
        super(pChangeSide);
        changeSide = pChangeSide;
        useParityLines = pUseParityLines;
        if (!pLineOrientation.equals(BorderLayout.EAST) && !pLineOrientation.equals(BorderLayout.WEST)) {
            lineOrientation = BorderLayout.EAST;
        } else {
            lineOrientation = pLineOrientation;
        }
        _initGui();
        if (pChangeChunkList != null)
            setContent(pChangeChunkList);
    }

    /**
     * makes this DiffPanel's scrolling fixed to the scrolling of the passed JScrollPane
     *
     * @param pScrollPane JScrollPane that should determine the scrolling of the textPane and the lineNumbers of this DiffPanel
     */
    public void coupleToScrollPane(JScrollPane pScrollPane) {
        super.coupleScrollPanes(pScrollPane, mainScrollPane);
        super.coupleScrollPanes(pScrollPane, lineNumberingScrollPane);
    }

    /**
     * @return the ScrollPane that controls the Scrolling of the main textPane
     */
    public JScrollPane getMainScrollPane() {
        return mainScrollPane;
    }

    /**
     * @return the JTextPane that contains the diff-text
     */
    JTextPane getTextPane() {
        return textPane;
    }

    /**
     * disposes of the currently subscribed Observable (if that exists) and subscribes to the passed one
     *
     * @param pChangeChunkList {@code Observable<List<IFileChangeChunk>>} detailing the changes to the text in this panel
     */
    public void setContent(Observable<List<IFileChangeChunk>> pChangeChunkList) {
        if (disposable != null)
            disposable.dispose();
        disposable = pChangeChunkList.subscribe(this::_textChanged);
        textPane.setCaretPosition(0);
    }

    private void _textChanged(List<IFileChangeChunk> pChangeChunkList) {
        final int caretPosition = textPane.getCaretPosition();
        // insert the text from the IFileDiffs
      TextHighlightUtil.insertColoredText(textPane, pChangeChunkList, changeSide, useParityLines);
        super.writeLineNums(lineNumbering, pChangeChunkList, useParityLines);
        textPane.setCaretPosition(caretPosition);
        revalidate();
    }

    private void _initGui() {
        setLayout(new BorderLayout());

        // textPane should no be editable, but the text should still be normal
        textPane.setEditable(false);

        // text here should look disabled/grey and not be editable
        lineNumbering.setEnabled(false);

        // ScrollPane setup
        mainScrollPane.setViewportView(textPane);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        lineNumberingScrollPane.setViewportView(lineNumbering);
        super.coupleScrollPanes(mainScrollPane, lineNumberingScrollPane);

        // add the parts to the DiffPanel
        add(mainScrollPane, BorderLayout.CENTER);
        add(lineNumberingScrollPane, lineOrientation);

        // remove Borders from the parts and use the set Border to draw a border around the whole panel
        Border usedBorder = lineNumberingScrollPane.getBorder();
        lineNumberingScrollPane.setBorder(null);
        mainScrollPane.setBorder(null);
        setBorder(usedBorder);
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}