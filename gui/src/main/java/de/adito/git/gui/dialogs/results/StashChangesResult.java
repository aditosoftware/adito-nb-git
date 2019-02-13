package de.adito.git.gui.dialogs.results;

/**
 * @author m.kaspera, 12.02.2019
 */
public class StashChangesResult
{

  private final String stashMessage;
  private final boolean includeUnTracked;

  public StashChangesResult(String pStashMessage, boolean pKeepIndex)
  {
    stashMessage = pStashMessage;
    includeUnTracked = pKeepIndex;
  }

  public String getStashMessage()
  {
    return stashMessage;
  }

  public boolean isIncludeUnTracked()
  {
    return includeUnTracked;
  }
}
