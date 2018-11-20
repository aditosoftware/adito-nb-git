package de.adito.git.gui.popup;

import javax.swing.*;

/**
 * Mouse provider for the MouseListener
 *
 * @author a.arnold, 15.11.2018
 */
class MouseSensor extends JPanel {

    MouseSensor(MouseDragHandler pDragHandler) {
        addMouseListener(pDragHandler);
        addMouseMotionListener(pDragHandler);
    }
}
