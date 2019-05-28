package de.adito.git.nbm.window;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.table.TableModel;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An interface to create all topComponents in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
interface ITopComponentFactory
{

  AllBranchTopComponent createAllBranchTopComponent(@NotNull Observable<Optional<IRepository>> pRepository);

  CommitHistoryTopComponent createCommitHistoryTopComponent(@NotNull Observable<Optional<IRepository>> pRepository, @NotNull TableModel pTableModel,
                                                            @NotNull @Assisted Runnable pLoadMoreCallback,
                                                            @NotNull @Assisted Consumer<ICommitFilter> pRefreshContentCallBack,
                                                            @NotNull ICommitFilter pStartFilter, @Nullable String pDisplayableContext);

  StatusWindowTopComponent createStatusWindowTopComponent(@NotNull Observable<Optional<IRepository>> pRepository);

}
