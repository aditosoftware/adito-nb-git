package de.adito.git.gui.actions.commands;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.dialogs.results.IMergeConflictDialogResult;
import de.adito.git.gui.sequences.MergeConflictSequence;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

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
   * @param pMergeConflictSequence MergeConflictSequence used to determine if an Auto-Resolve should be performed and to display the merge itself
   * @param pRepository            current Repository
   * @param pStashedCommitId       sha-1 id of the stashed commit to un-stash
   */
  public static void doUnStashing(@NotNull MergeConflictSequence pMergeConflictSequence, @NotNull String pStashedCommitId,
                                  @NotNull Observable<Optional<IRepository>> pRepository)
  {
    pRepository.blockingFirst().ifPresent(pRepo -> {
      List<IMergeData> stashConflicts;
      try
      {
        stashConflicts = pRepo.unStashChanges(pStashedCommitId);
      }
      catch (AditoGitException pE)
      {
        throw new RuntimeException(pE);
      }
      IMergeConflictDialogResult dialogResult = null;
      if (!stashConflicts.isEmpty())
      {
        dialogResult = pMergeConflictSequence.performMergeConflictSequence(pRepository, stashConflicts, false, "Stash Conflicts");
      }
      if (stashConflicts.isEmpty() || dialogResult.isFinishMerge())
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
