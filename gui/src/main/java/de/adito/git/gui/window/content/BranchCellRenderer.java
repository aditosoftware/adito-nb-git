package de.adito.git.gui.window.content;

import de.adito.git.api.data.IBranch;
import de.adito.git.gui.icon.SwingIconLoaderImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static de.adito.git.gui.Constants.CARET_RIGHT;

/**
 * This renderer is for the popup window.
 * Every row gets an arrow, to show that other options are possible
 * There are overridden functions because performance reasons which are implemented from the {@link DefaultListCellRenderer}.
 *
 * @author a.arnold, 21.11.2018
 */
class BranchCellRenderer extends JPanel implements ListCellRenderer<IBranch>
{
  private JLabel leftLabel = new JLabel();
  private static final int LIST_SPACING_TOP_BOTTOM = 2;
  private static final int LIST_ELEMENTS_OFFSET_LEFT = 18;

  BranchCellRenderer()
  {
    leftLabel.setBorder(new EmptyBorder(LIST_SPACING_TOP_BOTTOM, LIST_ELEMENTS_OFFSET_LEFT, LIST_SPACING_TOP_BOTTOM, 0));
    JLabel rightLabel = new JLabel();
    Icon arrowIcon = new SwingIconLoaderImpl().getIcon(CARET_RIGHT);
    rightLabel.setIcon(arrowIcon);
    setLayout(new BorderLayout());
    add(leftLabel, BorderLayout.CENTER);
    add(rightLabel, BorderLayout.EAST);
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IBranch> pList, IBranch pBranch, int pIndex, boolean pIsSelected, boolean pCellFocus)
  {
    setComponentOrientation(pList.getComponentOrientation());

    if (pIsSelected)
    {
      setBackground(pList.getSelectionBackground());
      setForeground(pList.getSelectionForeground());
    }
    else
    {
      setBackground(pList.getBackground());
      setForeground(pList.getForeground());
    }
    setEnabled(pList.isEnabled());


    leftLabel.setText(pBranch.getSimpleName());
    return this;
  }

  /**
   * see {@link }
   * <p>
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   *
   * @return <code>true</code> if the background is completely opaque
   * and differs from the JList's background;
   * <code>false</code> otherwise
   * @since 1.5
   */
  @Override
  public boolean isOpaque()
  {
    Color back = getBackground();
    Component p = getParent();
    if (p != null)
    {
      p = p.getParent();
    }
    // p should now be the JList.
    boolean colorMatch = (back != null) && (p != null) &&
        back.equals(p.getBackground()) &&
        p.isOpaque();
    return !colorMatch && super.isOpaque();
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   *
   * @since 1.5
   */
  @Override
  public void repaint()
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void repaint(long pTm, int pX, int pY, int pWidth, int pHeight)
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void repaint(Rectangle pR)
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  protected void firePropertyChange(String pPropertyName, Object pOldValue, Object pNewValue)
  {
    // Strings get interned...
    if ("text".equals(pPropertyName)
        || (("font".equals(pPropertyName) || "foreground".equals(pPropertyName))
        && pOldValue != pNewValue
        && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null))
    {

      super.firePropertyChange(pPropertyName, pOldValue, pNewValue);
    }
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, byte pOldValue, byte pNewValue)
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, char pOldValue, char pNewValue)
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, short pOldValue, short pNewValue)
  {
    //Overridden for performance reasons.
  }

  /**
   * .
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, int pOldValue, int pNewValue)
  {
    //Overridden for performance reasons
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, long pOldValue, long pNewValue)
  {
    //Overridden for performance reasons.

  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, float pOldValue, float pNewValue)
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, double pOldValue, double pNewValue)
  {
    //Overridden for performance reasons.
  }

  /**
   * Overridden for performance reasons.
   * See the <a href="#override">Implementation Note</a>
   * for more information.
   */
  @Override
  public void firePropertyChange(String pPropertyName, boolean pOldValue, boolean pNewValue)
  {
    //Overridden for performance reasons.
  }
}
