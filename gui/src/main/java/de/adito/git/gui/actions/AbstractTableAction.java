package de.adito.git.gui.actions;

import javax.swing.*;

/**
 * Super-class for all actions that can have several rows selected
 *
 * @author m.kaspera 04.10.2018
 */
public abstract class AbstractTableAction extends AbstractAction {

    // storage for the selected rows by the user
    protected int[] rows;

    /**
     *
     * @param name the title of the action (is displayed in a menu)
     */
    protected AbstractTableAction(String name){
        super(name);
    }

    /**
     * set rows to row numbers, check if action should be enabled/disabled
     *
     * @param rowNumbers the rows selected by the user
     */
    public abstract void setRows(int[] rowNumbers);

}