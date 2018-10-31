package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileChanges;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.TextHighlightUtil;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.impl.data.FileChangeChunkImpl;
import io.reactivex.disposables.Disposable;
import org.eclipse.jgit.diff.Edit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Dialog/Panel for displaying the merge-conflicts
 * Also offers the possibility of accepting changes from both the
 * merge-base/current side and the side that is being merged
 *
 * @author m.kaspera 22.10.2018
 */
class MergeConflictResolutionDialog extends JPanel implements IDiscardable {

    private final static int SCROLL_SPEED_INCREMENT = 16;
    private final static String ACCEPT_CHANGE_ICON = "/de/adito/git/gui/icons/acceptChangeRight.png";
    private final static String ACCEPT_CHANGE_RIGHT_ICON = "/de/adito/git/gui/icons/acceptChange.png";
    private final static String DISCARD_CHANGE_ICON = "/de/adito/git/gui/icons/discardChange.png";
    private final IMergeDiff mergeDiff;
    private final IIconLoader iconLoader;
    private final JTextPane currentBranchVersionPane = new JTextPane();
    private final JTextPane forkPointVersionPane = new JTextPane();
    private final JTextPane mergeBranchVersionPane = new JTextPane();
    private final JPanel leftButtonPanel = new JPanel(null);
    private final JPanel rightButtonPanel = new JPanel(null);
    private int caretPosition = 0;
    private Set<Disposable> disposables = new HashSet<>(25);
    private _PaneDocumentListener paneDocumentListener = new _PaneDocumentListener();

    @Inject
    MergeConflictResolutionDialog(IIconLoader pIconLoader, @Assisted IMergeDiff pMergeDiff) {
        mergeDiff = pMergeDiff;
        iconLoader = pIconLoader;
        _initGui();
        _writeText(IMergeDiff.CONFLICT_SIDE.YOURS, currentBranchVersionPane);
        _writeText(IMergeDiff.CONFLICT_SIDE.THEIRS, mergeBranchVersionPane);
    }

    private void _initGui() {
        // create a panel in a scrollPane for each of the textPanes
        setLayout(new BorderLayout());
        JPanel currentBranchPanel = _initMergePanel(IMergeDiff.CONFLICT_SIDE.YOURS, leftButtonPanel, currentBranchVersionPane);
        JPanel mergeBranchPanel = _initMergePanel(IMergeDiff.CONFLICT_SIDE.THEIRS, rightButtonPanel, mergeBranchVersionPane);

        JPanel forkPointPanel = new JPanel(new BorderLayout());
        forkPointVersionPane.setEditable(true);
        forkPointVersionPane.getDocument().addDocumentListener(paneDocumentListener);
        forkPointPanel.add(forkPointVersionPane, BorderLayout.CENTER);

        JScrollPane currentBranchScrollPane = new JScrollPane(currentBranchPanel);
        currentBranchScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        JScrollPane forkPointScrollPane = new JScrollPane(forkPointPanel);
        forkPointScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        JScrollPane mergeBranchScrollPane = new JScrollPane(mergeBranchPanel);
        mergeBranchScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);

