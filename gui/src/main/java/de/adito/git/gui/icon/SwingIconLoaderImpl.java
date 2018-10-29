package de.adito.git.gui.icon;

import javax.swing.*;

/**
 * @author m.kaspera 25.10.2018
 */
public class SwingIconLoaderImpl implements IIconLoader {

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageIcon getIcon(String iconPath) {
        return new ImageIcon(getClass().getResource(iconPath));
    }
}
