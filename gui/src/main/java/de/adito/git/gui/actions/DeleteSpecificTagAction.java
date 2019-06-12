package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ITag;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Action that deletes a specific task, happens ot carry the name of the tag to be deleted
 *
 * @author m.kaspera, 07.03.2019
 */
class DeleteSpecificTagAction extends AbstractTableAction
{

  private final ITag tag;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  DeleteSpecificTagAction(@Assisted Observable<Optional<IRepository>> pRepository, @Assisted ITag pTag)
  {
    super(pTag.getName(), Observable.just(Optional.of(true)));
    tag = pTag;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    Optional<IRepository> repo = repository.blockingFirst();
    repo.ifPresent(pIRepository -> pIRepository.deleteTag(tag));
  }
}
