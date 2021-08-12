package de.adito.git.gui.window.content;

import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 10.08.2021
 */
public interface ILookupComponent<T>
{

  @NotNull
  JComponent getComponent();

  @NotNull
  Observable<Optional<List<T>>> observeSelectedItems();

}
