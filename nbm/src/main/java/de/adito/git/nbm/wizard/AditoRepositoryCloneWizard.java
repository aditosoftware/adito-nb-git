package de.adito.git.nbm.wizard;

import com.google.common.collect.Sets;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.Image;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author a.arnold, 07.01.2019
 */
public class AditoRepositoryCloneWizard implements WizardDescriptor.ProgressInstantiatingIterator
{
  private static final ImageIcon W_PROJECT_IMAGE = ImageUtilities.loadImageIcon("de/adito/git/nbm/wizard/adito16.png", false);
  static final String W_PROJECT_NAME = "nbg.name";
  static final String W_PROJECT_PATH = "nbg.project.path";
  static final String W_REPOSITORY_PATH = "nbg.repository.path";
  static final String W_SSH_PATH = "nbg.ssh.path";
  static final String W_SSH_KEY_PASS = "nbg.ssh.pass";
  static final String W_BRANCH_LIST = "nbg.branch.list";
  static final String W_BRANCH = "nbg.branch";

  private int index;
  private WizardDescriptor wizard;
  private WizardDescriptor.Panel[] panels;

  /**
   * Creates the WizardPanels
   *
   * @return An array of panels
   */
  private WizardDescriptor.Panel[] _getPanels()
  {
    if (panels == null)
    {
      panels = new WizardDescriptor.Panel[]{
          //the panels for the wizard
          new CloneWizardPanel1(),
          new CloneWizardPanel2()
      };
      String[] steps = _createSteps();
      for (int i = 0; i < panels.length; i++)
      {
        Component component = panels[i].getComponent();
        if (steps[i] == null)
        {
          // Default step name to component name of panel. Mainly
          // useful for getting the name of the target chooser to
          // appear in the list of steps.
          steps[i] = component.getName();
        }
        if (component instanceof JComponent)
        {
          //assume Swing Component
          JComponent jc = (JComponent) component;
          // Sets step number of a component
          jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
          // Sets steps names for a panel
          jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
          // Turn on subtitle creation on each step
          jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
          // Show steps on the left side with the image on the background
          jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
          // Turn on numbering of all steps
          jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
        }
      }
    }
    return panels;
  }

  /**
   * Initiate the action after the Wizard is finished
   *
   * @param pHandle The handler for the progress bar
   * @return
   */
  @Override
  public Set instantiate(ProgressHandle pHandle)
  {
    FileObject instantiate = AditoRepositoryCloneWizardExec.instantiate(pHandle, wizard);
    return instantiate == null ? Collections.emptySet() : Sets.newHashSet(instantiate);
  }

  @Override
  public Set instantiate()
  {
    return Collections.emptySet();
  }

  @Override
  public void initialize(WizardDescriptor pWizard)
  {
    wizard = pWizard;
  }

  @Override
  public void uninitialize(WizardDescriptor pWizard)
  {
    panels = null;
  }

  @Override
  public WizardDescriptor.Panel current()
  {
    return _getPanels()[index];
  }

  @Override
  public String name()
  {
    return index + 1 + ". from " + _getPanels().length;
  }

  @Override
  public boolean hasNext()
  {
    return index < _getPanels().length - 1;
  }

  @Override
  public boolean hasPrevious()
  {
    return index > 0;
  }

  @Override
  public void nextPanel()
  {
    if (!hasNext())
      throw new NoSuchElementException("Index: " + index + ", num panels: " + _getPanels().length);
    index++;
  }

  @Override
  public void previousPanel()
  {
    if (!hasPrevious())
      throw new NoSuchElementException("Index: " + index + ", num panels: " + _getPanels().length);
    index--;
  }

  @Override
  public void addChangeListener(ChangeListener pL)
  {
    //no listener
  }

  @Override
  public void removeChangeListener(ChangeListener pL)
  {
    //no listener
  }

  /**
   * Create an String[] to count the wizard panels
   *
   * @return The wizardp panales in a String[]
   */
  private String[] _createSteps()
  {
    String[] res = new String[panels.length];
    for (int i = 0; i < res.length; i++)
      res[i] = panels[i].getComponent().getName();
    return res;
  }

  /**
   * return the bundle message
   *
   * @param pObject the caller class
   * @param pMsg    the name of the property
   * @param pParams optional param
   * @return the text inside the bundle file as string
   */
  public static String getMessage(Object pObject, String pMsg, Object... pParams)
  {
    return NbBundle.getMessage(pObject.getClass(), pMsg, pParams);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  //used in layer.xml
  public static Image getProjectImage()
  {
    return W_PROJECT_IMAGE.getImage();
  }
}
