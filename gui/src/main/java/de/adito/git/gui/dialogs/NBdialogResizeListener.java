package de.adito.git.gui.dialogs;

import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.swing.ComponentResizeListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Sets the size of the netbeans dialog presenter to a stored size, and adds a listener that stores the new size if the window is resized
 *
 * @author m.kaspera, 28.05.2020
 */
class NBdialogResizeListener implements PropertyChangeListener
{

  private final JComponent component;
  private final IPrefStore prefStore;
  private final String prefStoreKey;
  private boolean listenerAdded = false;

  public NBdialogResizeListener(JComponent pComponent, IPrefStore pPrefStore, String pPrefStoreKey)
  {
    component = pComponent;
    prefStore = pPrefStore;
    prefStoreKey = pPrefStoreKey;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if ("ancestor".equals(evt.getPropertyName()) && !listenerAdded)
    {
      // the way from the rootPane down is a faster and more secure way, since it fixed for all dialogs. The way up can depend on the layout of the specific dialog
      JRootPane rootPane = component.getRootPane();
      if (rootPane == null || _doesNotContainComponentOfType(rootPane, JLayeredPane.class))
        return;
      JLayeredPane layeredPane = (JLayeredPane) _getComponentOfType(rootPane, JLayeredPane.class);
      if (layeredPane == null || _doesNotContainComponentOfType(layeredPane, JPanel.class))
        return;
      JPanel panel = (JPanel) _getComponentOfType(layeredPane, JPanel.class);
      // the presenter itself is of an anonymous class, so can't use instanceOf or isAssignableFrom here
      if (panel != null && "org.netbeans.core.windows.services.NbPresenter$1".equals(panel.getComponents()[0].getClass().getName()))
      {
        panel.getComponents()[0].setPreferredSize(ComponentResizeListener._getPreferredSize(prefStore, prefStoreKey, new Dimension(1600, 900)));
        panel.getComponents()[0].addComponentListener(new ComponentResizeListener(prefStore, prefStoreKey));
        listenerAdded = true;
      }
    }
  }

  /**
   * @param pComponent     Component to search
   * @param pSearchedClass Class to search in the components of pComponent
   * @return true if one of the components of pComponent is of type pSearchedClass, false otherwise
   */
  private static boolean _doesNotContainComponentOfType(@NotNull JComponent pComponent, @NotNull Class<?> pSearchedClass)
  {
    for (Component subComponent : pComponent.getComponents())
    {
      if (subComponent.getClass() == pSearchedClass)
        return false;
    }
    return true;
  }

  /**
   * @param pComponent     Component to search
   * @param pSearchedClass Class to search in the components of pComponent
   * @return the component of the components contained in pComponent that is of type pSearchedClass, null if none are of that class
   */
  @Nullable
  private static Component _getComponentOfType(@NotNull JComponent pComponent, @NotNull Class<?> pSearchedClass)
  {
    for (Component subComponent : pComponent.getComponents())
    {
      if (subComponent.getClass() == pSearchedClass)
        return subComponent;
    }
    return null;
  }
}
