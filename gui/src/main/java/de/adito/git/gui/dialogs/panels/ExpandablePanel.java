package de.adito.git.gui.dialogs.panels;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.Constants;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.impl.Util;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A panel that get two parts:
 * The one labelled upper part is always shown, the one labelled lower part/component ist only shown if the user clicks on the "Show more" button separating the
 * two components.
 * If there are some information that should be extracted after the user is finished with the dialog, that information has to be put into the upper panel/component.
 * The lower component is only used to show addtional info or hints
 *
 * @author m.kaspera, 17.06.2020
 */
public class ExpandablePanel<T> extends AditoBaseDialog<T>
{

  private static final double GAP_VERTICAL = 10;
  private static final double SEPARATING_PANEL_ICON_GAP = 3;
  private static final double SEPARATING_PANEL_SEPARATOR_GAP = 7;

  private final JComponent lowerComponent;
  private final TableLayoutUtil tlu;
  private final AditoBaseDialog<T> upperComponent;

  @Inject
  public ExpandablePanel(IIconLoader pIconLoader, @Assisted("upper") AditoBaseDialog<T> pUpperComponent, @Assisted("lower") JComponent pLowerComponent)
  {
    upperComponent = pUpperComponent;
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    double[] cols = {pref, fill};
    double[] rows = {pref,
                     GAP_VERTICAL,
                     pref,
                     GAP_VERTICAL,
                     pref};
    setLayout(new TableLayout(cols, rows));
    tlu = new TableLayoutUtil(this);
    lowerComponent = pLowerComponent;
    tlu.add(0, 0, pUpperComponent);
    tlu.add(0, 2, 1, 2, new _SeparatingPanel(pIconLoader, this::_showLowerComponent, this::_hideLowerComponent));
  }

  private void _showLowerComponent()
  {
    tlu.add(0, 4, lowerComponent);
    revalidate();
    // this re-adjusts the size of the dialog to match the contents. Only setting the size and calling revalidate does not work
    ((Dialog) getRootPane().getParent()).pack();
    getRootPane().revalidate();
    repaint();
  }

  private void _hideLowerComponent()
  {
    remove(lowerComponent);
    // this re-adjusts the size of the dialog to match the contents. Only setting the size and calling revalidate does not work
    ((Dialog) getRootPane().getParent()).pack();
    revalidate();
    repaint();
  }

  @Nullable
  @Override
  public String getMessage()
  {
    return upperComponent.getMessage();
  }

  @Nullable
  @Override
  public T getInformation()
  {
    return upperComponent.getInformation();
  }

  /**
   * Panel consisting of three visual elements: One icon that changes form depending on the current state of the panel, a label that shows "Show more"
   */
  private static class _SeparatingPanel extends JPanel
  {

    private final JLabel expandIcon = new JLabel();
    private final JLabel showMoreLabel = new JLabel(Util.getResource(ExpandablePanel.class, "expandablePanelShowMoreText"));
    private final Runnable showLowerComponent;
    private final Runnable hideLowerComponent;
    private final Icon expandedIcon;
    private final Icon hiddenIcon;
    private boolean isExpaned = false;

    public _SeparatingPanel(IIconLoader pIconLoader, Runnable pShowLowerComponent, Runnable pHideLowerComponent)
    {
      super();
      double fill = TableLayout.FILL;
      double pref = TableLayout.PREFERRED;

      double[] cols = {pref, SEPARATING_PANEL_ICON_GAP, pref, SEPARATING_PANEL_SEPARATOR_GAP, fill};
      double[] rows = {pref};
      setLayout(new TableLayout(cols, rows));
      TableLayoutUtil tlu = new TableLayoutUtil(this);
      showLowerComponent = pShowLowerComponent;
      hideLowerComponent = pHideLowerComponent;
      expandedIcon = pIconLoader.getIcon(Constants.SHOW_LESS_DIALOG_OPTION);
      hiddenIcon = pIconLoader.getIcon(Constants.SHOW_MORE_DIALOG_OPTION);
      expandIcon.setIcon(hiddenIcon);
      tlu.add(0, 0, expandIcon);
      tlu.add(2, 0, showMoreLabel);
      tlu.add(4, 0, new _SeparatorPanel());
      expandIcon.addMouseListener(new ExpandOrHideListener());
      showMoreLabel.addMouseListener(new ExpandOrHideListener());
    }

    /**
     * Simple Panel that makes it so the Separator it contains is displayed in the middle of a line, instead of at the top or bottom.
     * In order to accomplish this, there has to be a panel with size 1/2 line height above and one of the same size below the separator, because the separator itself
     * has no size and would end up at the top or bottom otherwise
     */
    private class _SeparatorPanel extends JPanel
    {

      public _SeparatorPanel()
      {
        super(new BorderLayout());
        JSeparator separator = new JSeparator();
        // add a upper and lower panel to "sandwich" in the separator. Only setting the upper or lower does not work, since the separator has no height and would
        // end up at the bottom or top, and not the middle
        JPanel upperBorderPanel = new JPanel();
        upperBorderPanel.setBorder(new EmptyBorder((showMoreLabel.getFontMetrics(showMoreLabel.getFont()).getHeight() - 1) / 2, 0, 0, 0));
        JPanel lowerBorderPanel = new JPanel();
        lowerBorderPanel.setBorder(new EmptyBorder((showMoreLabel.getFontMetrics(showMoreLabel.getFont()).getHeight() - 1) / 2, 0, 0, 0));
        add(upperBorderPanel, BorderLayout.NORTH);
        add(separator, BorderLayout.CENTER);
        add(lowerBorderPanel, BorderLayout.SOUTH);
      }
    }

    /**
     * MouseListener that triggers the runnable and actions that hide or show the lower component, depending on the isExpanded state. Also inverts the isExpanded state at
     * the end
     */
    private class ExpandOrHideListener extends MouseAdapter
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (isExpaned)
        {
          expandIcon.setIcon(hiddenIcon);
          showMoreLabel.setText(Util.getResource(ExpandablePanel.class, "expandablePanelShowMoreText"));
          hideLowerComponent.run();
        }
        else
        {
          expandIcon.setIcon(expandedIcon);
          showMoreLabel.setText(Util.getResource(ExpandablePanel.class, "expandablePanelShowLessText"));
          showLowerComponent.run();
        }
        isExpaned = !isExpaned;
      }
    }
  }
}
