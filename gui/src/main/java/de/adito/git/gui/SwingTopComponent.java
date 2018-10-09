package de.adito.git.gui;

import javax.swing.*;
import java.awt.*;

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
