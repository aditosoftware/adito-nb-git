package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.gui.dialogs.results.StashChangesResult;
import info.clearthought.layout.TableLayout;

import javax.swing.*;

/**
 * @author m.kaspera, 12.02.2019
 */
class StashChangesDialog extends AditoBaseDialog<StashChangesResult>
{

  private final JTextField stashMessageField;
  private final JCheckBox includeUnTrackedCheckbox;

  @Inject
  StashChangesDialog()
  {
    stashMessageField = new JTextField();
    includeUnTrackedCheckbox = new JCheckBox();
    includeUnTrackedCheckbox.setText("include un-tracked files");
    includeUnTrackedCheckbox.setSelected(false);
    _initGui();
  }

  private void _initGui()
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, pref, gap, fill, gap, pref, gap};
    double[] rows = {gap,
                     pref,
                     gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, new JLabel("Message:"));
    tlu.add(3, 1, stashMessageField);
    tlu.add(3, 3, includeUnTrackedCheckbox);
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public StashChangesResult getInformation()
  {
    return new StashChangesResult(stashMessageField.getText(), includeUnTrackedCheckbox.isSelected());
  }
}
