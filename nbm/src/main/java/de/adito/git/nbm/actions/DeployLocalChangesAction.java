package de.adito.git.nbm.actions;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.nbm.repo.RepositoryCache;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.netbeans.api.project.Project;
import org.openide.awt.*;
import org.openide.modules.Modules;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Deployt alle lokalen Änderungen.
 *
 * @author F.Adler, 27.06.2023
 */
@ActionID(category = "adito/aods", id = "de.adito.git.nbm.actions.DeployLocalChangesAction")
@ActionRegistration(displayName = "#ACTION_deployProject")
@ActionReference(path = "Shortcuts", name = "DA-F")
public class DeployLocalChangesAction extends SystemAction
{

  @Override
  public String getName()
  {
    return NbBundle.getMessage(getClass(), "ACTION_deployProject");
  }

  @Override
  public HelpCtx getHelpCtx()
  {
    return null;
  }

  /**
   * Perform a ADITO Designer deploy of all sources that are not commited
   *
   * @param pActionEvent the event to be processed
   */
  @Override
  public void actionPerformed(@NonNull ActionEvent pActionEvent)
  {
    Optional<Project> project = ProjectUtility.findProjectFromActives(TopComponent.getRegistry());
    Observable<Optional<IRepository>> repository = project.map(pProj -> RepositoryCache.getInstance().findRepository(pProj))
        .orElse(Observable.just(Optional.empty()));

    if (repository.blockingFirst().isPresent())
    {
      Observable<Optional<IFileStatus>> status = repository.blockingFirst().get().getStatus();

      if (status.blockingFirst().isPresent())
      {
        List<String> uncommittedFiles = getSourcesToDeploy(status);

        deploy(uncommittedFiles);
      }
      else
      {
        Logger.getLogger(DeployLocalChangesAction.class.getName()).log(Level.INFO, "There is nothing to deploy");
      }
    }
    else
    {
      Logger.getLogger(DeployLocalChangesAction.class.getName()).log(Level.INFO, "No repository was found");
    }
  }

  /**
   * Creates a list and returns all sources that are not commited and should be deployed
   *
   * @param pStatus The files which status are uncommited
   * @return The list of sources which should be deployed
   */
  private List<String> getSourcesToDeploy(@NonNull Observable<Optional<IFileStatus>> pStatus)
  {
    List<String> uncommittedList = new ArrayList<>();
    if (pStatus.blockingFirst().isPresent() && (!pStatus.blockingFirst().get().getUncommittedChanges().isEmpty()))
    {
      String regex = ".+?/([^/]+)/.*";
      Pattern pattern = Pattern.compile(regex);
      for (String e : pStatus.blockingFirst().get().getUncommittedChanges())
      {
        Matcher matcher = pattern.matcher(e);
        if (matcher.find())
        {
          uncommittedList.add(matcher.group(1));
        }
      }
      return uncommittedList;
    }
    return uncommittedList;
  }

  /**
   * Reflectionsmethod to call the ADITO Designers deploy
   *
   * @param pUncommittedList The list of sources that should be deployed
   */
  private void deploy(@NonNull List<String> pUncommittedList)
  {
    try
    {
      ClassLoader deployClassLoader = Modules.getDefault().findCodeNameBase("de.adito.designer.netbeans.Deploy").getClassLoader();
      Class<?> deployStarterClass = Class.forName("de.adito.aditoweb.nbm.deploy.impl.DeployStarter", true, deployClassLoader);
      Class<?> gui = Class.forName("de.adito.aditoweb.nbm.deploy.impl.gui.GuiInteractiveDeployCallback", true, deployClassLoader);
      Class<?> iInteractiveDeployCallback = Class.forName("de.adito.aditoweb.nbm.deploy.impl.IInteractiveDeployCallback", true, deployClassLoader);

      Constructor<?> guiConstructor = gui.getConstructor(Project.class, List.class);
      Object guiInstance = guiConstructor.newInstance(null, pUncommittedList);
      deployStarterClass.getMethod("deploy", iInteractiveDeployCallback).invoke(null, guiInstance);
    }
    catch (ReflectiveOperationException pE)
    {
      Logger.getLogger(DeployLocalChangesAction.class.getName()).log(Level.WARNING, pE.getLocalizedMessage());
    }
  }
}

