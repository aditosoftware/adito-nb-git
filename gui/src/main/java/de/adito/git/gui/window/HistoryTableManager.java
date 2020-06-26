package de.adito.git.gui.window;

import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.tablemodels.CommitHistoryTreeListTableModel;
import de.adito.git.impl.CommitHistoryItemsIteratorImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Logic for loading/refreshing the entries in the CommitHistoryTableModel
 *
 * @author m.kaspera, 23.05.2019
 */
public class HistoryTableManager
{

  private final Object lock = new Object();
  private final CommitHistoryTreeListTableModel tableModel;
  private final IRepository repository;
  private final IUserPreferences userPreferences;
  private CommitHistoryItemsIteratorImpl commitHistoryIterator;

  public HistoryTableManager(IRepository pRepository, IUserPreferences pUserPreferences)
  {
    repository = pRepository;
    userPreferences = pUserPreferences;
    tableModel = new CommitHistoryTreeListTableModel(new ArrayList<>());
  }

  public Consumer<ICommitFilter> getFilterChangedConsumer()
  {
    return pNewFilter -> {
      try
      {
        synchronized (lock)
        {
          commitHistoryIterator = new CommitHistoryItemsIteratorImpl(repository.getCommits(pNewFilter),
                                                                     repository.getBranches().blockingFirst(Optional.empty()).orElse(List.of()),
                                                                     repository.getTags().blockingFirst(List.of()),
                                                                     repository.getCommit(null));
          List<CommitHistoryTreeListItem> items = commitHistoryIterator.tryReadEntries(userPreferences.getNumLoadAdditionalCHEntries());
          tableModel.resetData(items);
        }
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
    };
  }

  public Runnable getLoadMoreRunnable()
  {
    return () -> {
      synchronized (lock)
      {
        tableModel.addData(commitHistoryIterator == null ? List.of() : commitHistoryIterator.tryReadEntries(userPreferences.getNumLoadAdditionalCHEntries()));
      }
    };
  }

  public CommitHistoryTreeListTableModel getTableModel()
  {
    return tableModel;
  }

}
