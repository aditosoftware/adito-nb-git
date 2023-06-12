package de.adito.git.nbm.window;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommitFilter;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

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

  CommitHistoryTopComponent createCommitHistoryTopComponent(@NonNull Observable<Optional<IRepository>> pRepository, @NonNull TableModel pTableModel,
                                                            @NonNull @Assisted Runnable pLoadMoreCallback,
                                                            @NonNull @Assisted Consumer<ICommitFilter> pRefreshContentCallBack,
                                                            @NonNull ICommitFilter pStartFilter, @Nullable String pDisplayableContext);

  StatusWindowTopComponent createStatusWindowTopComponent(@NonNull Observable<Optional<IRepository>> pRepository);

}
