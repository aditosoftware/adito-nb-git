package de.adito.git.gui.guice;

import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.spi.Elements;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for all things guice/all juicy things
 *
 * @author m.kaspera 30.10.2018
 */
public class GuiceUtil
{

  private GuiceUtil()
  {
  }

  /**
   * @param pToFilter Module whose bindings should be filtered
   * @param pToRemove the binding that should be removed/filtered from the module
   * @return a new Module all the bindings/factories/stuff from the passed Module, minus the filtered bindings
   */
  @NonNull
  public static Module filterModule(@NonNull Module pToFilter, @NonNull Key<?> pToRemove)
  {
    return Elements.getModule(Elements.getElements(pToFilter).stream().filter(element -> {
      if (element instanceof Binding)
        return !((Binding) element).getKey().equals(pToRemove);
      else
        return true;
    }).collect(Collectors.toList()));
  }

  /**
   * @param pToFilter Module whose bindings should be filtered
   * @param pToRemove the bindings that should be removed/filtered from the module
   * @return a new Module all the bindings/factories/stuff from the passed Module, minus the filtered bindings
   */
  @NonNull
  private static Module filterModule(@NonNull Module pToFilter, @NonNull List<Key<?>> pToRemove)
  {
    return Elements.getModule(Elements.getElements(pToFilter).stream().filter(element -> {
      if (element instanceof Binding)
        return pToRemove.stream().noneMatch(key -> key.equals(((Binding) element).getKey()));
      else
        return true;
    }).collect(Collectors.toList()));
  }

  /**
   * @param pToFilter Module whose bindings should be filtered
   * @param pToRemove the bindings that should be removed/filtered from the module
   * @return a new Module all the bindings/factories/stuff from the passed Module, minus the filtered bindings
   */
  @NonNull
  public static Module filterModule(@NonNull Module pToFilter, @NonNull Key<?>... pToRemove)
  {
    return filterModule(pToFilter, Arrays.asList(pToRemove));
  }

}
