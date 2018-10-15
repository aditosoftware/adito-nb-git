package de.adito.git.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author A.Arnold 11.10.2018
 */

public class SwingToolbar extends JPanel {

    /**
     * The toolbar for Swing
     * @param pActionList the list of actions for the toolbar
     */
    public SwingToolbar(List<Action> pActionList) {
        super(new BorderLayout());

        JToolBar toolBar = new JToolBar("JGit Toolbar");
        for (Action action : pActionList) {
            toolBar.add(action);
        }

        this.add(toolBar, BorderLayout.PAGE_START);
        toolBar.isVisible();
    }
}
