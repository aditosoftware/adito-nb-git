package de.adito.git.gui.menu;

import com.google.inject.Inject;
import de.adito.git.api.*;
import io.reactivex.rxjava3.core.Observable;

import javax.swing.*;
import java.util.*;

/**
 * @author m.kaspera, 07.03.2019
 */
class MenuProvider implements IMenuProvider
{

  private final IMenuFactory menuFactory;

  @Inject
  MenuProvider(IMenuFactory pMenuFactory)
  {
    menuFactory = pMenuFactory;
  }

  @Override
  public JMenu getDeleteTagsMenu(String pTitle, Observable<Optional<IRepository>> pRepository,
                                 Observable<Optional<List<CommitHistoryTreeListItem>>> pSelectedItemsObs)
  {
    return menuFactory.createDeleteTagsMenu(pTitle, pRepository, pSelectedItemsObs);
  }
}
