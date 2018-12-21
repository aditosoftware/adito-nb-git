package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.api.IUserInputPrompt;

/**
 * Implementation of the IUserInputPrompt interface, uses the DialogProvider from the gui package
 *
 * @author m.kaspera, 20.12.2018
 */
public class UserInputPromptImpl implements IUserInputPrompt
{

  private final IDialogProvider dialogProvider;

  @Inject
  UserInputPromptImpl(IDialogProvider pDialogProvider)
  {
    dialogProvider = pDialogProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptPassword(String pMessage)
  {
    DialogResult result = dialogProvider.showPasswordPromptDialog(pMessage);
    return new PromptResult(result.isPressedOk(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptPassphrase(String pMessage)
  {
    DialogResult result = dialogProvider.showPasswordPromptDialog(pMessage);
    return new PromptResult(result.isPressedOk(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptText(String pMessage)
  {
    DialogResult result = dialogProvider.showUserPromptDialog(pMessage);
    return new PromptResult(result.isPressedOk(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptYesNo(String pMessage)
  {
    throw new RuntimeException("UserInputPromptImpl.promptYesNo not implemented yet");
  }
}
