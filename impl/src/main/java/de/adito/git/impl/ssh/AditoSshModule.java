package de.adito.git.impl.ssh;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * guice module that defines the bindings for the ssh package
 *
 * @author m.kaspera, 21.12.2018
 */
public class AditoSshModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(new FactoryModuleBuilder().build(ISshFactory.class));
    bind(ISshProvider.class).to(SshProviderImpl.class);
  }
}
