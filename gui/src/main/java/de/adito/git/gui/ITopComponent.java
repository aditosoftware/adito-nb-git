package de.adito.git.gui;

import javax.swing.*;

/**
 * Interface to provide functionality of giving an overlying framework
 * control over the TopComponent
 *
 * @author A.Arnold 09.10.2018
 */
public interface ITopComponent {

    /**
     * @param jComponent the JComponent to hand over
     */
    void setComponent(JComponent jComponent);

}
