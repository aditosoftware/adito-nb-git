package de.adito.git.gui.swing;

import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel that represents a simple description-line
 *
 * @author w.glanzer, 12.02.2019
 */
public class LinedDecorator extends JPanel
{

  public LinedDecorator(@NotNull String pTitle, @Nullable Integer pFixedHeight)
  {
    super(new BorderLayout());
    JLabel name = new JLabel(pTitle);
    add(name, BorderLayout.WEST);
    add(new _Separator(new JSeparator().getForeground()), BorderLayout.CENTER);

    // Height
    int height = pFixedHeight == null ? name.getPreferredSize().height : pFixedHeight;
    setMinimumSize(new Dimension(0, height));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
  }

  /**
   * Separator-Line-Impl
   */
  private static class _Separator extends JComponent
  {
    private final Color color;

    public _Separator(Color pColor)
    {
      color = pColor;
      setOpaque(true);
      setBorder(new EmptyBorder(0, 3, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
      g.setColor(color);
      g.drawLine(getInsets().left, getHeight() / 2, getWidth() - getInsets().left - getInsets().right, getHeight() / 2);
    }
  }

}

