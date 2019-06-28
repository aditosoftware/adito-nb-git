package de.adito.git.gui.actions.commands;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.util.Collections;
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
    List<IMergeDiff> stashConflicts = pRepository.blockingFirst().map(pRepo -> {
      try
      {
        return pRepo.unStashChanges(pStashedCommitId);
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
    }).orElse(Collections.emptyList());
    DialogResult dialogResult = null;
    if (!stashConflicts.isEmpty())
    {
      dialogResult = pDialogProvider.showMergeConflictDialog(pRepository, stashConflicts, false);
    }
    if (stashConflicts.isEmpty() || dialogResult.isPressedOk())
    {
      pRepository.blockingFirst().ifPresent(pRepo -> {
        try
        {
          pRepo.dropStashedCommit(pStashedCommitId);
        }
        catch (AditoGitException pE)
        {
          throw new RuntimeException(pE);
        }
      });
    }
  }

}
