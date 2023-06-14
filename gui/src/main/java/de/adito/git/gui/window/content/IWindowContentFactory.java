package de.adito.git.gui.window.content;

import com.google.common.collect.Multimap;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

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

  StatusWindowContent createStatusWindowContent(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull Supplier<Multimap<Integer, Component>> pPopupMenuEntries);

  CommitHistoryWindowContent createCommitHistoryWindowContent(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull TableModel pTableModel,
                                                              @NonNull Runnable pLoadMoreCallback,
                                                              @NonNull Consumer<ICommitFilter> pRefreshContentCallBack,
                                                              @NonNull ICommitFilter pStartFilter);

  BranchWindowContent createBranchWindowContent(@NonNull Observable<Optional<IRepository>> pRepository);

}
