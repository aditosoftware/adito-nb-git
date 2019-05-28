package de.adito.git.gui.window;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.gui.window.content.IWindowContentProvider;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

/**
 * A provider for all windows (not dialogs). This class only displays the windows
 *
 * @author a.arnold, 31.10.2018
 */
class WindowProviderImpl implements IWindowProvider
{

  private final IWindowContentProvider factory;
  private final IUserPreferences userPreferences;

  @Inject
  public WindowProviderImpl(IWindowContentProvider pProvider, IUserPreferences pUserPreferences)
  {
    factory = pProvider;
    userPreferences = pUserPreferences;
  }

  @Override
  public void showBranchListWindow(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    _showInFrame(factory.createBranchListWindowContent(pRepository));
  }

  @Override
  public void showCommitHistoryWindow(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull ICommitFilter pCommitFilter)
  {
    try
    {
      IRepository repo = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException("No valid repository found"));
      HistoryTableManager historyTableManager = new HistoryTableManager(repo, pCommitFilter, userPreferences);
      _showInFrame(factory.createCommitHistoryWindowContent(pRepository, historyTableManager.getTableModel(),
                                                            historyTableManager.getLoadMoreRunnable(), historyTableManager.getFilterChangedConsumer(), pCommitFilter));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showStatusWindow(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    _showInFrame(factory.createStatusWindowContent(pRepository));
  }

  private void _showInFrame(JComponent pComponent)
  {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(pComponent);
    frame.pack();
    frame.setVisible(true);
  }

}
