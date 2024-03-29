package de.adito.git.gui.swing;

import lombok.NonNull;

import javax.swing.*;
import java.util.function.Supplier;

/**
 * Creates a button with two icon states. Which state is shown depends on the result of the supplier, a click on the button triggers the passed action
 *
 * @author m.kaspera, 12.07.2019
 */
public class MutableIconActionButton
{

  private final JButton button;

  public MutableIconActionButton(@NonNull Action pAction, @NonNull Supplier<Boolean> pBooleanSupplier, @NonNull ImageIcon pIcon, @NonNull ImageIcon pAlternativeIcon)
  {
    button = new JButton(pBooleanSupplier.get() ? pIcon : pAlternativeIcon);
    button.addActionListener(e -> {
      pAction.actionPerformed(null);
      button.setIcon(pBooleanSupplier.get() ? pIcon : pAlternativeIcon);
    });
  }

  public JButton getButton()
  {
    return button;
  }
}
