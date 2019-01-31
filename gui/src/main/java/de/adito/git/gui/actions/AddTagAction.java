package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action that prompts the user for a name for the tag, and then tags a commit with that name (if there is only one commit selected)
 *
 * @author m.kaspera, 30.01.2019
 */
public class AddTagAction extends AbstractTableAction
{

  private static final String NOTIFY_CATEGORY = "Adding Tag";
  private static final String ACTION_NAME = "Add Tag";
  private final IDialogProvider dialogProvider;
  private final INotifyUtil notifyUtil;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;

  @Inject
  public AddTagAction(IDialogProvider pDialogProvider, INotifyUtil pNotifyUtil,
                      @Assisted Observable<Optional<IRepository>> pRepository,
                      @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    super(ACTION_NAME, _getIsEnabledObservable(pSelectedCommitObservable));
    dialogProvider = pDialogProvider;
    notifyUtil = pNotifyUtil;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    List<ICommit> selectedCommits = selectedCommitObservable.blockingFirst().orElse(Collections.emptyList());
    if (selectedCommits.size() == 1)
    {
      DialogResult dialogResult = dialogProvider.showUserPromptDialog("Insert the name of the tag");
      if (dialogResult.isPressedOk())
      {
        repository.blockingFirst().ifPresent(pRepoOpt -> pRepoOpt.createTag(dialogResult.getMessage(), selectedCommits.get(0).getId()));
        notifyUtil.notify(NOTIFY_CATEGORY, "Tag added successfully", true);
      }
      else
      {
        notifyUtil.notify(NOTIFY_CATEGORY, "Cancelled adding tag", true);
      }
    }
    else
    {
      notifyUtil.notify(NOTIFY_CATEGORY, "Could not tag commit, more than one or no commit was selected", true);
    }
  }

  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    return pSelectedCommitObservable.map(pSelectedCommits -> Optional.of(pSelectedCommits.orElse(Collections.emptyList()).size() == 1));
  }

}