        // add a splitPane on top of another splitPane so there are three sub-windows for each textPane/version of the file
        JSplitPane currentToForkSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, currentBranchScrollPane, forkPointScrollPane);
        JSplitPane threeWayPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, currentToForkSplit, mergeBranchScrollPane);
        currentToForkSplit.setResizeWeight(0.5);
        // 0.66 because the left side contains two sub-windows, the right only one
        threeWayPane.setResizeWeight(0.66);
        JPanel diffPanel = new JPanel(new BorderLayout());
        diffPanel.add(threeWayPane, BorderLayout.CENTER);
        diffPanel.add(_initAcceptAllPanel(), BorderLayout.NORTH);
        diffPanel.setPreferredSize(new Dimension(1600, 900));
        add(diffPanel, BorderLayout.CENTER);
    }

    /**
     * @param conflictSide which side of the conflict the panel is for
     * @param buttonPanel  JPanel that is designated to hold the accept/discard buttons for the IFileChangeChunks in this Panel
     * @param textPane     JTextPane will contain the text of the IFileChangeChunks
     * @return JPanel with all needed components
     */
    private JPanel _initMergePanel(IMergeDiff.CONFLICT_SIDE conflictSide, JPanel buttonPanel, JTextPane textPane) {
        JPanel mergePanel = new JPanel(new BorderLayout());
        buttonPanel.setPreferredSize(new Dimension(35, 1200));
        buttonPanel.setMinimumSize(new Dimension(35, 0));
        buttonPanel.setMaximumSize(new Dimension(35, Integer.MAX_VALUE));
        mergePanel.add(textPane, BorderLayout.CENTER);
        mergePanel.add(buttonPanel, conflictSide == IMergeDiff.CONFLICT_SIDE.YOURS ? BorderLayout.EAST : BorderLayout.WEST);
        return mergePanel;
    }

    private JPanel _initAcceptAllPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(_initAcceptAllButtonPanel("accept remaining YOURS changes", "discard all remaining YOURS changes", IMergeDiff.CONFLICT_SIDE.YOURS), BorderLayout.WEST);
        topPanel.add(_initAcceptAllButtonPanel("accept remaining THEIRS changes", "discard all remaining THEIRS changes", IMergeDiff.CONFLICT_SIDE.THEIRS), BorderLayout.EAST);
        return topPanel;
    }

    private JPanel _initAcceptAllButtonPanel(String acceptButtonText, String discardButtonText, IMergeDiff.CONFLICT_SIDE conflictSide) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        JButton acceptChangesButton = new JButton(acceptButtonText);
        JButton discardChangesButton = new JButton(discardButtonText);
        acceptChangesButton.addActionListener(e -> _acceptAllChanges(conflictSide));
        discardChangesButton.addActionListener(e -> _discardAllChanges(conflictSide));
        buttonPanel.add(acceptChangesButton);
        buttonPanel.add(discardChangesButton);
        return buttonPanel;
    }

    private void _writeText(IMergeDiff.CONFLICT_SIDE conflictSide, JTextPane textPane) {
        JPanel buttonPanel;
        String icon;
        if (conflictSide == IMergeDiff.CONFLICT_SIDE.YOURS) {
            icon = ACCEPT_CHANGE_ICON;
            buttonPanel = leftButtonPanel;
        } else {
            icon = ACCEPT_CHANGE_RIGHT_ICON;
            buttonPanel = rightButtonPanel;
        }
        Consumer<IFileChangeChunk> insertCall;
        insertCall = changeChunk -> mergeDiff.acceptChunk(changeChunk, conflictSide);
        disposables.add(mergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks().subscribe(changeChunks -> {
            // reset the text, else the text will just be appended to the textPane after each accepted change
            paneDocumentListener.disable();
            try {
                textPane.setText("");
                _clearPanel(buttonPanel);
                if (conflictSide == IMergeDiff.CONFLICT_SIDE.YOURS) {
                    forkPointVersionPane.setText("");
                }
                for (IFileChangeChunk changeChunk : changeChunks) {
                    if (changeChunk.getChangeType() != EChangeType.SAME) {
                        _insertButtonsForChunk(buttonPanel, icon, changeChunk, insertCall, mergeDiff.getDiff(conflictSide).getFileChanges(), textPane.getFontMetrics(textPane.getFont()).getHeight());
                    }
                    TextHighlightUtil.insertText(textPane.getStyledDocument(), changeChunk.getBLines(), changeChunk.getChangeType());
                    if (conflictSide == IMergeDiff.CONFLICT_SIDE.YOURS)
                        TextHighlightUtil.insertText(forkPointVersionPane.getStyledDocument(), changeChunk.getALines(), Color.WHITE);
                }
            } finally {
                forkPointVersionPane.setCaretPosition(caretPosition);
                paneDocumentListener.enable();
            }
        }));
    }

    @Override
    public void discard() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

    /**
     * @param drawArea      JPanel on which to draw the buttons on
     * @param iconUrl       the URI for the icon on the accept button
     * @param changeChunk   IFileChangeChunk for which to draw the button
     * @param acceptOnClick {@code Consumer<IFileChangeChunk>} that should be executed when the user clicks the "accept change" button
     * @param fileChanges   IFileChanges that contains the list of IFileChangeChunks
     * @param lineHeight    the height of one line of text in the JPanel that contains the text of the IFileChangeChunks
     */
    private void _insertButtonsForChunk(JPanel drawArea, String iconUrl, IFileChangeChunk changeChunk, Consumer<IFileChangeChunk> acceptOnClick, IFileChanges fileChanges, int lineHeight) {
        JButton discardChangeButton = new JButton("discard change", iconLoader.getIcon(DISCARD_CHANGE_ICON));
        JButton acceptChangeButton = new JButton("accept change", iconLoader.getIcon(iconUrl));
        drawArea.add(acceptChangeButton);
        drawArea.add(discardChangeButton);
        discardChangeButton.setBounds(0, (changeChunk.getBStart() + (changeChunk.getBEnd() - changeChunk.getBStart()) / 2) * lineHeight, 16, lineHeight);
        discardChangeButton.addActionListener(e -> fileChanges.replace(changeChunk,
                new FileChangeChunkImpl(
                        new Edit(changeChunk.getAStart(), changeChunk.getAEnd(), changeChunk.getBStart(), changeChunk.getBEnd()),
                        changeChunk.getALines(),
                        changeChunk.getBLines(),
                        EChangeType.SAME)));
        acceptChangeButton.setBounds(20, (changeChunk.getBStart() + (changeChunk.getBEnd() - changeChunk.getBStart()) / 2) * lineHeight, 16, lineHeight);
        acceptChangeButton.addActionListener(e -> acceptOnClick.accept(changeChunk));
    }

    /**
     * @param conflictSide CONFLICT_SIDE whose changes should be accepted
     */
    private void _acceptAllChanges(IMergeDiff.CONFLICT_SIDE conflictSide) {
        for (IFileChangeChunk changeChunk : mergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst()) {
            if (changeChunk.getChangeType() != EChangeType.SAME) {
                mergeDiff.acceptChunk(changeChunk, conflictSide);
            }
        }
    }

    /**
     * @param conflictSide CONFLICT_SIDE whose changes should be discarded
     */
    private void _discardAllChanges(IMergeDiff.CONFLICT_SIDE conflictSide) {
        for (IFileChangeChunk changeChunk : mergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst()) {
            if (changeChunk.getChangeType() != EChangeType.SAME) {
                mergeDiff.discardChange(changeChunk, conflictSide);
            }
        }
    }

    /**
     * Removes all Components registered on the JPanel and re-validates/repaints it
     *
     * @param panel JPanel from which to remove all Components
     */
    private void _clearPanel(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JButton) {
                for (ActionListener actionListener : ((JButton) component).getActionListeners()) {
                    ((JButton) component).removeActionListener(actionListener);
                }
            }
        }
        panel.removeAll();
        panel.validate();
        panel.repaint();
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
                        caretPosition = forkPointVersionPane.getCaretPosition() + insertedText.length();
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
                caretPosition = forkPointVersionPane.getCaretPosition();
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

}
