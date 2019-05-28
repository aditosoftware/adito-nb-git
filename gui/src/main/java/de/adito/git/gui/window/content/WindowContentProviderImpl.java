package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.Optional;
import java.util.function.Consumer;

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
  public JComponent createStatusWindowContent(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createStatusWindowContent(pRepository);
  }

  @Override
  public JComponent createBranchListWindowContent(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createBranchListWindowContent(pRepository);
  }

  @Override
  public JComponent createCommitHistoryWindowContent(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull TableModel pTableModel,
                                                     @NotNull Runnable pLoadMoreCallback, @NotNull Consumer<ICommitFilter> pFilterChangedCallback,
                                                     @NotNull ICommitFilter pStartFilter)
  {
    return windowContentFactory.createCommitHistoryWindowContent(pRepository, pTableModel, pLoadMoreCallback, pFilterChangedCallback, pStartFilter);
  }

  @Override
  public JComponent createStatusLineWindowContent(@NotNull Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createStatusLineWindowContent(pRepository);
  }

}
