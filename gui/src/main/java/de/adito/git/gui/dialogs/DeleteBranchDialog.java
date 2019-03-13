package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;

/**
 * @author m.kaspera, 13.03.2019
 */
public class DeleteBranchDialog extends AditoBaseDialog<Boolean>
{

  private final JCheckBox checkBox = new JCheckBox();

  @Inject
  public DeleteBranchDialog()
  {
    _initGui();
  }

  private void _initGui()
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, pref, gap, fill, gap};
    double[] rows = {gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, checkBox);
    tlu.add(3, 1, new JLabel("Delete remote branch as well (if existent)"));
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Boolean getInformation()
  {
    return checkBox.isSelected();
  }
}
