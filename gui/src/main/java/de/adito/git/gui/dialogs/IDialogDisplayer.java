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
   * @param pDialogContentSupplier
   * @param pTitle                 String with title of the dialogs
   * @return {@code true} if the "okay" button was pressed, {@code false} if the dialogs was cancelled
   */
  <S extends AditoBaseDialog<T>, T> DialogResult<S, T> showDialog(Function<IDescriptor, S> pDialogContentSupplier, String pTitle);

  interface IDescriptor
  {
    void setValid(boolean pValid);
  }

}
