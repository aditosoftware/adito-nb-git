package de.adito.git.gui.dialogs.panels;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Dialog with a text message and a checkbox with some specified description
 *
 * @author m.kaspera, 01.07.2019
 */
public class CheckboxPanel extends AditoBaseDialog<Boolean>
{

  private final JCheckBox doAutomaticallyCB;
  private final String message;

  @Inject
  CheckboxPanel(@Assisted("message") String pMessage, @Assisted("checkbox") String pCheckboxText)
  {
    message = pMessage;
    doAutomaticallyCB = new JCheckBox(pCheckboxText);
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
                     gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, 3, 1, new JLabel(message));
    tlu.add(1, 3, 3, 3, doAutomaticallyCB);
  }

  @Override
  public @Nullable String getMessage()
  {
    return null;
  }

  @Nullable
  @Override
  public Boolean getInformation()
  {
    return doAutomaticallyCB.isSelected();
  }
}
