package de.adito.git.gui.actions;

import de.adito.git.gui.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.util.Optional;

/**
 * Super-class for all actions that can have several rows selected
 *
 * @author m.kaspera 04.10.2018
 */
abstract class AbstractTableAction extends AbstractAction implements IDiscardable {

    private final Disposable disposable;

    /**
     *
     * @param name the title of the action (is displayed in a menu)
     * @param isEnabledObservable an observable that indicates the current state of the action (dis/enabled)
     */
    AbstractTableAction(String name, Observable<Optional<Boolean>> isEnabledObservable) {
        super(name);
        disposable = isEnabledObservable.subscribe(isEnabled -> setEnabled(isEnabled.orElse(false)));
    }

    @Override
    public void discard() {
        disposable.dispose();
    }
}
