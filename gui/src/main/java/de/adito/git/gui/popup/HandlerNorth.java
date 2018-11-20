package de.adito.git.gui.popup;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This handler resize the popup for clicking on the north side of the window
 *
 * @author a.arnold, 15.11.2018
 */
class HandlerNorth extends MouseDragHandler {
    HandlerNorth(PopupWindow pWindow) {
        super(pWindow, Cursor.N_RESIZE_CURSOR);
    }

    @Override
    protected Rectangle calc(MouseEvent e) {
        WindowBefore windowBefore = getWindowBefore();
        Point distance = getDistance(e);

        return new Rectangle(windowBefore.x, windowBefore.y + distance.y, windowBefore.width, windowBefore.height - distance.y);
    }
}
