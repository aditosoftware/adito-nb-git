package de.adito.git.gui.dialogs;

import org.jetbrains.annotations.Nullable;

/**
 * a util class for dialogs
 *
 * @author a.arnold 31.10.2018
 */
public class DialogResult<S, T> implements IDialogResult<S, T>
{
  private final S source;
  protected final IDialogDisplayer.EButtons selectedButton;
  private final String message;
  private final T information;

  public DialogResult(S pSource, IDialogDisplayer.EButtons pSelectedButton, @Nullable String pMessage, @Nullable T pInformation)
  {
    source = pSource;
    selectedButton = pSelectedButton;
    message = pMessage;
    information = pInformation;
  }

  @Override
  public S getSource()
  {
    return source;
  }


  public IDialogDisplayer.EButtons getSelectedButton()
  {
    return selectedButton;
  }

  @Override
  public String getMessage()
  {
    return message;
  }

  @Override
  public T getInformation()
  {
    return information;
  }
}
