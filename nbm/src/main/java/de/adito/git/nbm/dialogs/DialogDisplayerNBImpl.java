package de.adito.git.nbm.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
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
    JPanel borderPane = new JPanel(new BorderLayout());
    borderPane.add(content, BorderLayout.CENTER);
    borderPane.setBorder(new EmptyBorder(7, 7, 0, 7));
    dialogDescriptor.setMessage(borderPane);
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
    dialog.setResizable(true);
    dialog.setMinimumSize(new Dimension(250, 50));
    dialog.pack();
    dialog.setVisible(true);
    return new DialogResult<>(content, dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION, content.getMessage(), content.getInformation());
  }

}
