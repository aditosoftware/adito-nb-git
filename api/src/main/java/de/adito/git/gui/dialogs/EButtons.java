package de.adito.git.gui.dialogs;

/**
 * Defines the different kinds of buttons that can be used in dialogs
 *
 * @author m.kaspera, 24.06.2020
 */
public enum EButtons
{
  ESCAPE("Esc"),
  OK("OK"),
  ACCEPT_CHANGES("Accept Changes"),
  CANCEL("Cancel"),
  ABORT("Abort"),
  DISCARD_CHANGES("Discard Changes"),
  CONTTINUE_MERGE("Continue Merge"),
  ACCEPT_REMAINING("Accept remaining non-conflicting changes and exit"),
  ACCEPT_AS_IS("Accept as is and exit"),
  EXIT_ANYWAY("Exit anyway"),
  STASH_CHANGES("Stash Changes"),
  PUSH("Push"),
  CLOSE("Close"),
  YES("Yes"),
  NO("No"),
  CONFIRM("Confirm"),
  DELETE("Delete"),
  SAVE("Save"),
  COMMIT("Commit"),
  COMMIT_AND_PUSH("Commit & Push"),
  UNSTASH("Unstash commit"),
  CREATE_NEW_BRANCH("Create new remote branch"),
  KEEP_TRACKING("Keep tracking the selected branch"),
  MERGE_REMOTE("Merge remote into current"),
  ACCEPT_YOURS("Accept Left"),
  ACCEPT_THEIRS("Accept Right"),
  SKIP("Skip"),
  AUTO_RESOLVE("Resolve"),
  RESET_HEAD("Reset HEAD"),
  LEAVE_BE("Leave as is");

  private final String text;

  /**
   * @param pText text representation of the enum value
   */
  EButtons(String pText)
  {
    text = pText;
  }


  @Override
  public String toString()
  {
    return text;
  }
}
