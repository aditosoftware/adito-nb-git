package de.adito.git.gui.dialogs;

import com.google.inject.Inject;

import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Dialog that only has a textField in which the user can input information, such as a filePath or a userName
 *
 * @author m.kaspera, 21.12.2018
 */
class UserPromptDialog extends AditoBaseDialog<Object>
{

  private final static Insets TEXT_FIELD_INSETS = new Insets(0, 10, 0, 10);
  private final static int PATH_NUM_CHARS = 60;
  private final JTextField textField;

  @Inject
  UserPromptDialog()
  {
    textField = new JTextField(PATH_NUM_CHARS);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.insets = TEXT_FIELD_INSETS;
    add(textField, gbConstraints);
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
