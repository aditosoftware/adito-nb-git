package de.adito.git.gui.dialogs.panels;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IRemote;
import de.adito.git.gui.Constants;
import de.adito.git.impl.data.RemoteImpl;
import de.adito.git.impl.data.SSHKeyDetails;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.util.Arrays;

/**
 * Panel that holds the settings for a remote
 *
 * @author m.kaspera, 30.07.2019
 */
public class RemotePanel extends JPanel
{

  private static final String REMOTE_URL = "URL: ";
  private static final String SSH_KEY_FIELD_LABEL = "SSH key path: ";
  private static final String SSH_PASSPHRASE_FIELD_LABEL = "Passphrase for ssh key: ";

  private final JTextField sshKeyField = new JTextField();
  private final JTextField urlTextField = new JTextField();
  private final JPasswordField sshPassphraseField = new JPasswordField(30);
  private final String remoteUrl;

  public RemotePanel(IRepository pRepository, IRemote pRemote, IKeyStore pKeyStore)
  {
    setName(pRemote.getName());
    remoteUrl = pRemote.getUrl();
    urlTextField.setText(remoteUrl);
    _initGui();
    String sshKeyLocation = pRepository.getConfig().getSshKeyLocation(remoteUrl);
    if (sshKeyLocation != null)
    {
      sshKeyField.setText(sshKeyLocation);
      char[] passphrase = pKeyStore.read(sshKeyLocation);
      if (passphrase != null)
      {
        sshPassphraseField.setText(String.valueOf(passphrase));
        // null passphrase array
        Arrays.fill(passphrase, '0');
      }
    }
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
                     gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, new JLabel(REMOTE_URL));
    tlu.add(3, 1, urlTextField);
    tlu.add(1, 3, new JLabel(SSH_KEY_FIELD_LABEL));
    tlu.add(3, 3, sshKeyField);
    tlu.add(5, 3, _browseSshKeyButton());
    tlu.add(1, 5, new JLabel(SSH_PASSPHRASE_FIELD_LABEL));
    tlu.add(3, 5, 5, 5, sshPassphraseField);
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

  public Multimap<String, Object> getInformation()
  {
    Multimap<String, Object> settingsMap = HashMultimap.create();
    settingsMap.put(Constants.REMOTE_INFO_KEY, new RemoteImpl(getName(), urlTextField.getText(), IRemote.getFetchStringFromName(getName())));
    settingsMap.put(Constants.SSH_KEY_KEY, new SSHKeyDetails(sshKeyField.getText(), getName(), sshPassphraseField.getPassword()));
    return settingsMap;
  }

}
