package de.adito.git.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that offers methods to ask the user for specific inputs regarding authentication,
 * such as passwords or passPhrases for ssh keys
 *
 * @author m.kaspera, 20.12.2018
 */
public interface IUserInputPrompt
{

  /**
   * Prompts the user for a password, with pMessage as information for the user
   *
   * @param pMessage String to display to the user, informing him about what he should enter
   * @return PromptResult with information about the entered text by the user and if the user clicked OK
   */
  PromptResult promptPassword(String pMessage);

  /**
   * Prompts the user for a passphrase, with pMessage as information for the user
   *
   * @param pMessage pMessage String to display to the user, informing him about what he should enter
   * @return PromptResult with information about the entered text by the user and if the user clicked OK
   */
  PromptResult promptPassphrase(String pMessage);

  /**
   * Prompt the user for the location and password of their ssh key. If a key is selected, the panel automatically enters the saved password for the selected key if
   * a password for that key was saved at some point
   *
   * @param pMessage        Message to display to the user
   * @param pSshKeyLocation ssh key location, if already known
   * @param pPassphrase     passphrase for the ssh key, if already known
   * @param pKeyStore       KeyStore object, so if a ssh key is chosen in the dialog, an eventually already known passphrase for the key can be automatically loaded
   * @return PromptResult with the information if the user clicked okay and the ssh key location and passphrase
   */
  @NotNull
  PromptResult promptSSHInfo(@NotNull String pMessage, @Nullable String pSshKeyLocation, @Nullable char[] pPassphrase, @NotNull IKeyStore pKeyStore);

  /**
   * Prompts the user for a String (such as a filePath or username), with pMessage as information for the user
   *
   * @param pMessage pMessage String to display to the user, informing him about what he should enter
   * @return PromptResult with information about the entered text by the user and if the user clicked OK
   */
  PromptResult promptText(String pMessage);

  /**
   * Prompts the user with yes/no, with pMessage as information for the user
   *
   * @param pMessage pMessage String to display to the user, informing him about what he should enter
   * @return PromptResult with information about the entered text by the user and if the user clicked OK
   */
  PromptResult promptYesNo(String pMessage);

  /**
   * Prompts the user with a dialog that has an okay and cancel option, as well as a checkbox
   *
   * @param pMessage      String to display to the user, informing him about the purpose of the dialog
   * @param pCheckboxText Text in front of the textbox, explains what the textbox does/is for
   * @return PromptResult, the userInput is the string of the boolean that tells you whether the checkbox was ticket or not
   */
  PromptResult promptYesNoCheckbox(@NotNull String pMessage, @NotNull String pCheckboxText);

  /**
   * Prompts the user to select a file, information from pMessage should tell him which kind of file he should choose
   *
   * @param pMessage pMessage String to display to the user, informing him about what he should enter
   * @return PromptResult with information about the location of the file that the user chose
   */
  PromptResult promptFile(String pMessage);

  /**
   * Class for storing both the userInput from a prompt and if the user clicked the OK button
   */
  class PromptResult
  {

    private final boolean isPressedOK;
    private final String userInput;
    private final char[] userArrayInput;

    /**
     * Use this construcor if you have only a username or the location of an ssh key
     *
     * @param pIsPressedOK whether the user pressed the OK button or not
     * @param pUserInput   String with the input from the user, used for insensitive information such as username or location of an ssh key
     */
    public PromptResult(boolean pIsPressedOK, String pUserInput)
    {
      isPressedOK = pIsPressedOK;
      userInput = pUserInput;
      userArrayInput = null;
    }

    /**
     * Use this constructor if you have only a passphrase or password
     *
     * @param pIsPressedOK whether the user pressed the OK button or not
     * @param pUserArrayInput char array with input from the user (should be used for passwords/sensitive information)
     */
    public PromptResult(boolean pIsPressedOK, char[] pUserArrayInput)
    {
      isPressedOK = pIsPressedOK;
      userInput = null;
      userArrayInput = pUserArrayInput;
    }

    /**
     * Use this constructor if you have a combination of username/password or location of ssh Key/passphrase
     *
     * @param pIsPressedOK    whether the user pressed the OK button or not
     * @param pUserInput      String with the input from the user, used for insensitive information such as username or location of an ssh key
     * @param pUserArrayInput char array with input from the user (should be used for passwords/sensitive information)
     */
    public PromptResult(boolean pIsPressedOK, String pUserInput, char[] pUserArrayInput)
    {
      isPressedOK = pIsPressedOK;
      userInput = pUserInput;
      userArrayInput = pUserArrayInput;
    }

    /**
     * @return whether the user pressed the OK button or not
     */
    public boolean isPressedOK()
    {
      return isPressedOK;
    }

    /**
     * @return String with the input from the user, used for insensitive information such as username or location of an ssh key
     */
    public String getUserInput()
    {
      return userInput;
    }

    /**
     * @return char array with input from the user (should be used for passwords/sensitive information)
     */
    public char[] getUserArrayInput()
    {
      return userArrayInput;
    }
  }

}
