package de.adito.git.gui.dialogs;

import java.util.function.Function;

/**
 * Interface to provide functionality of giving an overlying framework
 * control over the looks of the dialogs
 *
 * @author m.kaspera 28.09.2018
 */
public interface IDialogDisplayer
{

  /**
   * @param pDialogContentSupplier Function that supplies the content for the dialog
   * @param pTitle                 String with title of the dialogs
   * @param pButtons               Buttons that allow the user to accept/deny the dialog or its presented choices
   * @return {@code true} if the "okay" button was pressed, {@code false} if the dialogs was cancelled
   */
  <S extends AditoBaseDialog<T>, T> DialogResult<S, T> showDialog(Function<IDescriptor, S> pDialogContentSupplier, String pTitle, EButtons[] pButtons);

  interface IDescriptor
  {
    void setValid(boolean pValid);
  }

  enum EButtons
  {
    OK("OK"),
    ACCEPT_CHANGES("Accept Changes"),
    CANCEL("Cancel"),
    ABORT("Abort"),
    DISCARD_CHANGES("Discard Changes"),
    STASH_CHANGES("Stash Changes"),
    PUSH("Push"),
    CLOSE("Close"),
    YES("Yes"),
    NO("No"),
    CONFIRM("Confirm"),
    DELETE("Delete"),
    SAVE("Save"),
    COMMIT("Commit"),
    UNSTASH("Unstash commit");

    private final String text;

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

}
