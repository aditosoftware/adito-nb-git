package de.adito.git.gui.icon;

import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.HashMap;

/**
 * @author m.kaspera 25.10.2018
 */
public class SwingIconLoaderImpl extends AbstractIconLoader
{
    private static HashMap<String, ImageIcon> iconCache = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable ImageIcon getIcon(@NotNull String pIconBase)
    {
        if (iconCache.containsKey(pIconBase)) {
            return iconCache.get(pIconBase);
        } else {
            ImageIcon icon = new ImageIcon(getClass().getResource(getIconResourceForTheme(pIconBase)));
            iconCache.put(pIconBase, icon);
            return icon;
        }
    }
}