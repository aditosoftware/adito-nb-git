package de.adito.git.nbm.wizard;

import de.adito.git.api.ICloneRepo;
import de.adito.git.api.data.IBranch;
import de.adito.git.nbm.IGitConstants;
import org.openide.WizardDescriptor;
import org.openide.util.*;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;

/**
 * @author a.arnold, 08.01.2019
 */
public class CloneWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor>
{
  private final ICloneRepo cloneRepo = IGitConstants.INJECTOR.getInstance(ICloneRepo.class);
  private CloneWizardVisualPanel2 panel2;
  private WizardDescriptor wizard;
  private ChangeSupport cs = new ChangeSupport(this);

  @Override
  public Component getComponent()
  {
    if (panel2 == null)
    {
      _createComponent();
    }
    return panel2;
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
    if (wizard == null || panel2 == null)
      return;
    _createBranchArray();
    Object branchList = wizard.getProperty(AditoRepositoryCloneWizard.W_BRANCH_LIST);
    if (branchList != null)
    {
      panel2.setBranchArray((IBranch[]) branchList);
    }
  }

  @Override
  public boolean isValid()
  {
    return true;
  }

  /**
   * Store the settings of the Wizard
   *
   * @param pSettings The wizard to load
   */
  @Override
  public void storeSettings(WizardDescriptor pSettings)
  {
    if (panel2 == null || pSettings == null)
    {
      return;
    }
    wizard.putProperty(AditoRepositoryCloneWizard.W_BRANCH, _getSelectedBranch());
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

  /**
   * creates all components in this panel
   */
  private void _createComponent()
  {
    panel2 = new CloneWizardVisualPanel2();
  }

  /**
   * creates an array of branches. This is saved in the {@link AditoRepositoryCloneWizard}
   */
  private void _createBranchArray()
  {
    if (wizard == null)
    {
      return;
    }
    String sshKeyLocation = (String) wizard.getProperty(AditoRepositoryCloneWizard.W_SSH_PATH);
    char[] sshKey = (char[]) wizard.getProperty(AditoRepositoryCloneWizard.W_SSH_KEY_PASS);
    String repositoryUrl = (String) wizard.getProperty(AditoRepositoryCloneWizard.W_REPOSITORY_PATH);
    if (repositoryUrl != null)
    {
      List<IBranch> branchesFromRemoteRepo = cloneRepo.getBranchesFromRemoteRepo(repositoryUrl, sshKeyLocation, sshKey);
      IBranch[] branchArray = branchesFromRemoteRepo
          .stream()
          .toArray(IBranch[]::new);
      wizard.putProperty(AditoRepositoryCloneWizard.W_BRANCH_LIST, branchArray);
    }
    //if the repositoryUrl is null we cant check the repo
    else wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Invalid.RepositoryUrl");
  }

  private IBranch _getSelectedBranch()
  {
    if (panel2 != null && panel2.getSelectedBranch() != null)
    {
      return panel2.getSelectedBranch();
    }
    return null;
  }

}
