package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for any kind of listenable model. Offers methods to add and remove listeners
 *
 * @author m.kaspera, 19.01.2023
 */
public class ListenableModel<T>
{

  protected List<T> listenerList = new ArrayList<>();

  /**
   * Add a listener to the list that is notified if the model changes
   *
   * @param pListener Listener to be added to the list of active listeners
   */
  void addListener(T pListener)
  {
    listenerList.add(pListener);
  }

  /**
   * Remove a listener from the list that is notified if the model changes
   *
   * @param pListener Listener to be removed from the list of active listeners
   */
  void removeListener(T pListener)
  {
    listenerList.remove(pListener);
  }

}
