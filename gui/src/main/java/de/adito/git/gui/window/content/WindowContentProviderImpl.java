package de.adito.git.gui.window.content;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
  public ILookupComponent<File> createStatusWindowContent(@NonNull Observable<Optional<IRepository>> pRepository,
                                                          @NonNull Supplier<Multimap<Integer, Component>> pPopupMenuEntries)
  {
    return windowContentFactory.createStatusWindowContent(pRepository, pPopupMenuEntries);
  }

  @Override
  public JComponent createCommitHistoryWindowContent(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull TableModel pTableModel,
                                                     @NonNull Runnable pLoadMoreCallback, @NonNull Consumer<ICommitFilter> pFilterChangedCallback,
                                                     @NonNull ICommitFilter pStartFilter)
  {
    return windowContentFactory.createCommitHistoryWindowContent(pRepository, pTableModel, pLoadMoreCallback, pFilterChangedCallback, pStartFilter);
  }

  @Override
  public JComponent createBranchWindowContent(@NonNull Observable<Optional<IRepository>> pRepository)
  {
    return windowContentFactory.createBranchWindowContent(pRepository);
  }

}
