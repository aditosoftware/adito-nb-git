package de.adito.git.nbm.actions;

import com.google.common.annotations.VisibleForTesting;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.common.IProjectQuery;
import de.adito.git.api.*;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.awt.*;
import org.openide.modules.Modules;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.*;

/**
 * Deploys all local changes that are not committed.
 *
 * @author F.Adler, 27.06.2023
 */
@ActionID(category = "adito/ribbon", id = "de.adito.git.nbm.actions.DeployLocalChangesAction")
@ActionRegistration(displayName = "#LBL_deployProject_Name", iconBase = "#ICON_deployProject_Path")
@ActionReferences({
    @ActionReference(path = "Toolbars/deploy", position = 200, separatorAfter = 250),
    @ActionReference(path = "Shortcuts", name = "SA-F8")})
public class DeployLocalChangesAction extends NodeAction
{
  private final transient INotifyUtil notifyUtil = IGitConstants.INJECTOR.getInstance(INotifyUtil.class);

  @Override
  protected String iconResource()
  {
    return NbBundle.getMessage(DeployLocalChangesAction.class, "ICON_deployProject_Path");
  }

  @Override
  protected void performAction(Node[] pNodes)
  {
    Observable<List<String>> uncommittedFilesObs = RepositoryUtility.getRepositoryObservable()
        .switchMap(pRepo -> pRepo.map(IRepository::getStatus).orElseGet(() -> Observable.just(Optional.empty())))
        .map(pIFileStatus -> pIFileStatus.isPresent() ? getAllUncommittedChanges(pIFileStatus) : Set.<String>of())
        .map(this::getSourcesToDeploy);

    List<String> uncommittedFiles = uncommittedFilesObs.blockingFirst();
    if (!uncommittedFiles.isEmpty())
    {
      deploy(uncommittedFiles);
    }
    else
    {
      notifyUtil.notify(NbBundle.getMessage(getClass(), "LBL_Deploy_Name"), NbBundle.getMessage(getClass(), "LBL_DeployNothingFound_Name"), true);
    }
  }

  @Override
  protected boolean enable(@NotNull Node[] pNodes)
  {
    return getCountOfSelectedProjects(pNodes) == 1;
  }

  /**
   * Get all currently selected nodes as a long
   *
   * @param nodes currently selected nodes
   * @return the number of projects that are selected
   */
  protected int getCountOfSelectedProjects(@NotNull Node[] nodes)
  {
    return ((int) Arrays.stream(nodes)
        .map(pNode -> IProjectQuery
            .getInstance()
            .findProjects(pNode, IProjectQuery.ReturnType.MULTIPLE_TO_NULL))
        .filter(Objects::nonNull)
        .distinct()
        .count());
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(getClass(), "LBL_deployProject_Name");
  }

  @Override
  public HelpCtx getHelpCtx()
  {
    return null;
  }

  /**
   * Get all uncommitted changes including untracked files that are added to the project
   *
   * @param pIFileStatus FileStatus of the current project/repository
   * @return complete list of all uncommitted changes
   */
  public Set<String> getAllUncommittedChanges(@NotNull Optional<IFileStatus> pIFileStatus)
  {
    if (pIFileStatus.isPresent())
    {
      IFileStatus fileStatus = pIFileStatus.get();
      Set<String> uncommittedFiles = new HashSet<>(fileStatus.getUncommittedChanges());
      uncommittedFiles.addAll(fileStatus.getUntracked());
      return uncommittedFiles;
    }
    return new HashSet<>();
  }

  /**
   * Creates a list and returns all sources that are not committed and should be deployed
   *
   * @param pUncommittedFiles The files which status are uncommitted
   * @return The list of sources which should be deployed
   */
  @VisibleForTesting
  List<String> getSourcesToDeploy(@NotNull Set<String> pUncommittedFiles)
  {
    Set<String> uncommittedList = new HashSet<>();
    //the following regex will find the name that has to be deployed
    //e.g. myContext/myEntity/myField/myProcess.js -> myEntity
    String regex = ".+?/([^/]+)/.*";
    Pattern pattern = Pattern.compile(regex);
    for (String path : pUncommittedFiles)
    {
      Matcher matcher = pattern.matcher(path);
      if (matcher.find())
      {
        uncommittedList.add(matcher.group(1));
      }
    }
    return new ArrayList<>(uncommittedList);
  }

  /**
   * Perform the deploy via reflection, since the deploy functionality is part of the ADITO Designer and cannot be called otherwise
   *
   * @param pUncommittedList The list of sources that should be deployed
   */
  @VisibleForTesting
  void deploy(@NotNull List<String> pUncommittedList)
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
      notifyUtil.notify(pE, pE.getMessage(), true);
    }
  }
}

