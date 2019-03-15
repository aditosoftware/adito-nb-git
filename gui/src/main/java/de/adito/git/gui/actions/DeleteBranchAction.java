package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EBranchType;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Action for deleting a branch
 *
 * @author m.kaspera, 13.03.2019
 */
public class DeleteBranchAction extends AbstractTableAction
{

  private final INotifyUtil notifyUtil;
  private final IAsyncProgressFacade progressFacade;
  private final IDialogProvider dialogProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<IBranch>> branch;

  @Inject
  public DeleteBranchAction(INotifyUtil pNotifyUtil, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider,
                            @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<IBranch>> pBranch)
  {
    super("Delete Branch", _getIsEnabledObservable(pBranch));
    notifyUtil = pNotifyUtil;
    progressFacade = pProgressFacade;
    dialogProvider = pDialogProvider;
    repository = pRepository;
    branch = pBranch;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    String branchName = branch.blockingFirst().map(IBranch::getSimpleName).orElse(null);
    if (branchName != null)
    {
      DialogResult<?, Boolean> result = dialogProvider.showDeleteBranchDialog(branchName);
      if (result.isPressedOk())
      {
        progressFacade.executeInBackground("Deleting branch " + branchName, pHandle -> {
          IRepository repo = repository.blockingFirst().orElseThrow(() -> new RuntimeException("no valid repository found"));
          repo.deleteBranch(branchName, result.getInformation());
        });
        notifyUtil.notify("Deleting branch", "Deleting branch was successful", true);
      }
      else
      {
        notifyUtil.notify("Deleting branch", "Delete was aborted", true);
      }
    }
  }

  /**
   * check the selection of columns in branch list
   *
   * @return return true if the selected list has one element, else false
   */
  private static Observable<Optional<Boolean>> _getIsEnabledObservable(Observable<Optional<IBranch>> pBranch)
  {
    return pBranch.map(pBranchOpt -> pBranchOpt.map(branch -> branch.getType() == EBranchType.LOCAL));
  }
}
