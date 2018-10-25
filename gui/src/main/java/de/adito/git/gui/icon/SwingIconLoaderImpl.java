package de.adito.git.gui.icon;

import javax.swing.*;
import java.io.File;

/**
 * @author m.kaspera 25.10.2018
 */
public class SwingIconLoaderImpl implements IIconLoader {
    @Override
    public ImageIcon getIcon(File iconFile) {
        return new ImageIcon(getClass().getResource(iconFile.getPath()));
    }
}
