package de.adito.git.gui.menu;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.ITag;
import de.adito.git.gui.actions.IActionProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import javax.swing.*;
import java.util.*;

/**
 * @author m.kaspera, 07.03.2019
 */
public class DeleteTagsMenu extends JMenu implements IDiscardable
{

  private final Disposable disposable;
  private final Object lock = new Object();
  private final IActionProvider actionProvider;

  @Inject
  public DeleteTagsMenu(IActionProvider pActionProvider, @Assisted String pTitle, @Assisted Observable<Optional<IRepository>> pRepository,
                        @Assisted Observable<Optional<List<CommitHistoryTreeListItem>>> pSelectedItemsObs)
  {
    super(pTitle);
    actionProvider = pActionProvider;
    disposable = pSelectedItemsObs.subscribe(pSelectedItemsOpt -> {
      if (pSelectedItemsOpt.isPresent())
      {
        _refreshEnabled(pSelectedItemsOpt.get());
        _rebuildMenu(pRepository, pSelectedItemsOpt.get());
      }
    });
  }

  private void _rebuildMenu(Observable<Optional<IRepository>> pRepository, List<CommitHistoryTreeListItem> pCommitHistoryTreeListItems)
  {
    synchronized (lock)
    {
      removeAll();
      for (CommitHistoryTreeListItem commitHistoryTreeListItem : pCommitHistoryTreeListItems)
      {
        for (ITag tag : commitHistoryTreeListItem.getTags())
          add(actionProvider.getDeleteSpecificTagAction(pRepository, tag));
      }
    }
  }

  private void _refreshEnabled(List<CommitHistoryTreeListItem> pSelectedItems)
  {
    setEnabled(pSelectedItems.stream().anyMatch(pSelectedItem -> !pSelectedItem.getTags().isEmpty()));
  }


  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
