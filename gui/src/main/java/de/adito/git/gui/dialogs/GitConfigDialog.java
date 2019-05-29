package de.adito.git.gui.dialogs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.impl.data.SSHKeyDetails;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.*;

/**
 * @author m.kaspera, 24.12.2018
 */
public class GitConfigDialog extends AditoBaseDialog<Multimap<String, Object>>
{

  private static final Border DEFAULT_MARGIN_BORDER = new EmptyBorder(15, 15, 0, 0);
  private static final String SSH_KEY_FIELD_LABEL = "SSH key path: ";
  private static final String SSH_PASSPHRASE_FIELD_LABEL = "Passphrase for ssh key: ";
  private List<RemotePanel> remoteSettingsPanels = new ArrayList<>();

  @Inject
  public GitConfigDialog(IKeyStore pKeyStore, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Optional<IRepository> optionalIRepository = pRepository.blockingFirst();
    if (optionalIRepository.isPresent())
    {
      @NotNull Set<String> remotes = optionalIRepository.get().getRemoteNames();
      if (remotes.size() == 1)
      {
        String remoteName = remotes.iterator().next();
        JPanel labelPanel = new JPanel(new BorderLayout());
        JLabel remoteNameLabel = _getBoldLabel("Remote: " + remoteName);
        // selber inset wie remotePanel
        remoteNameLabel.setBorder(DEFAULT_MARGIN_BORDER);
        labelPanel.add(remoteNameLabel);
        add(labelPanel);
        RemotePanel remoteSettingsPanel = new RemotePanel(optionalIRepository.get(), remoteName, pKeyStore);
        add(remoteSettingsPanel);
        remoteSettingsPanels.add(remoteSettingsPanel);
        add(new JSeparator(SwingConstants.HORIZONTAL));
      }
      else
      {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel labelPanel = new JPanel(new BorderLayout());
        JLabel remotesLabel = _getBoldLabel("Remotes:");
        remotesLabel.setAlignmentX(LEFT_ALIGNMENT);
        labelPanel.add(remotesLabel);
        add(labelPanel);
        remotesLabel.setBorder(DEFAULT_MARGIN_BORDER);
        tabbedPane.setBorder(new CompoundBorder(DEFAULT_MARGIN_BORDER, tabbedPane.getBorder()));
        add(tabbedPane);
        for (String remote : remotes)
        {
          RemotePanel remoteSettingsPanel = new RemotePanel(optionalIRepository.get(), remote, pKeyStore);
          tabbedPane.add(remote, remoteSettingsPanel);
          remoteSettingsPanels.add(remoteSettingsPanel);
        }
      }
    }
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Multimap<String, Object> getInformation()
  {
    Multimap<String, Object> settingsMap = HashMultimap.create();
    for (RemotePanel remoteSettingsPanel : remoteSettingsPanels)
    {
      settingsMap.putAll(remoteSettingsPanel.getInformation());
    }
    return settingsMap;
  }

  /**
   * creates a normal JLabel, except that the text is bold
   *
   * @param pText Text the label should have
   * @return JLabel with pText in bold
   */
  private static JLabel _getBoldLabel(String pText)
  {
    JLabel label = new JLabel(pText);
    label.setFont(new Font(label.getFont().getFontName(), Font.BOLD, label.getFont().getSize()));
    return label;
  }

  private static class RemotePanel extends JPanel
  {

    private final JTextField sshKeyField = new JTextField();
    private final JPasswordField sshPassphraseField = new JPasswordField(30);
    private final String remoteUrl;

    RemotePanel(IRepository pRepository, String pRemoteName, IKeyStore pKeyStore)
    {
      remoteUrl = pRepository.getConfig().getRemoteUrl(pRemoteName);
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
          for (int index = 0; index < passphrase.length; index++)
          {
            passphrase[index] = '0';
          }
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

    public Multimap<String, Object> getInformation()
    {
      Multimap<String, Object> settingsMap = HashMultimap.create();
      settingsMap.put(Constants.SSH_KEY_KEY, new SSHKeyDetails(sshKeyField.getText(), remoteUrl, sshPassphraseField.getPassword()));
      return settingsMap;
    }

  }
}
