package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.INotifyUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.ISaveUtil;
import de.adito.git.api.data.IBranch;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.dialogs.IDialogProvider;
import de.adito.git.gui.sequences.MergeConflictSequence;
import io.reactivex.rxjava3.core.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 21.07.2021
 */
public class MergeRemoteAction extends MergeAction
{
  @Inject
  MergeRemoteAction(IPrefStore pPrefStore, IAsyncProgressFacade pProgressFacade, IDialogProvider pDialogProvider, ISaveUtil pSaveUtil, INotifyUtil pNotifyUtil,
                    MergeConflictSequence pMergeConflictSequence, @Assisted Observable<Optional<IRepository>> pRepoObs,
                    @Assisted Observable<Optional<IBranch>> pTargetBranch)
  {
    super(pPrefStore, pProgressFacade, pDialogProvider, pSaveUtil, pNotifyUtil, pMergeConflictSequence, pRepoObs, pTargetBranch);
    putValue(Action.NAME, "Fetch and merge into Current");
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    repositoryObservable.blockingFirst().ifPresent(pIRepository -> {
      try
      {
        pIRepository.fetch();
      }
      catch (AditoGitException pE)
      {
        pE.printStackTrace();
      }
    });
    super.actionPerformed(pEvent);
  }


}
