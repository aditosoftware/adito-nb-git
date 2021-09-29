package de.adito.git.gui.tree.renderer;

import de.adito.git.gui.concurrency.GitProcessExecutors;
import de.adito.git.gui.icon.MissingIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Image;
import java.util.function.Supplier;

/**
 * Label that shows a default icon while the real icon is computed in the background. The default icon is replaced by the "real" icon once the computation is complete
 *
 * @author m.kaspera, 29.09.2021
 */
public class AsyncIconLabel extends JLabel
{

  public AsyncIconLabel(@Nullable Icon pDefaultImage, @NotNull Supplier<Image> pIconSupplier)
  {
    super(pDefaultImage == null ? MissingIcon.get16x16() : pDefaultImage, SwingConstants.CENTER);
    GitProcessExecutors.submit(() -> {
      Image icon = pIconSupplier.get();
      setIcon(new ImageIcon(icon));
      repaint();
    });
  }

  public AsyncIconLabel(@NotNull Supplier<Image> pIconSupplier)
  {
    this(null, pIconSupplier);
  }
}
