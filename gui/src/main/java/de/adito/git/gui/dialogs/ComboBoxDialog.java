package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

/**
 * Dialog with some text and a comboBox for selection for the user
 *
 * @author m.kaspera, 02.07.2019
 */
class ComboBoxDialog<T> extends AditoBaseDialog<T>
{

  private final JComboBox<T> selectionBox;

  @Inject
  ComboBoxDialog(@Assisted String pMessage, @Assisted List<T> pOptions)
  {
    selectionBox = new JComboBox<>(new Vector<>(pOptions));
    _initGui(pMessage);
  }

  private void _initGui(String pMessage)
  {
    double fill = TableLayout.FILL;
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, pref, gap, fill, gap};
    double[] rows = {gap,
                     pref,
                     gap,
                     pref,
                     gap};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    tlu.add(1, 1, 3, 1, new JLabel(pMessage));
    tlu.add(1, 3, 3, 3, selectionBox);
  }

  @Override
  public @Nullable String getMessage()
  {
    return null;
  }

  @Nullable
  @Override
  @SuppressWarnings("unchecked")
  public T getInformation()
  {
    return (T) selectionBox.getSelectedItem();
  }
}
