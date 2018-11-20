package de.adito.git.nbm.util;

import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.Set;

/**
 * @author a.arnold, 05.11.2018
 */
public class EditorObservable extends AbstractListenerObservable<PropertyChangeListener, TopComponent.Registry, Optional<TopComponent>> {

    public static Observable<Optional<TopComponent>> create() {
        return Observable.create(new EditorObservable())
                .startWith(Optional.ofNullable(getCurrentEditor()));
    }

    private EditorObservable() {
        super(TopComponent.getRegistry());
    }

    @NotNull
    @Override
    protected PropertyChangeListener registerListener(@NotNull TopComponent.Registry pRegistry, @NotNull IFireable<Optional<TopComponent>> pFireable) {
        PropertyChangeListener listener = evt -> pFireable.fireValueChanged(Optional.ofNullable(getCurrentEditor()));
        pRegistry.addPropertyChangeListener(listener);
        return listener;
    }

    @Override
    protected void removeListener(@NotNull TopComponent.Registry pRegistry, @NotNull PropertyChangeListener pPropertyChangeListener) {
        pRegistry.removePropertyChangeListener(pPropertyChangeListener);
    }

    private static TopComponent getCurrentEditor() {
        Set<? extends Mode> modes = WindowManager.getDefault().getModes();
        for (Mode mode : modes) {
            if ("editor".equals(mode.getName())) {
                return mode.getSelectedTopComponent();
            }
        }
        return null;
    }
}
