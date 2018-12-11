package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * @author m.kaspera 05.11.2018
 */
class ResetAction extends AbstractTableAction
{


  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  ResetAction(IDialogProvider pDialogProvider, @Assisted Observable<Optional<IRepository>> pRepository,
              @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super("Reset current Branch to here", _getIsEnabledObservable(pSelectedCommitObservable));
    dialogProvider = pDialogProvider;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    DialogResult<?, EResetType> dialogResult = dialogProvider.showResetDialog();
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    if (selectedCommits.size() == 1)
    {
      try
      {
        repository
            .blockingFirst()
            .orElseThrow(() -> new RuntimeException("no valid repository found"))
            .reset(selectedCommits.get(0).getId(), dialogResult.getInformation());
      }
      catch (Exception e1)
      {
        throw new RuntimeException(e1);
      }
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(selectedCommits -> Optional.of(selectedCommits.orElse(Collections.emptyList()).size() == 1));
  }

}
