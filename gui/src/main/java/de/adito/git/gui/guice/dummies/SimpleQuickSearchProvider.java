package de.adito.git.gui.guice.dummies;

import de.adito.git.api.*;

import javax.swing.*;

/**
 * @author w.glanzer, 12.02.2019
 */
public class SimpleQuickSearchProvider implements IQuickSearchProvider
{
  @Override
  public IQuickSearch attach(JComponent pComponent, Object pConstraints, IQuickSearch.ICallback pCallback)
  {
    return null;
  }
}
