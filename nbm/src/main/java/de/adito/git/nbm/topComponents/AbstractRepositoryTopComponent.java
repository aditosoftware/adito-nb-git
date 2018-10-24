package de.adito.git.nbm.topComponents;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.openide.windows.TopComponent;

import javax.swing.*;

/**
 * @author a.arnold, 24.10.2018
 */
abstract class AbstractRepositoryTopComponent extends TopComponent {
    private final Disposable displayNameDisposable;


    AbstractRepositoryTopComponent(Observable<IRepository> pRepository) {
        //Set the displayname in the TopComponent of NetBeans.
        displayNameDisposable = pRepository.subscribe(pRepo -> SwingUtilities.invokeLater(() -> {
            setDisplayName(getTopComponentName() + " - " + pRepo.getDirectory()); //todo I18N
        }));
    }

    protected abstract String getTopComponentName();

    @Override
    protected void componentClosed() {
        if (!displayNameDisposable.isDisposed())
            displayNameDisposable.dispose();
    }
}
