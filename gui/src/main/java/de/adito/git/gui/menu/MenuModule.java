package de.adito.git.gui.menu;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author m.kaspera, 07.03.2019
 */
public class MenuModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(new FactoryModuleBuilder().build(IMenuFactory.class));
    bind(IMenuProvider.class).to(MenuProvider.class);
  }

}
