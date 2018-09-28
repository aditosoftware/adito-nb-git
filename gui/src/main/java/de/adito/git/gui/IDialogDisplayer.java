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
     */
    void showDialog(JDialog pDialog, String pTitle);
}
