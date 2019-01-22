package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;

/**
 * @author a.arnold, 16.01.2019
 */
public class YesNoDialog extends AditoBaseDialog<Object>
{
  private final JLabel label = new JLabel();

  @Inject
  YesNoDialog(@Assisted String pMessage)
  {
    label.setText(pMessage);
    _initGui();
  }

  private void _initGui()
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, fill, gap};
    double[] rows = {gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, label);
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
