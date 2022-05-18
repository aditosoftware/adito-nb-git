package de.adito.git.nbm.dialogs;

import com.google.inject.AbstractModule;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import de.adito.git.gui.dialogs.LookupProvider;

/**
 * A guice module to define bindings for the dialogs package
 *
 * @author a.arnold, 31.10.2018
 */
public class NBDialogsModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    bind(IDialogDisplayer.class).to(DialogDisplayerNBImpl.class);
    bind(LookupProvider.class).to(LookupProviderNbImpl.class);
  }
}
