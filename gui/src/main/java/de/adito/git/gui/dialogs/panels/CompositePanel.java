package de.adito.git.gui.dialogs.panels;

import de.adito.git.gui.dialogs.AditoBaseDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Panel that lays out is components with a vertical box layout
 *
 * @author m.kaspera, 25.06.2020
 */
public class CompositePanel<T> extends AditoBaseDialog<T>
{

  private final InformationSupplier<T> infoCallable;

  /**
   * @param pComponents   List of components to include in this panel
   * @param pInfoSupplier Supplier to get the Information for the getInformation Function
   */
  public CompositePanel(List<JComponent> pComponents, InformationSupplier<T> pInfoSupplier)
  {
    this(pComponents, List.of(), pInfoSupplier);
  }

  /**
   * @param pComponents   List of components to include in this panel
   * @param pSpacings     List of spacings to use above, between and below the components. The first item in the list is the upper border, all following items
   *                      are added after another component was added
   * @param pInfoCallable Supplier to get the Information for the getInformation Function
   */
  public CompositePanel(List<JComponent> pComponents, List<Integer> pSpacings, InformationSupplier<T> pInfoCallable)
  {
    infoCallable = pInfoCallable;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    for (int index = 0; index < pComponents.size(); index++)
    {
      if (pSpacings.size() > index)
      {
        add(Box.createVerticalStrut(pSpacings.get(index)));
      }
      add(pComponents.get(index));
    }
    if (pSpacings.size() > pComponents.size())
      add(Box.createVerticalStrut(pSpacings.get(pSpacings.size() - 1)));
  }

  @Override
  public @Nullable String getMessage()
  {
    return null;
  }

  @Nullable
  @Override
  public T getInformation()
  {
    return infoCallable.get();
  }

  public interface InformationSupplier<T> extends Supplier<T>
  {
  }
}
