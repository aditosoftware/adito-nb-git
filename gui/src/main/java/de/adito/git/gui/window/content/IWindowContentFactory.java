package de.adito.git.gui.window.content;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.TableModel;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author m.kaspera 29.10.2018
 */
interface IWindowContentFactory
{

  StatusWindowContent createStatusWindowContent(@NotNull Observable<Optional<IRepository>> pRepository);

  CommitHistoryWindowContent createCommitHistoryWindowContent(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull TableModel pTableModel,
                                                              @NotNull Runnable pLoadMoreCallback,
                                                              @NotNull Consumer<ICommitFilter> pRefreshContentCallBack,
                                                              @NotNull ICommitFilter pStartFilter);

  StatusLineWindowContent createStatusLineWindowContent(@NotNull Observable<Optional<IRepository>> pRepository);

}
