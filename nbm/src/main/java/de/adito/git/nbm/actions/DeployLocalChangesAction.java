package de.adito.git.nbm.actions;

import com.google.common.annotations.VisibleForTesting;
import de.adito.git.api.*;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.nbm.IGitConstants;
import de.adito.git.nbm.util.RepositoryUtility;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.openide.awt.*;
import org.openide.modules.Modules;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.*;

/**
 * Deploys all local changes that are not committed.
 *
 * @author F.Adler, 27.06.2023
 */
@ActionID(category = "adito/ribbon", id = "de.adito.git.nbm.actions.DeployLocalChangesAction")
@ActionRegistration(displayName = "#LBL_deployProject_Name", iconBase = "de/adito/git/nbm/actions/indent_dark.png")
@ActionReferences({
    @ActionReference(path = "Toolbars/deploy", position = 200, separatorAfter = 250),
    @ActionReference(path = "Shortcuts", name = "SA-F8")})
public class DeployLocalChangesAction extends NBAction
{
  private final transient INotifyUtil notifyUtil = IGitConstants.INJECTOR.getInstance(INotifyUtil.class);

  @Override
  protected String iconResource()
  {
    return "de/adito/git/nbm/actions/indent_dark.png";
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

  @VisibleForTesting
  boolean isEnabled(@Nullable IRepository pRepository)
  {
    if (pRepository == null)
      return false;

    Observable<Object> res = pRepository
        .getStatus()
        .map(pIFileStatus ->
                 pIFileStatus
                     .map(IFileStatus::hasUncommittedChanges)
                     .orElse(false)
        );

    return !Boolean.FALSE.equals(res.blockingFirst());
  }

  @Override
  protected Observable<Optional<Boolean>> getIsEnabledObservable(@NonNull Observable<Optional<IRepository>> pRepositoryObservable)
  {
    return pRepositoryObservable.map(pRepoOpt -> pRepoOpt.map(this::isEnabled));
  }

  @Override
  public String getName()
  {
    return NbBundle.getMessage(getClass(), "LBL_deployProject_Name");
  }


  /**
   * Get all uncommitted changes including untracked files that are added to the project
   *
   * @param pIFileStatus FileStatus of the current project/repository
   * @return complete list of all uncommitted changes
   */
  public Set<String> getAllUncommittedChanges(@NonNull Optional<IFileStatus> pIFileStatus)
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
  List<String> getSourcesToDeploy(@NonNull Set<String> pUncommittedFiles)
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
      notifyUtil.notify(pE, pE.getMessage(), true);
    }
  }
}

