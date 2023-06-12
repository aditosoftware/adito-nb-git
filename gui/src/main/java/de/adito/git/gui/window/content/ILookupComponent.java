package de.adito.git.gui.window.content;

import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

/**
 * @author m.kaspera, 10.08.2021
 */
public interface ILookupComponent<T>
{

  @NonNull
  JComponent getComponent();

  @NonNull
  Observable<Optional<List<T>>> observeSelectedItems();

}
