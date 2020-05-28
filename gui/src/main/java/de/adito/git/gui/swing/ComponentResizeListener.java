package de.adito.git.gui.swing;

import de.adito.git.api.prefs.IPrefStore;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * ComponentListener that listens only for resize events. The new size is then stored in a prefStore
 * Also offers a method to get the preferred size from the prefStore
 *
 * @author m.kaspera, 27.05.2020
 */
public class ComponentResizeListener extends ComponentAdapter
{

  private final IPrefStore prefStore;
  private final String prefStoreKey;

  public ComponentResizeListener(@NotNull IPrefStore pPrefStore, @NotNull String pPrefStoreKey)
  {
    prefStore = pPrefStore;
    prefStoreKey = pPrefStoreKey;
  }

  @Override
  public void componentResized(ComponentEvent e)
  {
    prefStore.put(prefStoreKey, e.getComponent().getSize().width + "x" + e.getComponent().getSize().height);
  }

  /**
   * @param pPrefStore    PrefStore to retrieve the stored preferred size
   * @param pPrefStoreKey Key used to store/retrieve the preferred size
   * @param pDefaultSize  default size that is used if no preferred size is stored in the prefStore
   * @return Size stored in the PrefStore, or a default value
   */
  @NotNull
  public static Dimension _getPreferredSize(@NotNull IPrefStore pPrefStore, @NotNull String pPrefStoreKey, @NotNull Dimension pDefaultSize)
  {
    String sizeKey = pPrefStore.get(pPrefStoreKey);
    return sizeKey == null ? pDefaultSize : new Dimension(Integer.parseInt(sizeKey.split("x")[0]), Integer.parseInt(sizeKey.split("x")[1]));
  }
}
