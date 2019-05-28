package de.adito.git.gui.window;

import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import de.adito.git.api.IUserPreferences;
import de.adito.git.api.data.ICommitFilter;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.tablemodels.CommitHistoryTreeListTableModel;
import de.adito.git.impl.CommitHistoryItemsIteratorImpl;

import java.util.List;
import java.util.function.Consumer;

/**
 * Logic for loading/refreshing the entries in the CommitHistoryTableModel
 *
 * @author m.kaspera, 23.05.2019
 */
public class HistoryTableManager
{

  private final CommitHistoryTreeListTableModel tableModel;
  private final IRepository repository;
  private final IUserPreferences userPreferences;
  private CommitHistoryItemsIteratorImpl commitHistoryIterator;

  public HistoryTableManager(IRepository pRepository, ICommitFilter pInitialCommitFilter, IUserPreferences pUserPreferences) throws AditoGitException
  {
    repository = pRepository;
    userPreferences = pUserPreferences;
    commitHistoryIterator = new CommitHistoryItemsIteratorImpl(pRepository.getCommits(pInitialCommitFilter),
                                                               repository.getBranches().blockingFirst().orElse(List.of()),
                                                               repository.getTags());
    List<CommitHistoryTreeListItem> items = commitHistoryIterator.tryReadEntries(userPreferences.getNumLoadAdditionalCHEntries());
    tableModel = new CommitHistoryTreeListTableModel(items);
  }

  public Consumer<ICommitFilter> getFilterChangedConsumer()
  {
    return pNewFilter -> {
      try
      {
        commitHistoryIterator = new CommitHistoryItemsIteratorImpl(repository.getCommits(pNewFilter),
                                                                   repository.getBranches().blockingFirst().orElse(List.of()),
                                                                   repository.getTags());
        List<CommitHistoryTreeListItem> items = commitHistoryIterator.tryReadEntries(userPreferences.getNumLoadAdditionalCHEntries());
        tableModel.resetData(items);
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
    };
  }

  public Runnable getLoadMoreRunnable()
  {
    return () -> tableModel
        .addData(commitHistoryIterator.tryReadEntries(userPreferences.getNumLoadAdditionalCHEntries()));
  }

  public CommitHistoryTreeListTableModel getTableModel()
  {
    return tableModel;
  }

}
