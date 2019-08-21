package de.adito.git.gui.window.content;

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
public interface IWindowContentProvider
{

  JComponent createStatusWindowContent(@NotNull Observable<Optional<IRepository>> pRepository);

  JComponent createCommitHistoryWindowContent(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull TableModel pTableModel,
                                              @NotNull Runnable pLoadMoreCallback, @NotNull Consumer<ICommitFilter> pFilterChangedCallback,
                                              @NotNull ICommitFilter pStartFilter);

  JComponent createStatusLineWindowContent(@NotNull Observable<Optional<IRepository>> pRepository);

}
