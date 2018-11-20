package de.adito.git.gui.popup;

import de.adito.git.gui.popup.PopupWindow;

import javax.swing.*;
import java.awt.*;

/**
 * @author a.arnold, 12.11.2018
 */
public class test {

    public static void main(String[] args) {
        JWindow jWindow = new JWindow();
        JPanel jPanel = new JPanel();

        jPanel.setBackground(Color.green);

        PopupWindow popupWindow = new PopupWindow(jWindow, "Branches", _createList());

        JFrame frame = new JFrame();
        JButton button = new JButton();
        button.addActionListener(e -> popupWindow.setVisible(true));
        frame.add(button);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);
    }

    private static JList<String> _createList() {

        String[] data = {"test", "test", "test", "test", "test"};
        JList<String> list = new JList<>(data);
        return list;
    }
}
