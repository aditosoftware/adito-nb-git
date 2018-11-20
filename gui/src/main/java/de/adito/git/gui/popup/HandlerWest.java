package de.adito.git.gui.popup;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This handler resize the popup for clicking on the west side of the window
 *
 * @author a.arnold, 15.11.2018
 */
class HandlerWest extends MouseDragHandler {
    HandlerWest(PopupWindow pWindow) {
        super(pWindow, Cursor.W_RESIZE_CURSOR);
    }

    @Override
    protected Rectangle calc(MouseEvent e) {
        WindowBefore windowBefore = getWindowBefore();
        Point distance = getDistance(e);
        return new Rectangle(windowBefore.x + distance.x, windowBefore.y, windowBefore.width - distance.x, windowBefore.height);
    }
}
