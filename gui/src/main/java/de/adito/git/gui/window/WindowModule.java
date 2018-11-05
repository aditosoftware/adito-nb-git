package de.adito.git.gui.window;

import com.google.inject.AbstractModule;
import de.adito.git.gui.window.content.WindowContentModule;

/**
 * A Guice module to define bindings for the window package
 *
 * @author m.kaspera 29.10.2018
 */
public class WindowModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new WindowContentModule());
        bind(IWindowProvider.class).to(WindowProviderImpl.class);
    }

}
