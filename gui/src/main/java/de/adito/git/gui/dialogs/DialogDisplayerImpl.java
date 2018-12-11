package de.adito.git.gui.dialogs;

import java.util.function.Function;

/**
 * a class to show dialogs
 *
 * @author a.arnold, 31.10.2018
 */
class DialogDisplayerImpl implements IDialogDisplayer
{

  @Override
  public <S extends AditoBaseDialog<T>, T> DialogResult<S, T> showDialog(Function<IDescriptor, S> pDialogContentSupplier,
                                                                         String pTitle)
  {
    throw new RuntimeException("de.adito.git.gui.dialogs.DialogDisplayerImpl.showDialog");
  }
}
