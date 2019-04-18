package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
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
    pRepository.blockingFirst().map(IRepository::getConfig).ifPresent(pConfig -> sshKeyField.setText(pConfig.getSshKeyLocation(null)));
    sshPassphraseField.setEnabled(false);
    sshPassphraseField.setToolTipText(CANT_CHANGE_PASSW_HINT);
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
    tlu.add(1, 1, new JLabel(SSH_KEY_FIELD_LABEL));
    tlu.add(3, 1, sshKeyField);
    tlu.add(5, 1, _browseSshKeyButton());
    tlu.add(1, 3, new JLabel(SSH_PASSPHRASE_FIELD_LABEL));
    tlu.add(3, 3, 5, 3, sshPassphraseField);
  }

  private JButton _browseSshKeyButton()
  {
    JButton locationBrowseButton = new JButton("Browse");
    locationBrowseButton.addActionListener(e -> {
      JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnValue = fc.showSaveDialog(null);
      if (returnValue == JFileChooser.APPROVE_OPTION)
      {
        sshKeyField.setText(fc.getSelectedFile().getAbsolutePath());
      }
    });
    return locationBrowseButton;
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
