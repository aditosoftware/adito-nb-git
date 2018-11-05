package de.adito.git.gui.dialogs;

import javax.swing.*;

/**
 * a class to show dialogs
 *
 * @author a.arnold, 31.10.2018
 */
class DialogDisplayerImpl implements IDialogDisplayer {

    @Override
    public boolean showDialog(JPanel pDialog, String pTitle, boolean okEnabled) {
        throw new RuntimeException("de.adito.git.gui.dialogs.DialogDisplayerImpl.showDialog");
    }

    @Override
    public void disableOKButton() {
        throw new RuntimeException("de.adito.git.gui.dialogs.DialogDisplayerImpl.disableOKButton");
    }

    @Override
    public void enableOKButton() {
        throw new RuntimeException("de.adito.git.gui.dialogs.DialogDisplayerImpl.enableOKButton");
    }

}
