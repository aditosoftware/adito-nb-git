package de.adito.git.gui.guice;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.Elements;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for all things Guice/all juicy things
 *
 * @author m.kaspera 30.10.2018
 */
public class GuiceUtil {

    /**
     * @param toFilter Module whose bindings should be filtered
     * @param toRemove the binding that should be removed/filtered from the module
     * @return a new Module all the bindings/factories/stuff from the passed Module, minus the filtered bindings
     */
    @NotNull
    public static Module filterModule(@NotNull Module toFilter, @NotNull Key<?> toRemove) {
        return Elements.getModule(Elements.getElements(toFilter).stream().filter(element -> {
            if (element instanceof Binding)
                return !((Binding) element).getKey().equals(toRemove);
            else
                return true;
        }).collect(Collectors.toList()));
    }

    /**
     *
     * @param toFilter Module whose bindings should be filtered
     * @param toRemove the bindings that should be removed/filtered from the module
     * @return a new Module all the bindings/factories/stuff from the passed Module, minus the filtered bindings
     */
    @NotNull
    public static Module filterModule(@NotNull Module toFilter, @NotNull List<Key<?>> toRemove) {
        return Elements.getModule(Elements.getElements(toFilter).stream().filter(element -> {
            if (element instanceof Binding)
                return toRemove.stream().noneMatch(key -> key.equals(((Binding) element).getKey()));
            else
                return true;
        }).collect(Collectors.toList()));
    }

}
