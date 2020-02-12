package de.adito.git.gui.popup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

import static de.adito.git.gui.popup.PopupPanel.DRAG_BORDER_WIDTH;
import static javax.swing.SwingConstants.CENTER;

/**
 * A JLabel with the title of the popup
 *
 * @author a.arnold, 15.11.2018
 */
class TitlePanel extends JPanel
{
  TitlePanel(MouseDragHandler pMouseDragHandler, String pLabelName)
  {
    setLayout(new BorderLayout());
    JLabel titleLabel = new JLabel(pLabelName);
    titleLabel.setHorizontalAlignment(CENTER);
    titleLabel.setBorder(new EmptyBorder(0, 0, DRAG_BORDER_WIDTH, 0));
    add(titleLabel, BorderLayout.CENTER);
    setBackground(UIManager.getColor("adito.secondary.background.color"));
    addMouseListener(pMouseDragHandler);
    addMouseMotionListener(pMouseDragHandler);
  }
}
