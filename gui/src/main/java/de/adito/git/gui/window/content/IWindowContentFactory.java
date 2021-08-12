package de.adito.git.gui.window.content;

import com.google.common.collect.Multimap;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.TableModel;
import java.awt.Component;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author m.kaspera 29.10.2018
 */
interface IWindowContentFactory
{

  StatusWindowContent createStatusWindowContent(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull Supplier<Multimap<Integer, Component>> pPopupMenuEntries);

  CommitHistoryWindowContent createCommitHistoryWindowContent(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull TableModel pTableModel,
                                                              @NotNull Runnable pLoadMoreCallback,
                                                              @NotNull Consumer<ICommitFilter> pRefreshContentCallBack,
                                                              @NotNull ICommitFilter pStartFilter);

  BranchWindowContent createBranchWindowContent(@NotNull Observable<Optional<IRepository>> pRepository);

}
