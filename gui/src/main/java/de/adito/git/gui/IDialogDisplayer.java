package de.adito.git.gui;

import javax.swing.*;

/**
 * Interface to provide functionality of giving an overlying framework
 * control over the looks of the dialog
 *
 * @author m.kaspera 28.09.2018
 */
public interface IDialogDisplayer {

    /**
     *
     * @param pDialog JDialog that should be displayed
     * @param pTitle String with title of the dialog
     * @param okEnabled if the OK button is enabled by default
     * @return {@code true} if the "okay" button was pressed, {@code false} if the dialog was cancelled
     */
    boolean showDialog(JPanel pDialog, String pTitle, boolean okEnabled);

    void disableOKButton();

    void enableOKButton();
}
