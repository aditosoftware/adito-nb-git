package de.adito.git.nbm.window;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
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
    //Set the displayname in the TopComponent of NetBeans.
    displayNameDisposable = pRepository
        .switchMap(pRepoOpt -> pRepoOpt
            .map(IRepository::displayName)
            .orElseGet(() -> Observable.just(NbBundle.getMessage(AbstractRepositoryTopComponent.class, "Invalid.RepositoryNotValid"))))
        .subscribe(pRepoName -> SwingUtilities.invokeLater(() -> setDisplayName(getTopComponentName() + " - " + pRepoName)));
  }

  protected abstract String getInitialMode();

  protected abstract String getTopComponentName();

  @Override
  protected void componentClosed()
  {
    if (!displayNameDisposable.isDisposed())
      displayNameDisposable.dispose();
  }
}
