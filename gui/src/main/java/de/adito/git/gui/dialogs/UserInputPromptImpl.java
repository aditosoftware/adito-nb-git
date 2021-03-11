package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import de.adito.git.api.IKeyStore;
import de.adito.git.api.IUserInputPrompt;
import de.adito.git.gui.dialogs.filechooser.FileChooserProvider;
import de.adito.git.gui.dialogs.results.IFileSelectionDialogResult;
import de.adito.git.gui.dialogs.results.IUserPromptDialogResult;
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
    IUserPromptDialogResult<?, ?> result = dialogProvider.showPasswordPromptDialog(pMessage);
    return new PromptResult(result.isOkay(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptPassphrase(String pMessage)
  {
    IUserPromptDialogResult<?, char[]> result = dialogProvider.showPasswordPromptDialog(pMessage);
    return new PromptResult(result.isOkay(), result.getInformation());
  }

  @Override
  public @NotNull PromptResult promptSSHInfo(@NotNull String pMessage, @Nullable String pSshKeyLocation, @Nullable char[] pPassphrase, @NotNull IKeyStore pKeyStore)
  {
    IUserPromptDialogResult<?, char[]> result = dialogProvider.showSshInfoPromptDialog(pMessage, pSshKeyLocation, pPassphrase, pKeyStore);
    return new PromptResult(result.isOkay(), result.getMessage(), result.getInformation());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptText(String pMessage)
  {
    IUserPromptDialogResult<?, ?> result = dialogProvider.showUserPromptDialog(pMessage, null);
    return new PromptResult(result.isOkay(), result.getMessage());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptYesNo(String pMessage)
  {
    IUserPromptDialogResult<?, ?> result = dialogProvider.showYesNoDialog(pMessage);
    return new PromptResult(result.isOkay(), result.getMessage());
  }

  @Override
  public PromptResult promptYesNoCheckbox(@NotNull String pMessage, @NotNull String pCheckboxText)
  {
    IUserPromptDialogResult<?, Boolean> result = dialogProvider.showCheckboxPrompt(pMessage, pCheckboxText);
    return new PromptResult(result.isOkay(), result.getInformation().toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PromptResult promptFile(String pMessage)
  {
    IFileSelectionDialogResult<?, ?> result = dialogProvider.showFileSelectionDialog(pMessage, FileChooserProvider.FileSelectionMode.FILES_ONLY, null, null);
    return new PromptResult(result.acceptFiles(), result.getMessage());
  }
}
