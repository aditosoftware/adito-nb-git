package de.adito.git.gui.popup;

import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

/**
 * a implementation of the HeavyWightWindow to show all branches
 *
 * @author a.arnold, 12.11.2018
 */

public class PopupWindow extends JWindow {
    private final String title;
    private _WindowDisposer windowDisposer;

    public PopupWindow(Window parent, String pTitle, JComponent pComponent) {
        super(parent);
        windowDisposer = new _WindowDisposer();
        title = pTitle;
        _initGUI(pComponent);
        setMinimumSize(new Dimension(125, 200));
        setType(Type.POPUP);

        try {
            setAlwaysOnTop(true);
        } catch (SecurityException se) {
            throw new RuntimeException();
        }
    }

    private void _initGUI(JComponent pComponent) {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.gray));

        final int outerLine = 8;
        final int gab = 8;
        double fill = TableLayout.FILL;
        double[] cols = {outerLine, fill, outerLine};
        double[] rows = {outerLine,
                25, //Title
                gab,
                fill, //Content
                outerLine};

        panel.setLayout(new TableLayout(cols, rows));
        TableLayoutUtil tlu = new TableLayoutUtil(panel);
        tlu.add(0, 0, new MouseSensor(new HandlerNorthWest(this)));
        tlu.add(1, 0, new MouseSensor(new HandlerNorth(this)));
        tlu.add(2, 0, new MouseSensor(new HandlerNorthEast(this)));
        tlu.add(0, 1, 0, 3, new MouseSensor(new HandlerWest(this)));
        tlu.add(1, 1, new TitleLabel(new HandlerMovement(this), title));
        tlu.add(2, 1, 2, 3, new MouseSensor(new HandlerEast(this)));
        tlu.add(1, 2, new MouseSensor(new HandlerMovement(this)));
        tlu.add(1, 3, _createContentPanel(pComponent));
        tlu.add(0, 4, new MouseSensor(new HandlerSouthWest(this)));
        tlu.add(1, 4, new MouseSensor(new HandlerSouth(this)));
        tlu.add(2, 4, new MouseSensor(new HandlerSouthEast(this)));
        add(panel);
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

    @Override
    public void setVisible(boolean b) {
        pack();
        super.setVisible(b);
        SwingUtilities.invokeLater(() -> Toolkit.getDefaultToolkit().addAWTEventListener(windowDisposer, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.KEY_EVENT_MASK));
    }

    private class _WindowDisposer implements AWTEventListener {

        @Override
        public void eventDispatched(AWTEvent event) {
            if (event.getID() != WindowEvent.WINDOW_OPENED) {
                if (event.getSource() != PopupWindow.this || !((WindowEvent) event).getWindow().getType().equals(Type.POPUP)) {
                    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    dispose();
                }
            }
        }
    }
}

