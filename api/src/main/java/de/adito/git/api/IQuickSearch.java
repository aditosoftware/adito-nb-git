package de.adito.git.api;

/**
 * Wrapper around a QuickSearch implementation
 *
 * @author m.kaspera, 06.02.2019
 */
public interface IQuickSearch
{

  /**
   * @param pEnabled whether or not quicksearch is active
   */
  void setEnabled(boolean pEnabled);

  /**
   * @return whether or not quicksearch is active
   */
  boolean isEnabled();

  /**
   * @param pIsAlwaysShown whether or not the searchField is always shown
   */
  void setAlwaysShown(boolean pIsAlwaysShown);

  /**
   * @return whether or not the searchField is always shown
   */
  boolean isAlwaysShown();

  /**
   * removes quicksearch from the component it is attached to
   */
  void detach();

  /**
   * Interface defining the methods that get called by the QuickSearch to do the actual searching
   */
  interface ICallback
  {

    /**
     * Try to find strings with the prefix, if exactly one matches return the complete string
     *
     * @param pPrefix prefix to look for
     * @return String containing the prefix if one was found, the prefix if several match or an empty string if nothing can be found with the prefix
     */
    String findMaxPrefix(String pPrefix);

    /**
     * called when the user cancels the quicksearch, either by hitting esc or clicking somewhere not in the searchField
     */
    void quickSearchCancelled();

    /**
     * called when the user confirms the quicksearch
     */
    void quickSearchConfirmed();

    /**
     * called when the text searched by the user changes in any way, recalculate the matching results
     *
     * @param pSearchText the whole text that the user is searching for
     */
    void quickSearchUpdate(String pSearchText);

    /**
     * called when the user presses up/down, select the next/previous matching result. Should also wrap around the end/beginning of the result list
     *
     * @param pForward whether to cycle forward or backward through the matching results
     */
    void showNextSelection(boolean pForward);

  }

}
