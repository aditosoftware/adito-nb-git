package de.adito.git.gui.dialogs;

import com.google.inject.Inject;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog that prompts the user for a password, uses a JPasswordField
 *
 * @author m.kaspera, 20.12.2018
 */
class PasswordPromptDialog extends AditoBaseDialog<Object>
{

  private final static Insets PW_FIELD_INSETS = new Insets(0, 10, 0, 10);
  private final static int PW_FIELD_NUM_CHARS = 30;
  private final JPasswordField passwordField;

  @Inject
  PasswordPromptDialog()
  {
    passwordField = new JPasswordField(PW_FIELD_NUM_CHARS);
    _initGui();
  }

  private void _initGui()
  {
    passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height));
    setLayout(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.insets = PW_FIELD_INSETS;
    add(passwordField, gbConstraints);
  }

  @Override
  public String getMessage()
  {
    return String.valueOf(passwordField.getPassword());
  }

  @Override
  public Object getInformation()
  {
    return null;
  }
}
