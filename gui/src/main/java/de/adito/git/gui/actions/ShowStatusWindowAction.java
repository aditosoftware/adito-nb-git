package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author m.kaspera 06.11.2018
 */
class ShowStatusWindowAction extends AbstractAction
{

  private final IWindowProvider windowProvider;
  private final Observable<Optional<IRepository>> repository;
  private final IAsyncProgressFacade progressFacade;

  @Inject
  ShowStatusWindowAction(IWindowProvider pWindowProvider, IAsyncProgressFacade pProgressFacade,
                         @Assisted Observable<Optional<IRepository>> pRepository)
  {
    progressFacade = pProgressFacade;
    putValue(Action.NAME, "Show Status Window");
    putValue(Action.SHORT_DESCRIPTION, "Show all local changes");
    windowProvider = pWindowProvider;
    repository = pRepository;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    progressFacade.executeInBackground("Preparing Overview", pHandle -> {
      windowProvider.showStatusWindow(repository); //shows in EDT
    });
  }

}
