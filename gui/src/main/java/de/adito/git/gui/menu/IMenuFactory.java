package de.adito.git.gui.menu;

import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 07.03.2019
 */
interface IMenuFactory
{

  DeleteTagsMenu createDeleteTagsMenu(String pTitle, Observable<Optional<IRepository>> pRepository,
                                      Observable<Optional<List<CommitHistoryTreeListItem>>> pSelectedItemsObs);

}
