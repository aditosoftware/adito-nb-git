package de.adito.git.impl.data;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice Module for the data package
 *
 * @author m.kaspera, 24.12.2018
 */
public class DataModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(new FactoryModuleBuilder().build(IDataFactory.class));
  }

}
