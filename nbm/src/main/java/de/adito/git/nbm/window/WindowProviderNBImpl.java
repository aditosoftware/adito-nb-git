package de.adito.git.nbm.window;

import com.google.inject.Inject;
import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.tablemodels.CommitHistoryTreeListTableModel;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.function.Supplier;

/**
 * A provider for all Windows in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
class WindowProviderNBImpl implements IWindowProvider
{
  private final ITopComponentFactory topComponentFactory;
  private final IUserPreferences userPreferences;

  @Inject
  public WindowProviderNBImpl(ITopComponentFactory pTopComponentFactory, IUserPreferences pUserPreferences)
  {
    topComponentFactory = pTopComponentFactory;
    userPreferences = pUserPreferences;
  }

  @Override
  public void showBranchListWindow(Observable<Optional<IRepository>> pRepository)
  {
    _SingletonIdentifier identifier = new _SingletonIdentifier(pRepository, "showBranchListWindow");
    _openTCinEDT(identifier, () -> topComponentFactory.createAllBranchTopComponent(pRepository));
  }

  @Override
  public void showCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, IBranch pBranch)
  {
    try
    {
      IRepository repo = _getRepository(pRepository);
      List<ICommit> commits = repo.getCommits(pBranch, userPreferences.getNumLoadAdditionalCHEntries());
      CommitHistoryTreeListTableModel tableModel = new CommitHistoryTreeListTableModel(repo.getCommitHistoryTreeList(commits, null));
      Runnable loadMoreCallBack = () -> {
        try
        {
          if (tableModel.getRowCount() > 0)
          {
            tableModel.addData(
                repo.getCommitHistoryTreeList(
                    repo.getCommits(pBranch, tableModel.getRowCount(), userPreferences.getNumLoadAdditionalCHEntries()),
                    (CommitHistoryTreeListItem) tableModel.getValueAt(tableModel.getRowCount() - 1, 0)));
          }
        }
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
      };
      Runnable refreshContentCallBack = () -> {
        try
        {
          tableModel.resetData(repo.getCommitHistoryTreeList(
              repo.getCommits(pBranch, 0, userPreferences.getNumLoadAdditionalCHEntries()), null));
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
      };

      _SingletonIdentifier identifier = new _SingletonIdentifier(pRepository, "showCommitHistoryWindow", pBranch == null ? "null" : pBranch.getName());
      _openTCinEDT(identifier, () -> topComponentFactory
                       .createCommitHistoryTopComponent(pRepository, tableModel, loadMoreCallBack, refreshContentCallBack,
                                                        pBranch != null ? pBranch.getSimpleName() : null));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showFileCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, File pFile)
  {
    try
    {
      IRepository repo = _getRepository(pRepository);
      List<ICommit> commits = repo.getCommits(pFile, userPreferences.getNumLoadAdditionalCHEntries());
      CommitHistoryTreeListTableModel tableModel = new CommitHistoryTreeListTableModel(repo.getCommitHistoryTreeList(commits, null));
      Runnable loadMoreCallBack = () -> {
        try
        {
          if (tableModel.getRowCount() > 0)
          {
            tableModel.addData(
                repo.getCommitHistoryTreeList(
                    repo.getCommits(pFile, tableModel.getRowCount(), userPreferences.getNumLoadAdditionalCHEntries()),
                    (CommitHistoryTreeListItem) tableModel.getValueAt(tableModel.getRowCount() - 1, 0)));
          }
        }
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
      };
      Runnable refreshContentCallBack = () -> {
        try
        {
          tableModel.resetData(repo.getCommitHistoryTreeList(
              repo.getCommits(pFile, 0, userPreferences.getNumLoadAdditionalCHEntries()), null));
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
      };

      String path = pFile.getAbsolutePath();
      _SingletonIdentifier identifier = new _SingletonIdentifier(pRepository, "showFileCommitHistoryWindow", path);
      _openTCinEDT(identifier, () -> topComponentFactory
          .createCommitHistoryTopComponent(pRepository, tableModel, loadMoreCallBack, refreshContentCallBack, path));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showStatusWindow(Observable<Optional<IRepository>> pRepository)
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
      WindowManager.getDefault().findMode(tc.getInitialMode()).dockInto(tc);
      if (!tc.isOpened())
        tc.open();
      tc.requestActive();
    });
  }

  private static class _SingletonIdentifier
  {
    private Observable<Optional<IRepository>> repoObservable;
    private Set<Object> objects;

    public _SingletonIdentifier(Observable<Optional<IRepository>> pRepoObservable, Object... pObjects)
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
