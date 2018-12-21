package de.adito.git.api;

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
   * Class for storing both the userInput from a prompt and if the user clicked the OK button
   */
  class PromptResult
  {

    private final boolean isPressedOK;
    private final String userInput;

    public PromptResult(boolean pIsPressedOK, String pUserInput)
    {
      isPressedOK = pIsPressedOK;
      userInput = pUserInput;
    }

    /**
     * @return whether the user pressed the OK button or not
     */
    public boolean isPressedOK()
    {
      return isPressedOK;
    }

    /**
     * @return String with the input from the user
     */
    public String getUserInput()
    {
      return userInput;
    }
  }

}
