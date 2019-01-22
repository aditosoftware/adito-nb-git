package de.adito.git.nbm.wizard;

import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.nbm.prefs.NBPrefStore;
import info.clearthought.layout.TableLayout;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import javax.swing.*;
import java.io.File;

import static de.adito.git.nbm.IGitConstants.*;

/**
 * @author a.arnold, 08.01.2019
 */
public class CloneWizardVisualPanel1 extends JPanel
{
  private JTextField locationTextField;
  private JTextField repositoryUrlTextField;
  private JTextField projectNameTextField;
  private JTextField sshKeyDataTextField;
  private JButton locationBrowseButton;
  private JButton sshDataBrowserButton;
  private JPasswordField sshKeyPassField;
  private NBPrefStore preferences;

  CloneWizardVisualPanel1()
  {
    preferences = new NBPrefStore();
    _initComponents();
  }

  private void _initComponents()
  {
    // set default location of the projects
    if (preferences.get(GIT_PROJECT_LOCATION) == null)
      preferences.put(GIT_PROJECT_LOCATION, ProjectChooser.getProjectsFolder().getAbsolutePath());
    locationBrowseButton = new JButton(AditoRepositoryCloneWizard.getMessage(this, "Label.Browse"));
    JLabel locationLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.Location"));
    locationTextField = new JTextField(preferences.get(GIT_PROJECT_LOCATION));

    JLabel repositoryUrlLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.RepositoryURL"));
    repositoryUrlTextField = new JTextField();

    JLabel projectNameLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.ProjectName"));
    projectNameTextField = new JTextField();

    // set default of the ssh key location
    if (preferences.get(GIT_SSH_KEY) == null)
      preferences.put(GIT_SSH_KEY, System.getProperty(new File(new File(System.getProperty("user.home"), ".ssh"), "id_rsa").getAbsolutePath()));
    JLabel sshKeyDataLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.SSHKeyData"));
    sshKeyDataTextField = new JTextField(preferences.get(GIT_SSH_KEY));
    sshDataBrowserButton = new JButton(AditoRepositoryCloneWizard.getMessage(this, "Label.Browse"));

    JLabel sshKeyPassLabel = new JLabel(AditoRepositoryCloneWizard.getMessage(this, "Label.SSHKeyPass"));
    sshKeyPassField = new JPasswordField();

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
                     gap,
                     pref,
                     gap,
                     pref,
                     gap};

    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, repositoryUrlLabel);
    tlu.add(3, 1, repositoryUrlTextField);
    tlu.add(1, 3, locationLabel);
    tlu.add(3, 3, locationTextField);
    tlu.add(5, 3, locationBrowseButton);
    tlu.add(1, 5, projectNameLabel);
    tlu.add(3, 5, projectNameTextField);
    tlu.add(1, 7, sshKeyDataLabel);
    tlu.add(3, 7, sshKeyDataTextField);
    tlu.add(5, 7, sshDataBrowserButton);
    tlu.add(1, 9, sshKeyPassLabel);
    tlu.add(3, 9, sshKeyPassField);
  }

  @Override
  public String getName()
  {
    return AditoRepositoryCloneWizard.getMessage(this, "Title.Name.Panel1");
  }

  JPasswordField getSshKeyPassField()
  {
    return sshKeyPassField;
  }

  JTextField getProjectNameTextField()
  {
    return projectNameTextField;
  }

  JTextField getLocationTextField()
  {
    return locationTextField;
  }

  JButton getLocationBrowseButton()
  {
    return locationBrowseButton;
  }

  JTextField getSshKeyDataTextField()
  {
    return sshKeyDataTextField;
  }

  JButton getSshDataBrowserButton()
  {
    return sshDataBrowserButton;
  }

  JTextField getRepositoryUrlTextField()
  {
    return repositoryUrlTextField;
  }

}
