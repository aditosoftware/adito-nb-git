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
  <SOURCE_TYPE extends AditoBaseDialog<RESULT_TYPE>, RESULT_TYPE> DialogResult<SOURCE_TYPE, RESULT_TYPE> showDialog(Function<IDescriptor, SOURCE_TYPE> pDialogContentSupplier,
                                                                                                                    String pTitle, EButtons[] pButtons);

  interface IDescriptor
  {
    void setValid(boolean pValid);
  }

}
