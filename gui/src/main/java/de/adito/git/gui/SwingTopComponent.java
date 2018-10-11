package de.adito.git.gui;

import javax.swing.*;

/**
 * The TopComponent to show the panels
 * @author A.Arnold 11.10.2018
 */
public class SwingTopComponent implements ITopComponent {
    @Override
    public void setComponent(JComponent jComponent) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(jComponent);
        frame.pack();
        frame.setVisible(true);
    }
}
