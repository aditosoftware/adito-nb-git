package de.adito.git.gui.swing;

import org.jetbrains.annotations.Nullable;

import javax.swing.FocusManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * JTextField that can have a String as placholder text
 *
 * @author m.kaspera, 13.03.2020
 */
public class TextFieldWithPlaceholder extends JTextField implements FocusListener
{
  private final Color PLACEHOLDER_TEXT_COLOR = UIManager.getColor("adito.menu.text.disabledForeground");
  private final String placeholder;

  /**
   * @param pPlaceholder Text that is used as placeholder
   */
  public TextFieldWithPlaceholder(@Nullable String pPlaceholder)
  {
    this(null, pPlaceholder);
  }

  /**
   * @param text         this text will be set in the textfield at the start
   * @param pPlaceholder Text that is used as placeholder
   */
  public TextFieldWithPlaceholder(@Nullable String text, @Nullable String pPlaceholder)
  {
    super(text);
    placeholder = pPlaceholder;
    if (placeholder != null)
      addFocusListener(this);
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    if (placeholder != null && getText().isEmpty() && FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != this)
    {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(PLACEHOLDER_TEXT_COLOR);
      g2.setFont(getFont().deriveFont(Font.ITALIC));
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int y = g2.getFontMetrics(g2.getFont()).getAscent() + (getHeight() - g2.getFontMetrics(g2.getFont()).getHeight()) / 2;
      g2.drawString(placeholder, 5, y);
      g2.dispose();
    }
  }

  @Override
  public void focusGained(FocusEvent e)
  {
    repaint();
  }

  @Override
  public void focusLost(FocusEvent e)
  {
    repaint();
  }
}
