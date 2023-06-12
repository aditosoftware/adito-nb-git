package de.adito.git.gui.window;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.gui.window.content.IWindowContentProvider;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import javax.swing.*;
import java.awt.Component;
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
  public void showCommitHistoryWindow(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull ICommitFilter pCommitFilter)
  {
    try
    {
      IRepository repo = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException(Util.getResource(this.getClass(), "noValidRepoMsg")));
      HistoryTableManager historyTableManager = new HistoryTableManager(repo, userPreferences);
      _showInFrame(factory.createCommitHistoryWindowContent(pRepository, historyTableManager.getTableModel(),
                                                            historyTableManager.getLoadMoreRunnable(), historyTableManager.getFilterChangedConsumer(), pCommitFilter));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void showStatusWindow(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    ArrayListMultimap<Integer, Component> componentMap = ArrayListMultimap.create();
    _showInFrame(factory.createStatusWindowContent(pRepository, () -> componentMap).getComponent());
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
