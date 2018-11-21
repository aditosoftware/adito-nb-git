package de.adito.git.gui.popup;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * An abstract class to handle all mouse handler
 *
 * @author a.arnold, 15.11.2018
 */
abstract class MouseDragHandler extends MouseAdapter {
    private final PopupWindow window;
    private final int cursorType;
    private final Point mouseBefore;
    private WindowBefore windowBefore;
    private Dimension minDimension;

    public MouseDragHandler(PopupWindow pWindow, int pCursorType) {
        window = pWindow;
        minDimension = new Dimension(125, 200);
        cursorType = pCursorType;
        mouseBefore = new Point();
        windowBefore = new WindowBefore(new Point(), new Dimension());
    }

    WindowBefore getWindowBefore() {
        return windowBefore;
    }

    Point getDistance(MouseEvent e) {
        Point p = new Point();
        p.x = e.getLocationOnScreen().x - mouseBefore.x;
        p.y = e.getLocationOnScreen().y - mouseBefore.y;
        return p;
    }

    PopupWindow getWindow() {
        return window;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseBefore.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y);
        windowBefore = new WindowBefore(window.getLocation(), window.getSize());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        window.setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        window.setCursor(null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Rectangle calculatedRec = calc(e);
        if(calculatedRec.getWidth() < minDimension.width){
            calculatedRec.setSize(minDimension.width, calculatedRec.height);
            calculatedRec.setLocation(window.getX(), calculatedRec.y);
        }
        if(calculatedRec.getHeight() < minDimension.height) {
            calculatedRec.setSize(calculatedRec.width, minDimension.height);
            calculatedRec.setLocation(calculatedRec.x, window.getY());
        }
        window.setBounds(calculatedRec);
    }

    protected abstract Rectangle calc(MouseEvent e);
}
