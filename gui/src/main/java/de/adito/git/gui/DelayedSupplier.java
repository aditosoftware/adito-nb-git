package de.adito.git.gui;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Supplier that works like a lazy, non-threadsafe AtomicReference, i.e. the underlying supplier can be changed by calling the set method while the actual value
 * is only computed at the time that get is called instead of when the set method is called like in an AtomicReference
 *
 * @author m.kaspera, 17.05.2022
 */
public class DelayedSupplier<T> implements Supplier<T>
{

  private Supplier<T> innerSupplier = null;

  public void setInnerSupplier(@NonNull Supplier<T> pInnerSupplier)
  {
    innerSupplier = pInnerSupplier;
  }

  /**
   * calls the underlying supplier and retrieves its value. null if no supplier has been set, or the supplier returns null
   *
   * @return the value of the underlying supplier, possibly null if no supplier is set or the supplier returns null
   */
  @Override
  @Nullable
  public T get()
  {
    return Optional.ofNullable(innerSupplier).map(Supplier::get).orElse(null);
  }
}
