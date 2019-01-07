package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author m.kaspera, 24.12.2018
 */
public class GitConfigDialog extends AditoBaseDialog<Map<String, String>>
{

  private static final String SSH_KEY_FIELD_LABEL = "SSH key path: ";
  private static final String SSH_PASSPHRASE_FIELD_LABEL = "Passphrase for ssh key: ";
  private static final String CANT_CHANGE_PASSW_HINT = "Password is only changed if it gets queried because required and no password is" +
      " saved/saved password is wrong";
  private final JTextField sshKeyField = new JTextField();
  private final JPasswordField sshPassphraseField = new JPasswordField(30);

  @Inject
  public GitConfigDialog(@Assisted Observable<Optional<IRepository>> pRepository)
  {
    _initGui();
    pRepository.blockingFirst().map(IRepository::getConfig).ifPresent(pConfig -> sshKeyField.setText(pConfig.getSshKeyLocation()));
    sshPassphraseField.setEnabled(false);
    sshPassphraseField.setToolTipText(CANT_CHANGE_PASSW_HINT);
  }

  private void _initGui()
  {
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    add(new JLabel(SSH_KEY_FIELD_LABEL), gbc);
    gbc.gridx = 1;
    add(sshKeyField, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    add(new JLabel(SSH_PASSPHRASE_FIELD_LABEL), gbc);
    gbc.gridx = 1;
    add(sshPassphraseField, gbc);
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Map<String, String> getInformation()
  {
    Map<String, String> settingsMap = new HashMap<>();
    settingsMap.put(Constants.SSH_KEY_KEY, sshKeyField.getText());
    return settingsMap;
  }
}
