package de.adito.git.nbm.wizard;

import de.adito.git.api.ICloneRepo;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.prefs.NBPrefStore;
import de.adito.git.nbm.sidebar.DocumentUpdateChangeListener;
import org.jetbrains.annotations.NotNull;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileSystemView;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static de.adito.git.nbm.IGitConstants.GIT_PROJECT_LOCATION;
import static de.adito.git.nbm.IGitConstants.GIT_SSH_KEY;

/**
 * @author a.arnold, 08.01.2019
 */
public class CloneWizardPanel1 implements org.openide.WizardDescriptor.Panel<WizardDescriptor>
{

  private final ICloneRepo cloneRepo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);
  private CloneWizardVisualPanel1 panel1;
  private WizardDescriptor wizard;
  private final ChangeSupport cs = new ChangeSupport(this);
  private NBPrefStore preferences;
  private Pattern compiledUrlPattern;

  @Override
  public Component getComponent()
  {
    if (panel1 == null)
    {
      _createComponent();
    }
    return panel1;
  }

  /**
   * creates all components in this panel
   */
  private void _createComponent()
  {
    compiledUrlPattern = Pattern.compile("((ssh://)?git@.*:?.*/+.*[.]git)|(https://.*(/.*)+[.]git)");
    preferences = new NBPrefStore();
    panel1 = new CloneWizardVisualPanel1();
    panel1.getProjectNameTextField().getDocument().addDocumentListener(new DocumentUpdateChangeListener()
    {
      @Override
      public void update(DocumentEvent pEvent)
      {
        cs.fireChange();
      }
    });
    panel1.getProjectNameTextField().getDocument().addDocumentListener(new _DocumentListener());
    panel1.getLocationTextField().getDocument().addDocumentListener(new _DocumentListener());
    panel1.getRepositoryUrlTextField().getDocument().addDocumentListener(new _DocumentListener());
    panel1.getSshKeyDataTextField().getDocument().addDocumentListener(new _DocumentListener());
    //the browser button for LocationTextField
    panel1.getLocationBrowseButton().addActionListener(e -> {
      String s = _fileStringChooser(JFileChooser.DIRECTORIES_ONLY, panel1.getLocationTextField().getText());
      panel1.getLocationTextField().setText(s);
    });
    //the browser button for SSHKeyTextField
    panel1.getSshDataBrowserButton().addActionListener(e -> {
      String s = _fileStringChooser(JFileChooser.FILES_ONLY, panel1.getSshKeyDataTextField().getText());
      panel1.getSshKeyDataTextField().setText(s);
    });
  }

  @Override
  public HelpCtx getHelp()
  {
    return HelpCtx.DEFAULT_HELP;
  }

  /**
   * reload the settings to the wizard
   *
   * @param pSettings the wizard to load
   */
  @Override
  public void readSettings(WizardDescriptor pSettings)
  {
    wizard = pSettings;
    _updateComponent();
  }

  /**
   * This helper method put all variables from the wizard to local variables
   */
  private void _updateComponent()
  {
    if (wizard == null || panel1 == null)
      return;
    //loading the project name
    Object name = wizard.getProperty(AditoRepositoryCloneWizard.W_PROJECT_NAME);
    if (name != null)
    {
      panel1.getProjectNameTextField().setText(name.toString());
    }
    //loading the project path
    Object projectPath = wizard.getProperty(AditoRepositoryCloneWizard.W_PROJECT_PATH);
    if (projectPath != null)
    {
      panel1.getLocationTextField().setText(projectPath.toString());
    }
    //loading the repository path
    Object repositoryPath = wizard.getProperty(AditoRepositoryCloneWizard.W_REPOSITORY_PATH);
    if (repositoryPath != null)
    {
      panel1.getRepositoryUrlTextField().setText(repositoryPath.toString());
    }
    //loading the sshKey path
    Object sshPath = wizard.getProperty(AditoRepositoryCloneWizard.W_SSH_PATH);
    if (sshPath != null)
    {
      panel1.getSshKeyDataTextField().setText(sshPath.toString());
    }
    //loading the sshPassword
    Object sshPassword = wizard.getProperty(AditoRepositoryCloneWizard.W_SSH_KEY_PASS);
    if (sshPassword != null)
    {
      panel1.getSshKeyPassField().setText(String.valueOf((char[]) sshPassword));
    }
  }

  /**
   * this method checks the fields. (only the required fields)
   *
   * @return if the boolean return a true, the next button is clickable
   */
  @Override
  public boolean isValid()
  {
    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
    boolean valid = _isNotEmpty();
    _checkValidPath(_getProjectPath());
    _checkValidFileName(_getProjectName());
    _checkValidRepository(_getRepositoryPath());
    return valid && (wizard == null || wizard.getProperty(WizardDescriptor.PROP_ERROR_MESSAGE) == null);
  }

  /**
   * Helper method to check the required fields
   *
   * @return True if the fields are not empty
   */
  private boolean _isNotEmpty()
  {
    boolean valid = false;
    if (panel1 != null)
    {
      valid = !_getProjectName().isEmpty() && !_getProjectPath().isEmpty() && !_getRepositoryPath().isEmpty();
    }
    return valid;
  }

  /**
   * a helper class to create a JFileChooser to browse a file
   *
   * @param pSelectionMode the Mode of File, DIRECTORIES_ONLY, FILES_ONLY or FILES_AND_DIRECTORIES
   * @param pReturnText
   * @return return the path of a file of directory as {@link String}
   */
  private String _fileStringChooser(int pSelectionMode, String pReturnText)
  {

    JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
    fc.setDialogTitle(AditoRepositoryCloneWizard.getMessage(this, "Title.Dialog"));
    fc.setFileSelectionMode(pSelectionMode);
    int returnValue = fc.showSaveDialog(null);
    if (returnValue == JFileChooser.APPROVE_OPTION)
    {
      File selectedFile = fc.getSelectedFile();
      if (selectedFile.isDirectory() || selectedFile.isFile())
        return selectedFile.getAbsolutePath();
    }
    return pReturnText;
  }

  /**
   * Check if the path and the project name as directory exists.
   *
   * @param pProjectPath The path of the project
   */
  private void _checkValidPath(@NotNull String pProjectPath)
  {
    if (!pProjectPath.isEmpty())
    {
      FileObject fileObject = FileUtil.toFileObject(new File(pProjectPath));
      if (fileObject == null)
      {
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, (AditoRepositoryCloneWizard.getMessage(this, "Invalid.Path", pProjectPath)));
      }
      if (!_getProjectName().isEmpty() && new File(_getProjectPath(), _getProjectName()).exists())
      {
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, AditoRepositoryCloneWizard.getMessage(this, "Invalid.Directory", _getProjectName()));
      }
    }
  }

  /**
   * @param pFileName the file to check
   */
  private void _checkValidFileName(@NotNull String pFileName)
  {
    File f = new File(pFileName);
    try
    {
      f.getCanonicalPath();
    }
    catch (IOException pE)
    {
      wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, AditoRepositoryCloneWizard.getMessage(this, "Invalid.FileName", pFileName));
    }
  }

  /**
   * checks whether or not the url for the repository may be a valid ssh or https url
   *
   * @param pUrl url input for the repository
   */
  private void _checkValidRepository(@NotNull String pUrl)
  {
    if (!pUrl.isEmpty() && !compiledUrlPattern.matcher(pUrl).matches())
    {
      wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, AditoRepositoryCloneWizard.getMessage(this, "Invalid.Url", pUrl));
    }
  }

  /**
   * Store the settings for the Wizard
   *
   * @param pSettings The wizard to load
   */
  @Override
  public void storeSettings(WizardDescriptor pSettings)
  {
    if (panel1 == null || pSettings == null)
    {
      return;
    }
    wizard.putProperty(AditoRepositoryCloneWizard.W_PROJECT_NAME, _getProjectName());
    wizard.putProperty(AditoRepositoryCloneWizard.W_PROJECT_PATH, _getProjectPath());
    preferences.put(GIT_PROJECT_LOCATION, _getProjectPath());
    wizard.putProperty(AditoRepositoryCloneWizard.W_REPOSITORY_PATH, _getRepositoryPath());
    String sshPath = _getSshPath();
    char[] sshPassword = _getSSHPassword();
    wizard.putProperty(AditoRepositoryCloneWizard.W_SSH_PATH, sshPath);
    preferences.put(GIT_SSH_KEY, sshPath);
    if (sshPath != null && !sshPath.isEmpty() && sshPassword != null)
    {
      cloneRepo.getConfig().setPassphrase(sshPassword, sshPath);
    }
  }

  private String _getProjectPath()
  {
    return panel1 == null ? "" : panel1.getLocationTextField().getText();
  }

  private String _getProjectName()
  {
    return panel1 == null ? "" : panel1.getProjectNameTextField().getText();
  }

  private String _getSshPath()
  {
    return panel1 == null ? "" : panel1.getSshKeyDataTextField().getText();
  }

  private String _getRepositoryPath()
  {
    return panel1 == null ? "" : panel1.getRepositoryUrlTextField().getText();
  }

  private char[] _getSSHPassword()
  {
    return panel1.getSshKeyPassField().getPassword();
  }

  @Override
  public void addChangeListener(ChangeListener pL)
  {
    cs.addChangeListener(pL);
  }

  @Override
  public void removeChangeListener(ChangeListener pL)
  {
    cs.removeChangeListener(pL);
  }

  private class _DocumentListener extends DocumentUpdateChangeListener
  {
    @Override
    public void update(DocumentEvent pE)
    {
      cs.fireChange();
    }
  }
}
