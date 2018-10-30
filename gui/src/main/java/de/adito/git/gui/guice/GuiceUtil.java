package de.adito.git.gui.guice;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.Elements;

import java.util.stream.Collectors;

/**
 * @author m.kaspera 30.10.2018
 */
public class GuiceUtil {

    public static Module filterModule(Module toFilter, Key<?> toRemove) {
        return Elements.getModule(Elements.getElements(toFilter).stream().filter(element -> {
            if (element instanceof Binding)
                return !((Binding) element).getKey().equals(toRemove);
            else
                return true;
        }).collect(Collectors.toList()));
    }

}
