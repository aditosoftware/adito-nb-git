package de.adito.git;

import com.google.inject.Inject;
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
     */
    @Override
    public void showDialog(JDialog pDialog, String pTitle) {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(pDialog, pTitle);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setResizable(true);
        dialog.setMinimumSize(new Dimension(250, 200));
        dialog.pack();
        dialog.setVisible(true);
    }
}
