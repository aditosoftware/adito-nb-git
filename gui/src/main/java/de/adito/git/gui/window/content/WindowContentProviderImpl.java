package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.Optional;

/**
 * @author a.arnold, 31.10.2018
 */
public class WindowContentProviderImpl implements IWindowContentProvider
{

  private final IWindowContentFactory windowContentFactory;

  @Inject
  public WindowContentProviderImpl(IWindowContentFactory pWindowContentFactory)
  {
    windowContentFactory = pWindowContentFactory;
  }

  @Override
  public JComponent createStatusWindowContent(Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createStatusWindowContent(pRepository);
  }

  @Override
  public JComponent createBranchListWindowContent(Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createBranchListWindowContent(pRepository);
  }

  @Override
  public JComponent createCommitHistoryWindowContent(Observable<Optional<IRepository>> pRepository, TableModel pTableModel, Runnable pLoadCallback)
  {
    return windowContentFactory.createCommitHistoryWindowContent(pRepository, pTableModel, pLoadCallback);
  }

  @Override
  public JComponent createStatusLineWindowContent(Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createStatusLineWindowContent(pRepository);
  }

}
