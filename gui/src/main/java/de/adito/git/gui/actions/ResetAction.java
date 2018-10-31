package de.adito.git.gui.actions;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.dialogs.IDialogProvider;
import io.reactivex.Observable;

import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author m.kaspera 31.10.2018
 */
class ResetAction extends AbstractTableAction {

    ResetAction(IDialogProvider pDialogProvider, @Assisted Observable<IRepository> pRepository, @Assisted Observable<List<IFileChangeType>> pSelectedFilesObservable) {
        super("Reset");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    protected boolean isEnabled0() {
        return false;
    }
}
