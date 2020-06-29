package de.adito.git.gui.menu;

import de.adito.git.api.*;
import io.reactivex.rxjava3.core.Observable;

import java.util.*;

/**
 * @author m.kaspera, 07.03.2019
 */
interface IMenuFactory
{

  DeleteTagsMenu createDeleteTagsMenu(String pTitle, Observable<Optional<IRepository>> pRepository,
                                      Observable<Optional<List<CommitHistoryTreeListItem>>> pSelectedItemsObs);

}
