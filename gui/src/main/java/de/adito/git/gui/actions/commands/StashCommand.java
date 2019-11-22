package de.adito.git.gui.actions.commands;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 14.12.2018
 */
public class StashCommand
{

  private StashCommand()
  {
  }

  /**
   * @param pRepository      current Repository
   * @param pStashedCommitId sha-1 id of the stashed commit to un-stash
   */
  public static void doUnStashing(IDialogProvider pDialogProvider, String pStashedCommitId, Observable<Optional<IRepository>> pRepository)
  {
    pRepository.blockingFirst().ifPresent(pRepo -> {
      List<IMergeDiff> stashConflicts;
      try
      {
        pRepo.setUpdateFlag(false);
        stashConflicts = pRepo.unStashChanges(pStashedCommitId);
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
      finally
      {
        pRepo.setUpdateFlag(true);
      }
      DialogResult dialogResult = null;
      if (!stashConflicts.isEmpty())
      {
        dialogResult = pDialogProvider.showMergeConflictDialog(pRepository, stashConflicts, false, "Stash Conflicts");
      }
      if (stashConflicts.isEmpty() || dialogResult.isPressedOk())
      {
        try
        {
          pRepo.dropStashedCommit(pStashedCommitId);
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
      }
    });
  }

}
