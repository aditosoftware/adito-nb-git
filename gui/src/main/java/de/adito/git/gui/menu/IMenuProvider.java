package de.adito.git.gui.menu;

import de.adito.git.api.*;
import io.reactivex.rxjava3.core.Observable;

import javax.swing.*;
import java.util.*;

/**
 * @author m.kaspera, 07.03.2019
 */
public interface IMenuProvider
{

  /**
   * create a Menu that has pTitle as title and one menuItem/Action for each tag in the selected CommitHistoryTreeListItems that performs a delete on
   * that tag
   *
   * @param pTitle            Title of the submenu
   * @param pRepository       Observable with the current Repository
   * @param pSelectedItemsObs Observable of the currently selected CommitHistoryTreeListItems
   * @return JMenu with menu items for each tag
   */
  JMenu getDeleteTagsMenu(String pTitle, Observable<Optional<IRepository>> pRepository,
                          Observable<Optional<List<CommitHistoryTreeListItem>>> pSelectedItemsObs);

}
