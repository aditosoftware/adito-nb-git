package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ITag;
import io.reactivex.rxjava3.core.Observable;

import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * General Action for deleting tags, the passed Observable determines which tag gets deleted when actionPerformed is called
 *
 * @author m.kaspera, 07.06.2019
 */
class DeleteTagAction extends AbstractTableAction
{

  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<ITag>> tagObservable;

  @Inject
  DeleteTagAction(@Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<ITag>> pTagObservable)
  {
    super("Delete", Observable.just(Optional.of(true)));
    repository = pRepository;
    tagObservable = pTagObservable;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    Optional<IRepository> repo = repository.blockingFirst();
    repo.ifPresent(pIRepository -> tagObservable.blockingFirst().ifPresent(pIRepository::deleteTag));
  }

}
