package de.adito.git.gui.popup;

import java.awt.*;

/**
 * A helper class to save the last locations of the popup window
 *
 * @author a.arnold, 15.11.2018
 */
class WindowBefore {
    final int x;
    final int y;
    final int height;
    final int width;

    WindowBefore(Point pLocation, Dimension pSize) {
        x = pLocation.x;
        y = pLocation.y;
        width = pSize.width;
        height = pSize.height;
    }
}
