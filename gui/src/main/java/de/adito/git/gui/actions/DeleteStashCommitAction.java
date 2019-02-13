package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.progress.IAsyncProgressFacade;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera, 12.02.2019
 */
public class DeleteStashCommitAction extends AbstractAction
{

  private final IAsyncProgressFacade progressFacade;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<String>> commitId;

  @Inject
  public DeleteStashCommitAction(IAsyncProgressFacade pProgressFacade, @Assisted Observable<Optional<IRepository>> pRepository,
                                 @Assisted Observable<Optional<String>> pCommitId)
  {
    progressFacade = pProgressFacade;
    repository = pRepository;
    commitId = pCommitId;
  }

  @Override
  public void actionPerformed(ActionEvent pEvent)
  {
    repository.blockingFirst().ifPresent(repo -> progressFacade.executeInBackground("unStashing changes", pHandle -> {
      String commitHash = commitId.blockingFirst().orElse(null);
      if (commitHash != null)
        repo.dropStashedCommit(commitHash);
    }));
  }
}
