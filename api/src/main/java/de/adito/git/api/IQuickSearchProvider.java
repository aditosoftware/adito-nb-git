package de.adito.git.api;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provider that allows the attachment of a QuickSearch to a component
 *
 * @author m.kaspera, 06.02.2019
 */
public interface IQuickSearchProvider
{

  /**
   * attaches QuickSearch to a component. Component should either be able to add a searchField or handle the display of the search string in another
   * way
   *
   * @param pComponent   JComponent that the quickSearch is registered on, quickSearch listens to keyEvents from this component and tries to attach
   *                     the searchField to this component
   * @param pConstraints Constraints for when the quickSearch attaches the searchField to the component (Layout constraints)
   * @param pCallback    Implementation of the callback that gets called when a user types something, handles search and highlighting
   * @return IQuickSearch object
   */
  @Nullable
  IQuickSearch attach(JComponent pComponent, Object pConstraints, IQuickSearch.ICallback pCallback);

}
