package de.adito.git.gui.icon;

import javax.swing.*;

/**
 * @author m.kaspera 25.10.2018
 */
public interface IIconLoader {

    /**
     *
     * @param iconPath Path to the file that contains the icon as string. Do not use \\ as separator
     * @return ImageIcon for the passed file
     */
    ImageIcon getIcon(String iconPath);

}
