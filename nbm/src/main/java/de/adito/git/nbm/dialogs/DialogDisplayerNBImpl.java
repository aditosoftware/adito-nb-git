package de.adito.git.nbm.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.*;

/**
 * Implementation of the IActionProvider that shows the dialogs in the
 * fashion of Netbeans
 *
 * @author m.kaspera 28.09.2018
 */
class DialogDisplayerNBImpl implements IDialogDisplayer {

    @Inject
    DialogDisplayerNBImpl() {
    }

    private DialogDescriptor dialogDescriptor;

    /**
     * @param pDialog JDialog that should be displayed
     * @param pTitle  String with title of the dialogs
     * @return {@code true} if the "okay" button was pressed, {@code false} if the dialogs was cancelled
     */
    @Override
    public boolean showDialog(JPanel pDialog, String pTitle, boolean okEnabled) {
        Object[] buttons = new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION};
        dialogDescriptor = new DialogDescriptor(pDialog, pTitle, true, buttons,
                DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null, null);
        if (!okEnabled) {
            dialogDescriptor.setValid(false);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setResizable(true);
        dialog.setMinimumSize(new Dimension(250, 200));
        dialog.pack();
        dialog.setVisible(true);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }

    @Override
    public void disableOKButton() {
        dialogDescriptor.setValid(false);
    }

    @Override
    public void enableOKButton() {
        dialogDescriptor.setValid(true);
    }

}
