package de.adito.git.nbm.window;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import javax.annotation.Nullable;
import javax.swing.table.TableModel;
import java.util.Optional;

/**
 * An interface to create all topComponents in NetBeans
 *
 * @author a.arnold, 31.10.2018
 */
interface ITopComponentFactory
{

  AllBranchTopComponent createAllBranchTopComponent(Observable<Optional<IRepository>> pRepository);

  CommitHistoryTopComponent createCommitHistoryTopComponent(Observable<Optional<IRepository>> pRepository,
                                                            TableModel pTableModel, @Assisted("loadMore") Runnable pLoadMoreCallback,
                                                            @Assisted("refreshContent") Runnable pRefreshContentCallBack,
                                                            @Nullable String pDisplayableContext);

  StatusWindowTopComponent createStatusWindowTopComponent(Observable<Optional<IRepository>> pRepository);

}
