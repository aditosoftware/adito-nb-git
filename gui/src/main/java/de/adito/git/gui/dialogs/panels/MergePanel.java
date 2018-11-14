package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.impl.data.FileChangeChunkImpl;
import io.reactivex.disposables.Disposable;
import org.eclipse.jgit.diff.Edit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

import static de.adito.git.gui.Constants.SCROLL_SPEED_INCREMENT;

/**
 * Panel for the YOURS and THEIRS version of the dialog in a merge conflict (the panel on the left/right)
 * Contains the YOURS/THEIRS version with the changes to the fork-point version marked, the line numbers and the accept/discard change buttons
 *
 * @author m.kaspera 12.11.2018
 */
public class MergePanel extends ChangeDisplayPanel implements IDiscardable {

    private static final int ADDITIONAL_HEIGHT_PIXELS = 3;
    private static final int BUTTON_BAR_WIDTH = 35;
    private static final int HALF_BUTTON_HEIGHT = 8;
    private final DiffPanel diffPanel;
    private final String lineOrientation;
    private final JScrollPane buttonPanelScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final JPanel buttonBar = new JPanel();
    private final IMergeDiff.CONFLICT_SIDE conflictSide;
    private final IMergeDiff mergeDiff;
    private final ImageIcon discardIcon;
    private final ImageIcon acceptIcon;
    private final Disposable disposable;

    public MergePanel(IMergeDiff.CONFLICT_SIDE pConflictSide, @NotNull IMergeDiff pMergeDiff, @NotNull ImageIcon pAcceptYoursIcon, @NotNull ImageIcon pAcceptTheirsIcon, @NotNull ImageIcon pDiscardIcon) {
        super(EChangeSide.NEW);
        conflictSide = pConflictSide;
        mergeDiff = pMergeDiff;
        acceptIcon = pConflictSide == IMergeDiff.CONFLICT_SIDE.YOURS ? pAcceptYoursIcon : pAcceptTheirsIcon;
        discardIcon = pDiscardIcon;
        if (conflictSide == IMergeDiff.CONFLICT_SIDE.THEIRS)
            lineOrientation = BorderLayout.WEST;
        else
            lineOrientation = BorderLayout.EAST;
        diffPanel = new DiffPanel(lineOrientation, EChangeSide.NEW, pMergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks(), false);
        _initGui();
        disposable = pMergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks().subscribe(pIFileChangeChunks -> {
            _clearPanel(buttonBar);
            _initButtonPanel(pIFileChangeChunks);
        });
    }

    private void _initButtonPanel(List<IFileChangeChunk> pChangeChunks) {
        final int lineHeight = diffPanel.getTextPane().getFontMetrics(diffPanel.getTextPane().getFont()).getHeight();
        buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.Y_AXIS));
        buttonBar.setMinimumSize(new Dimension(BUTTON_BAR_WIDTH, 0));
        buttonBar.setMaximumSize(new Dimension(BUTTON_BAR_WIDTH, Integer.MAX_VALUE));
        // since the mergePanel has no parityLines, the number of Lines * the height of one line should be the total size of the other components
//        buttonBar.setPreferredSize(new Dimension(35, changeChunkList.get(changeChunkList.size() - 1).getAEnd() * lineHeight));
        // start with -3 because else the the buttons are not aligned with the lines, probably due to borders or something
        int heightBefore = -ADDITIONAL_HEIGHT_PIXELS;
        for (IFileChangeChunk changeChunk : pChangeChunks) {
            if (changeChunk.getChangeType() != EChangeType.SAME) {
                heightBefore = _insertButtonsForChunk(changeChunk, heightBefore, lineHeight);
            }
        }
        buttonBar.add(Box.createRigidArea(new Dimension(0, ((pChangeChunks.get(pChangeChunks.size() - 1).getBEnd() + 1) * lineHeight - (heightBefore)) + ADDITIONAL_HEIGHT_PIXELS)));
    }

    /**
     * @param changeChunk  IFileChangeChunk for which to draw the button
     * @param heightBefore number of pixels from the very top down to the middle of the last buttonPanel
     * @param lineHeight   the height of one line of text in the JPanel that contains the text of the IFileChangeChunks
     */
    private int _insertButtonsForChunk(IFileChangeChunk changeChunk, int heightBefore, int lineHeight) {
        JPanel dualButtonPanel = new JPanel(new BorderLayout());
        JButton discardChangeButton = _createButton(discardIcon, "discard change");
        JButton acceptChangeButton = _createButton(acceptIcon, "accept this change");
        acceptChangeButton.setBorder(null);
        discardChangeButton.setBorder(null);
        dualButtonPanel.add(discardChangeButton, BorderLayout.CENTER);
        dualButtonPanel.add(acceptChangeButton, lineOrientation);
        int currentHeight = (int) ((changeChunk.getBStart() + (double) (changeChunk.getBEnd() - changeChunk.getBStart()) / 2) * lineHeight);
        buttonBar.add(Box.createRigidArea(new Dimension(0, (currentHeight - (heightBefore + HALF_BUTTON_HEIGHT)))));
        buttonBar.add(dualButtonPanel);
//        discardChangeButton.setBounds(0, (changeChunk.getBStart() + (changeChunk.getBEnd() - changeChunk.getBStart()) / 2) * lineHeight, 16, lineHeight);
//        acceptChangeButton.setBounds(20, (changeChunk.getBStart() + (changeChunk.getBEnd() - changeChunk.getBStart()) / 2) * lineHeight, 16, lineHeight);
        discardChangeButton.addActionListener(e -> mergeDiff.getDiff(conflictSide).getFileChanges().replace(changeChunk,
                new FileChangeChunkImpl(
                        new Edit(changeChunk.getAStart(), changeChunk.getAEnd(), changeChunk.getBStart(), changeChunk.getBEnd()),
                        changeChunk.getALines(),
                        changeChunk.getBLines(),
                        EChangeType.SAME)));
        acceptChangeButton.addActionListener(e -> mergeDiff.acceptChunk(changeChunk, conflictSide));
        return currentHeight + HALF_BUTTON_HEIGHT;
    }

    private JButton _createButton(ImageIcon icon, String toolTip) {
        JButton createdButton = new JButton(icon);
        createdButton.setToolTipText(toolTip);
        return createdButton;
    }

    private void _initGui() {
        setLayout(new BorderLayout());

        buttonPanelScrollPane.add(buttonBar);
        buttonPanelScrollPane.setViewportView(buttonBar);
        buttonPanelScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
        buttonPanelScrollPane.setBorder(null);

        super.coupleScrollPanes(diffPanel.getMainScrollPane(), buttonPanelScrollPane);

        add(diffPanel, BorderLayout.CENTER);
        add(buttonPanelScrollPane, lineOrientation);

        // only have the border around the whole Panel, not the part DiffPanel
        final Border usedBorder = diffPanel.getBorder();
        diffPanel.setBorder(null);
        setBorder(usedBorder);
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}
