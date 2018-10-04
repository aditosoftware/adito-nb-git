package de.adito.git.nbm;

import com.google.inject.Inject;
import de.adito.git.gui.IDialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.*;

/**
 * Implementation of the IDialogDisplayer that shows the dialog in the
 * fashion of Netbeans
 *
 * @author m.kaspera 28.09.2018
 */
public class DialogDisplayerImpl implements IDialogDisplayer {

    @Inject
    DialogDisplayerImpl(){
    }

    /**
     *
     * @param pDialog JDialog that should be displayed
     * @param pTitle String with title of the dialog
     * @return {@code true} if the "okay" button was pressed, {@code false} if the dialog was cancelled
     */
    @Override
    public boolean showDialog(JPanel pDialog, String pTitle) {
        Object[] buttons = new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION};
        DialogDescriptor dialogDescriptor = new DialogDescriptor(pDialog, pTitle, true, buttons,
                DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setResizable(true);
        dialog.setMinimumSize(new Dimension(250, 200));
        dialog.pack();
        dialog.setVisible(true);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }
}
