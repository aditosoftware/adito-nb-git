package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.api.data.EResetType;

import javax.swing.*;
import java.awt.*;

/**
 * @author m.kaspera 31.10.2018
 */
class ResetDialog extends AditoBaseDialog<EResetType>
{

  private JRadioButton softButton;
  private JRadioButton mixedButton;
  private JRadioButton hardButton;

  @Inject
  ResetDialog()
  {
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    ButtonGroup radioButtonGroup = new ButtonGroup();
    softButton = new JRadioButton("Soft: only reset HEAD");
    mixedButton = new JRadioButton("Mixed: reset HEAD and index");
    hardButton = new JRadioButton("Hard: reset HEAD, index and working directory. BEWARE: all changes will be lost");
    radioButtonGroup.add(softButton);
    radioButtonGroup.add(mixedButton);
    radioButtonGroup.add(hardButton);
    add(softButton, BorderLayout.NORTH);
    add(mixedButton, BorderLayout.CENTER);
    add(hardButton, BorderLayout.SOUTH);
    mixedButton.setSelected(true);
  }

  EResetType getResetType()
  {
    EResetType returnValue = null;
    if (softButton.isSelected())
      returnValue = EResetType.SOFT;
    else if (mixedButton.isSelected())
      returnValue = EResetType.MIXED;
    else if (hardButton.isSelected())
      returnValue = EResetType.HARD;
    return returnValue;
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public EResetType getInformation()
  {
    return getResetType();
  }
}
