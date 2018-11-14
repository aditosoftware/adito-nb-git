package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.gui.TextHighlightUtil;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author m.kaspera 12.11.2018
 */
class ChangeDisplayPanel extends JPanel {

    private final EChangeSide changeSide;

    ChangeDisplayPanel(EChangeSide pChangeSide) {
        changeSide = pChangeSide;
    }

    /**
     * Makes the masterScrollPane determine the scrolling behaviour/speed of the slaveScrollPane.
     * Both Panes are then  intertwined and cannot be scrolled independently
     *
     * @param masterScrollPane ScrollPane that will control scrolling behaviour. Is notified when the slave scrolls and does scroll then, too
     * @param slaveScrollPane  ScrollPane that is linked to the master. Scrolling in the master also means scrolling in the slave, and vice versa
     */
    void coupleScrollPanes(JScrollPane masterScrollPane, JScrollPane slaveScrollPane) {
        slaveScrollPane.getVerticalScrollBar().setModel(masterScrollPane.getVerticalScrollBar().getModel());
        slaveScrollPane.setWheelScrollingEnabled(false);
        slaveScrollPane.addMouseWheelListener(masterScrollPane::dispatchEvent);
    }

    void writeLineNums(JTextPane pLineNumberingPane, List<IFileChangeChunk> pChangeChunkList, boolean pUseParityLines) {
        TextHighlightUtil.insertColoredLineNumbers(pLineNumberingPane, pChangeChunkList, changeSide, pUseParityLines);
    }

    /**
     * Removes all Components registered on the JPanel and re-validates/repaints it
     *
     * @param panel JPanel from which to remove all Components
     */
    void _clearPanel(JPanel panel) {
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

    JTextPane _createNonWrappingTextPane() {
        return new NonWrappingTextPane();
    }

    /**
     * JTextPane that does not wrap, which means it is actually useful in a ScrollPane
     * since the ScrollPane can actually do some work, instead of the JTextPane doing it needlessly.
     * Also useful for making sure the JTextPane doesn't get longer than intended because of line-wrapping
     */
    private static class NonWrappingTextPane extends JTextPane {
        NonWrappingTextPane() {
            super();
        }

        // Override getScrollableTracksViewportWidth
        // to preserve the full width of the text
        @Override
        public boolean getScrollableTracksViewportWidth() {
            Component parent = getParent();
            ComponentUI ui = getUI();

            return parent == null || (ui.getPreferredSize(this).width <= parent
                    .getSize().width);
        }
    }
}
