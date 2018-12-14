package de.adito.git.gui.actions.commands;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.dialogs.*;
import io.reactivex.Observable;

import java.util.*;

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
    if (!stashConflicts.isEmpty())
    {
      DialogResult dialogResult = pDialogProvider.showMergeConflictDialog(pRepository, stashConflicts);
      if (dialogResult.isPressedOk())
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

}
