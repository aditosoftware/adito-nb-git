package de.adito.git.nbm.window;

import com.google.inject.Inject;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.gui.tableModels.CommitHistoryTreeListTableModel;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.io.File;
import java.util.*;

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
    _openTCinEDT(topComponentFactory.createAllBranchTopComponent(pRepository));
  }

  @Override
  public void showCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, IBranch pBranch)
  {
    IRepository repo = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException(
        NbBundle.getMessage(WindowProviderNBImpl.class, "Invalid.RepositoryNotValid")));
    try
    {
      List<ICommit> commits = repo.getCommits(pBranch, userPreferences.getNumLoadAdditionalCHEntries());
      CommitHistoryTreeListTableModel tableModel = new CommitHistoryTreeListTableModel(repo.getCommitHistoryTreeList(commits, null));
      Runnable loadMoreCallBack = () -> {
        try
        {
          tableModel.addData(
              repo.getCommitHistoryTreeList(
                  repo.getCommits(pBranch, tableModel.getRowCount(), userPreferences.getNumLoadAdditionalCHEntries()),
                  (CommitHistoryTreeListItem) tableModel.getValueAt(tableModel.getRowCount() - 1, 0)));
        }
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
      };
      _openTCinEDT(topComponentFactory.createCommitHistoryTopComponent(pRepository, tableModel, loadMoreCallBack, pBranch != null ?
          pBranch.getSimpleName() : NbBundle.getMessage(WindowProviderNBImpl.class, "Label.CommitsProject")));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showFileCommitHistoryWindow(Observable<Optional<IRepository>> pRepository, File pFile)
  {
    IRepository repo = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException(
        NbBundle.getMessage(WindowProviderNBImpl.class, "Invalid.RepositoryNotValid")));
    try
    {
      List<ICommit> commits = repo.getCommits(pFile, userPreferences.getNumLoadAdditionalCHEntries());
      CommitHistoryTreeListTableModel tableModel = new CommitHistoryTreeListTableModel(repo.getCommitHistoryTreeList(commits, null));
      Runnable loadMoreCallBack = () -> {
        try
        {
          tableModel.addData(
              repo.getCommitHistoryTreeList(
                  repo.getCommits(pFile, tableModel.getRowCount(), userPreferences.getNumLoadAdditionalCHEntries()),
                  (CommitHistoryTreeListItem) tableModel.getValueAt(tableModel.getRowCount() - 1, 0)));
        }
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
      };
      _openTCinEDT(topComponentFactory.createCommitHistoryTopComponent(pRepository, tableModel, loadMoreCallBack, pFile.getAbsolutePath()));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showStatusWindow(Observable<Optional<IRepository>> pRepository)
  {
    _openTCinEDT(topComponentFactory.createStatusWindowTopComponent(pRepository));
  }

  /**
   * A helper class to open TopComponents in EDT
   *
   * @param pComponent The AbstractRepositoryTopComponent that should be displayed
   */
  private static void _openTCinEDT(@NotNull AbstractRepositoryTopComponent pComponent)
  {
    SwingUtilities.invokeLater(() -> {
      pComponent.open();
      WindowManager.getDefault().findMode(pComponent.getInitialMode()).dockInto(pComponent);
      pComponent.requestActive();
    });
  }
}
