package de.adito.git.gui.dialogs.panels;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author m.kaspera, 18.06.2020
 */
public class PanelModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(new FactoryModuleBuilder().build(PanelFactoryImpl.IGuicePanelFactory.class));
    bind(IPanelFactory.class).to(PanelFactoryImpl.class);
  }
}
