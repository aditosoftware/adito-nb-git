package de.adito.git.api;

/**
 * @author m.kaspera 16.10.2018
 */
public interface IDiscardable
{

  /**
   * clean up all relevant dependencies/connections
   */
  void discard();
}
