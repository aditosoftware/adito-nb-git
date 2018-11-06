package de.adito.git.gui.actions;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.gui.window.IWindowProvider;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author m.kaspera 06.11.2018
 */
public class ShowStatusWindowAction extends AbstractAction {

    private final IWindowProvider windowProvider;
    private final Observable<IRepository> repository;

    @Inject
    ShowStatusWindowAction(IWindowProvider pWindowProvider, @Assisted Observable<IRepository> pRepository) {
        putValue(Action.NAME, "Show Status Window");
        putValue(Action.SHORT_DESCRIPTION, "Shows all changed files of the working copy with the type of change");
        windowProvider = pWindowProvider;
        repository = pRepository;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        windowProvider.showStatusWindow(repository);
    }
}
