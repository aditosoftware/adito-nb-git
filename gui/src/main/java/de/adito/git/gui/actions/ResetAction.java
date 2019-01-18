package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EResetType;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera 05.11.2018
 */
class ResetAction extends AbstractTableAction
{


  private INotifyUtil notifyUtil;
  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  ResetAction(INotifyUtil pNotifyUtil, IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade,
              @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super("Reset current Branch to here", _getIsEnabledObservable(pSelectedCommitObservable));
    notifyUtil = pNotifyUtil;
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    DialogResult<?, EResetType> dialogResult = dialogProvider.showResetDialog();
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    if (selectedCommits.size() == 1 && dialogResult.isPressedOk())
    {
      progressFacade.executeInBackground("Resetting to commit " + selectedCommits.get(0).getId(), pHandle -> {
        repository.blockingFirst()
            .orElseThrow(() -> new RuntimeException("no valid repository found"))
            .reset(selectedCommits.get(0).getId(), dialogResult.getInformation());
        notifyUtil.notify("Reset success", "Resetting back to commit with id "
            + selectedCommits.get(0).getId() + " and option " + dialogResult.getInformation() + " was successful", false);
      });
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(selectedCommits -> Optional.of(selectedCommits.orElse(Collections.emptyList()).size() == 1));
  }

}
