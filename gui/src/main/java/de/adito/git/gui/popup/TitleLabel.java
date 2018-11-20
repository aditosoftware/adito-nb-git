package de.adito.git.gui.popup;

import javax.swing.*;

/**
 * A JLabel with the title of the popup
 *
 * @author a.arnold, 15.11.2018
 */
class TitleLabel extends JLabel {
    TitleLabel(MouseDragHandler pMouseDragHandler, String pLabelName) {
        setHorizontalAlignment(CENTER);
        setText(pLabelName);
        addMouseListener(pMouseDragHandler);
        addMouseMotionListener(pMouseDragHandler);
    }
}
