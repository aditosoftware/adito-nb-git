package de.adito.git.gui.icon;

import javax.swing.*;
import java.io.File;

/**
 * @author m.kaspera 25.10.2018
 */
public interface IIconLoader {

    ImageIcon getIcon(File iconFile);

}
