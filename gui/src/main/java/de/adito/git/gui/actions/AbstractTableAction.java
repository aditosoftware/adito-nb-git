package de.adito.git.gui.actions;

import javax.swing.*;

/**
 * Super-class for all actions that can have several rows selected
 *
 * @author m.kaspera 04.10.2018
 */
public abstract class AbstractTableAction extends AbstractAction {

    /**
     *
     * @param name the title of the action (is displayed in a menu)
     */
    AbstractTableAction(String name){
        super(name);
    }

    @Override
    public final boolean isEnabled() {
        return isEnabled0();
    }

    protected abstract boolean isEnabled0();

}
