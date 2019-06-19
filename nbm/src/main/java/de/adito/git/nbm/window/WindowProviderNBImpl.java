package de.adito.git.nbm.window;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.gui.window.HistoryTableManager;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A provider for all Windows in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
class WindowProviderNBImpl implements IWindowProvider
{
  private static final Logger LOGGER = Logger.getLogger(WindowProviderNBImpl.class.getName());
  private final ITopComponentFactory topComponentFactory;
  private final IUserPreferences userPreferences;

  @Inject
  public WindowProviderNBImpl(ITopComponentFactory pTopComponentFactory, IUserPreferences pUserPreferences)
  {
    topComponentFactory = pTopComponentFactory;
    userPreferences = pUserPreferences;
  }

  @Override
  public void showBranchListWindow(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    _SingletonIdentifier identifier = new _SingletonIdentifier(pRepository, "showBranchListWindow");
    _openTCinEDT(identifier, () -> topComponentFactory.createAllBranchTopComponent(pRepository));
  }

  @Override
  public void showCommitHistoryWindow(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull ICommitFilter pCommitFilter)
  {
    try
    {
      IRepository repo = _getRepository(pRepository);
      HistoryTableManager historyTableManager = new HistoryTableManager(repo, userPreferences);

      _SingletonIdentifier identifier = new _SingletonIdentifier(pRepository, "CommitHistoryWindow" + (pCommitFilter.getFiles().isEmpty()
          ? "" : pCommitFilter.getFiles()));
      String title = NbBundle.getMessage(WindowProviderNBImpl.class, "Label.Commits") + (pCommitFilter.getFiles().isEmpty() ? "" : pCommitFilter.getFiles());
      _openTCinEDT(identifier, () -> topComponentFactory
          .createCommitHistoryTopComponent(pRepository, historyTableManager.getTableModel(),
                                           historyTableManager.getLoadMoreRunnable(), historyTableManager.getFilterChangedConsumer(), pCommitFilter, title));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showStatusWindow(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    _SingletonIdentifier identifier = new _SingletonIdentifier(pRepository, "showStatusWindow");
    _openTCinEDT(identifier, () -> topComponentFactory.createStatusWindowTopComponent(pRepository));
  }

  @NotNull
  private static IRepository _getRepository(@NotNull Observable<Optional<IRepository>> pRepo)
  {
    return pRepo
        .blockingFirst()
        .orElseThrow(() -> new RuntimeException(NbBundle.getMessage(WindowProviderNBImpl.class,
                                                                    "Invalid.RepositoryNotValid")));
  }

  /**
   * A helper class to open TopComponents in EDT
   *
   * @param pComponentSupplier The supplier for the AbstractRepositoryTopComponent that should be displayed
   */
  private static void _openTCinEDT(@NotNull _SingletonIdentifier pIdentifier, @NotNull Supplier<AbstractRepositoryTopComponent> pComponentSupplier)
  {
    for (TopComponent openedTC : TopComponent.getRegistry().getOpened())
    {
      if (Objects.equals(pIdentifier, openedTC.getClientProperty("adito.git.windowprovider.key")))
      {
        SwingUtilities.invokeLater(openedTC::requestActive);
        return;
      }
    }

    SwingUtilities.invokeLater(() -> {
      AbstractRepositoryTopComponent tc = pComponentSupplier.get();
      tc.putClientProperty("adito.git.windowprovider.key", pIdentifier);
      Mode tcMode = WindowManager.getDefault().findMode(tc.getInitialMode());
      if (tcMode == null)
      {
        LOGGER.log(Level.WARNING, () -> String.format("Could not find valid mode for initial mode %s given by TopComponent %s, falling back to output mode",
                                                      tc.getInitialMode(), tc.getTopComponentName()));
        tcMode = WindowManager.getDefault().findMode("output");
      }
      tcMode.dockInto(tc);
      if (!tc.isOpened())
        tc.open();
      tc.requestActive();
    });
  }

  private static class _SingletonIdentifier
  {
    private Observable<Optional<IRepository>> repoObservable;
    private Set<Object> objects;

    _SingletonIdentifier(Observable<Optional<IRepository>> pRepoObservable, Object... pObjects)
    {
      repoObservable = pRepoObservable;
      objects = Set.of(pObjects);
    }

    @Override
    public boolean equals(Object pO)
    {
      if (this == pO) return true;
      if (pO == null || getClass() != pO.getClass()) return false;
      _SingletonIdentifier that = (_SingletonIdentifier) pO;

      try
      {
        IRepository repo1 = _getRepository(repoObservable);
        IRepository repo2 = _getRepository(that.repoObservable);
        return Objects.equals(repo1, repo2) && Objects.equals(objects, that.objects);
      }
      catch (Exception e)
      {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      try
      {
        IRepository repo = _getRepository(repoObservable);
        return Objects.hash(repo, objects);
      }
      catch (Exception e)
      {
        return Objects.hash(repoObservable, objects);
      }
    }
  }
}
