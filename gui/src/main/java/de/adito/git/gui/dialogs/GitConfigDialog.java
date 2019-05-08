package de.adito.git.gui.dialogs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.Constants;
import de.adito.git.gui.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.util.*;

/**
 * @author m.kaspera, 24.12.2018
 */
public class GitConfigDialog extends AditoBaseDialog<Multimap<String, Object>>
{

  private static final String SSH_KEY_FIELD_LABEL = "SSH key path: ";
  private static final String SSH_PASSPHRASE_FIELD_LABEL = "Passphrase for ssh key: ";
  private static final String CANT_CHANGE_PASSW_HINT = "Password is only changed if it gets queried because required and no password is" +
      " saved/saved password is wrong";
  private List<RemotePanel> remoteSettingsPanels = new ArrayList<>();

  @Inject
  public GitConfigDialog(@Assisted Observable<Optional<IRepository>> pRepository)
  {
    setLayout(new BorderLayout(0, 10));

    Optional<IRepository> optionalIRepository = pRepository.blockingFirst();
    if (optionalIRepository.isPresent())
    {
      @NotNull Set<String> remotes = optionalIRepository.get().getRemoteNames();
      if (remotes.size() == 1)
      {
        String remoteName = remotes.iterator().next();
        JLabel remoteNameLabel = new JLabel("Remote: " + remoteName);
        // selber inset wie remotePanel
        remoteNameLabel.setBorder(new EmptyBorder(15, 15, 0, 0));
        add(remoteNameLabel, BorderLayout.NORTH);
        RemotePanel remoteSettingsPanel = new RemotePanel(optionalIRepository.get(), remoteName);
        add(remoteSettingsPanel, BorderLayout.CENTER);
        remoteSettingsPanels.add(remoteSettingsPanel);
      }
      else
      {
        JTabbedPane tabbedPane = new JTabbedPane();
        JLabel remotesLabel = new JLabel("Remotes:");
        remotesLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        add(remotesLabel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        for (String remote : remotes)
        {
          RemotePanel remoteSettingsPanel = new RemotePanel(optionalIRepository.get(), remote);
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

  private static class RemotePanel extends JPanel
  {

    private final JTextField sshKeyField = new JTextField();
    private final JPasswordField sshPassphraseField = new JPasswordField(30);
    private final String remoteUrl;

    RemotePanel(IRepository pRepository, String pRemoteName)
    {
      remoteUrl = pRepository.getConfig().getRemoteUrl(pRemoteName);
      _initGui();
      sshKeyField.setText(pRepository.getConfig().getSshKeyLocation(pRepository.getConfig().getRemoteUrl(pRemoteName)));
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

    public Multimap<String, Object> getInformation()
    {
      Multimap<String, Object> settingsMap = HashMultimap.create();
      String[] value = {sshKeyField.getText(), remoteUrl};
      settingsMap.put(Constants.SSH_KEY_KEY, value);
      return settingsMap;
    }

  }
}
