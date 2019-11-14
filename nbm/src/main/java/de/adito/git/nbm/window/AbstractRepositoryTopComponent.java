package de.adito.git.nbm.window;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.nbm.util.ProjectUtility;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * An abstract class to show the topComponents in NetBeans
 *
 * @author a.arnold, 24.10.2018
 */
abstract class AbstractRepositoryTopComponent extends TopComponent
{
  private final Disposable displayNameDisposable;

  /**
   * Abstract class to give all TopComponents
   *
   * @param pRepository Observable of the Repository for the selected project
   */
  AbstractRepositoryTopComponent(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    super(_initLookup());
    //Set the displayname in the TopComponent of NetBeans.
    displayNameDisposable = pRepository
        .switchMap(pRepoOpt -> pRepoOpt
            .map(IRepository::displayName)
            .orElseGet(() -> Observable.just(NbBundle.getMessage(AbstractRepositoryTopComponent.class, "Invalid.RepositoryNotValid"))))
        .subscribe(pRepoName -> SwingUtilities.invokeLater(() -> setDisplayName(getTopComponentName() + " - " + pRepoName)));
  }

  /**
   * initializes Lookup with the current Project, or an empty lookup if none can be found
   *
   * @return fixed Lookup with either the current Project or an empty lookup
   */
  private static Lookup _initLookup()
  {
    Optional<Project> optionalProject = ProjectUtility.findProjectFromActives(TopComponent.getRegistry());
    if (optionalProject.isPresent())
      return Lookups.fixed(optionalProject.get());
    return Lookups.fixed();
  }

  protected abstract String getInitialMode();

  protected abstract String getTopComponentName();

  @Override
  protected void componentClosed()
  {
    if (!displayNameDisposable.isDisposed())
      displayNameDisposable.dispose();

    // Discard all discardable components
    Arrays.stream(getComponents())
        .filter(IDiscardable.class::isInstance)
        .map(IDiscardable.class::cast)
        .forEach(pDiscardable -> {
          try
          {
            pDiscardable.discard();
          }
          catch (Exception ignored)
          {
            // ignored
          }
        });
  }
}
