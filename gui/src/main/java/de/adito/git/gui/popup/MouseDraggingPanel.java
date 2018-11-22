package de.adito.git.gui.popup;

import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A Panel for all mouse handlers.
 * This {@link JPanel} is a "popup" without border.
 *
 * @author a.arnold, 22.11.2018
 */
class MouseDraggingPanel extends JPanel {

    MouseDraggingPanel(JComponent pComponent, String pTitle, PopupWindow pWindow) {
        setBorder(new LineBorder(Color.gray));
        final int outerLine = 8;
        final int gab = 8;
        double fill = TableLayout.FILL;
        double[] cols = {outerLine, fill, outerLine};
        double[] rows = {outerLine,
                10, //Title
                gab,
                fill, //Content
                outerLine};

        setLayout(new TableLayout(cols, rows));
        TableLayoutUtil tlu = new TableLayoutUtil(this);
        tlu.add(0, 0, new MouseSensor(new HandlerNorthWest(pWindow)));
        tlu.add(1, 0, new MouseSensor(new HandlerNorth(pWindow)));
        tlu.add(2, 0, new MouseSensor(new HandlerNorthEast(pWindow)));
        tlu.add(0, 1, 0, 3, new MouseSensor(new HandlerWest(pWindow)));
        tlu.add(1, 1, new TitleLabel(new HandlerMovement(pWindow), pTitle));
        tlu.add(2, 1, 2, 3, new MouseSensor(new HandlerEast(pWindow)));
        tlu.add(1, 2, new MouseSensor(new HandlerMovement(pWindow)));
        tlu.add(1, 3, _createContentPanel(pComponent));
        tlu.add(0, 4, new MouseSensor(new HandlerSouthWest(pWindow)));
        tlu.add(1, 4, new MouseSensor(new HandlerSouth(pWindow)));
        tlu.add(2, 4, new MouseSensor(new HandlerSouthEast(pWindow)));
    }

    private JComponent _createContentPanel(JComponent pComponent) {
        JScrollPane contentScrollPane = new JScrollPane(pComponent);
        contentScrollPane.setPreferredSize(new Dimension(200, 350));
        contentScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentScrollPane.setViewportBorder(null);
        contentScrollPane.getViewport().setBorder(null);
        contentScrollPane.setBorder(null);
        return contentScrollPane;
    }

}