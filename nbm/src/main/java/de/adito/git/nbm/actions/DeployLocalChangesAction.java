package de.adito.git.nbm.actions;

import com.google.common.annotations.VisibleForTesting;
import de.adito.git.api.IRepository;
import de.adito.git.nbm.util.RepositoryUtility;
import de.adito.notification.INotificationFacade;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.netbeans.api.project.Project;
import org.openide.awt.*;
import org.openide.modules.Modules;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.*;

/**
 * Deploys all local changes that are not committed.
 *
 * @author F.Adler, 27.06.2023
 */
@ActionID(category = "adito/ribbon", id = "de.adito.git.nbm.actions.DeployLocalChangesAction")
@ActionRegistration(displayName = "#ACTION_deployProject",
    iconBase = "de/adito/git/nbm/actions/indent_dark.png")
@ActionReferences({
    @ActionReference(path = "Toolbars/deploy", position = 500, separatorAfter = 550),
    @ActionReference(path = "Shortcuts", name = "SA-F8")})
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
   * Perform a ADITO Designer deploy of all sources that are not committed
   *
   * @param pActionEvent the event to be processed
   */
  @Override
  public void actionPerformed(@NonNull ActionEvent pActionEvent)
  {
    Observable<List<String>> uncommittedFilesObs = RepositoryUtility.getRepositoryObservable()
        .switchMap(pRepo -> pRepo.map(IRepository::getStatus).orElseGet(() -> Observable.just(Optional.empty())))
        .map(pIFileStatus -> pIFileStatus.isPresent() ? pIFileStatus.get().getUncommittedChanges() : Set.<String>of())
        .map(this::getSourcesToDeploy);

    List<String> uncommittedFiles = uncommittedFilesObs.blockingFirst();
    if (!uncommittedFiles.isEmpty())
    {
      deploy(uncommittedFiles);
    }
    else
    {
      INotificationFacade.INSTANCE.notify("Deploy", "There is nothing to deploy", true, null);
    }
  }


  /**
   * Creates a list and returns all sources that are not committed and should be deployed
   *
   * @param pUncommittedFiles The files which status are uncommitted
   * @return The list of sources which should be deployed
   */
  @VisibleForTesting
  List<String> getSourcesToDeploy(@NonNull Set<String> pUncommittedFiles)
  {
    Set<String> uncommittedList = new HashSet<>();
    String regex = ".+?/([^/]+)/.*";
    Pattern pattern = Pattern.compile(regex);
    for (String e : pUncommittedFiles)
    {
      Matcher matcher = pattern.matcher(e);
      if (matcher.find())
      {
        uncommittedList.add(matcher.group(1));
      }
    }
    return new ArrayList<>(uncommittedList);
  }

  /**
   * Reflectionmethod to call the ADITO Designers deploy
   *
   * @param pUncommittedList The list of sources that should be deployed
   */
  @VisibleForTesting
  void deploy(@NonNull List<String> pUncommittedList)
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
      INotificationFacade.INSTANCE.error(pE);
    }
  }
}

