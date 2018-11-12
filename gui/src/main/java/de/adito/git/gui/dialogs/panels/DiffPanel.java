package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.data.EChangeSide;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Class to handle the basic layout of the two panels that display the differences between two files
 *
 * @author m.kaspera 12.11.2018
 */
public class DiffPanel extends JPanel {

    private final static int SCROLL_SPEED_INCREMENT = 16;
    private final EChangeSide changeSide;
    private final JTextPane lineNumArea = new JTextPane();
    private final JTextPane textPane = new JTextPane();
    private final JScrollPane mainScrollPane = new JScrollPane();
    private final JScrollPane lineScrollPane = new JScrollPane();

    public DiffPanel(EChangeSide pChangeSide) {
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
    public void coupleToScrollPane(JScrollPane pScrollPane) {
        _coupleScrollPanes(pScrollPane, mainScrollPane);
        _coupleScrollPanes(pScrollPane, lineScrollPane);
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
    public JTextPane getTextPane() {
        return textPane;
    }

    /**
     * @return JTextPane that contains only the line-numbering
     */
    public JTextPane getLineNumArea() {
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