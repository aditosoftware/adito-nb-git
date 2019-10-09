package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IUserInputPrompt;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    DialogResult<?, char[]> result = dialogProvider.showPasswordPromptDialog(pMessage);
    return new PromptResult(result.isPressedOk(), result.getInformation());
  }

  @Override
  public @NotNull PromptResult promptSSHInfo(@NotNull String pMessage, @Nullable String pSshKeyLocation, @Nullable char[] pPassphrase, @Nullable IKeyStore pKeyStore)
  {
    DialogResult<?, char[]> result = dialogProvider.showSshInfoPromptDialog(pMessage, pSshKeyLocation, pPassphrase, pKeyStore);
    return new PromptResult(result.isPressedOk(), result.getMessage(), result.getInformation());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptText(String pMessage)
  {
    DialogResult result = dialogProvider.showUserPromptDialog(pMessage, null);
    return new PromptResult(result.isPressedOk(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptYesNo(String pMessage)
  {
    DialogResult result = dialogProvider.showYesNoDialog(pMessage);
    return new PromptResult(result.isPressedOk(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptFile(String pMessage)
  {
    DialogResult result = dialogProvider.showFileSelectionDialog(pMessage, "", FileChooserProvider.FileSelectionMode.FILES_ONLY, null);
    return new PromptResult(result.isPressedOk(), result.getMessage());
  }
}
