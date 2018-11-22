package de.adito.git.gui.popup;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This handler moves the popup for clicking on the middle of the window
 *
 * @author a.arnold, 15.11.2018
 */
class HandlerMovement extends MouseDragHandler {
    HandlerMovement(PopupWindow pWindow) {
        super(pWindow, Cursor.getDefaultCursor().getType());
    }

    @Override
    protected Rectangle calc(MouseEvent e) {
        WindowBefore windowBefore = getWindowBefore();
        Point distance = getDistance(e);
        return new Rectangle(windowBefore.x + distance.x, windowBefore.y + distance.y, windowBefore.width, windowBefore.height);
    }
}
