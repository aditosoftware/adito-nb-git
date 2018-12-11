package de.adito.git.nbm.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.dialogs.*;
import org.openide.*;

import java.awt.*;
import java.util.function.Function;

/**
 * Implementation of the IActionProvider that shows the dialogs in the
 * fashion of Netbeans
 *
 * @author m.kaspera 28.09.2018
 */
class DialogDisplayerNBImpl implements IDialogDisplayer
{

  @Inject
  DialogDisplayerNBImpl()
  {
  }


  /**
   * @param pTitle String with title of the dialogs
   * @return {@code true} if the "okay" button was pressed, {@code false} if the dialogs was cancelled
   */
  @Override
  public <S extends AditoBaseDialog<T>, T> DialogResult<S, T> showDialog(Function<IDescriptor, S> pDialogContentSupplier, String pTitle)
  {
    Object[] buttons = new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION};
    DialogDescriptor dialogDescriptor = new DialogDescriptor(null, pTitle, true, buttons,
                                                             DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null, null);
    S content = pDialogContentSupplier.apply(dialogDescriptor::setValid);
    dialogDescriptor.setMessage(content);
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
    dialog.setResizable(true);
    dialog.setMinimumSize(new Dimension(250, 200));
    dialog.pack();
    dialog.setVisible(true);
    return new DialogResult<>(content, dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION, content.getMessage(), content.getInformation());
  }

}
