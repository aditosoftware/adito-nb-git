package de.adito.git.gui.dialogs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.dialogs.panels.PanelModule;

/**
 * A uice module to define bindings for the dialog package
 *
 * @author m.kaspera 26.10.2018
 */
public class DialogModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    install(new PanelModule());
    install(new FactoryModuleBuilder().build(CommitDetailsPanel.ICommitDetailsPanelFactory.class));
    install(new FactoryModuleBuilder().build(IDialogFactory.class));
    bind(IDialogProvider.class).to(DialogProviderImpl.class);
    bind(IDialogDisplayer.class).to(DialogDisplayerImpl.class);
  }

}
