package de.adito.git.gui.dialogs;

import org.jetbrains.annotations.Nullable;

/**
 * a util class for dialogs
 *
 * @author a.arnold 31.10.2018
 */
public class DialogResult<S, T>
{
  private S source;
  private boolean pressedOk;
  private String message;
  private T information;

  public DialogResult(S pSource, boolean pPressedOk, @Nullable String pMessage, @Nullable T pInformation)
  {
    source = pSource;
    pressedOk = pPressedOk;
    message = pMessage;
    information = pInformation;
  }

  S getSource()
  {
    return source;
  }

  public boolean isPressedOk()
  {
    return pressedOk;
  }

  public String getMessage()
  {
    return message;
  }

  public T getInformation()
  {
    return information;
  }
}
