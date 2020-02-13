package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.annotation.Nullable;
import javax.swing.*;

/**
 * Dialog that only has a textField in which the user can input information, such as a filePath or a userName
 *
 * @author m.kaspera, 21.12.2018
 */
class UserPromptDialog extends AditoBaseDialog<Object>
{

  private static final int PATH_NUM_CHARS = 60;
  private final JTextField textField;

  @Inject
  UserPromptDialog(@Nullable @Assisted String pDefault)
  {
    textField = new JTextField(PATH_NUM_CHARS);
    if (pDefault != null)
      textField.setText(pDefault);
    _initGui();
  }

  private void _initGui()
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, fill, gap};
    double[] rows = {gap,
                     pref};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, textField);
  }

  @Override
  public String getMessage()
  {
    return textField.getText();
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
