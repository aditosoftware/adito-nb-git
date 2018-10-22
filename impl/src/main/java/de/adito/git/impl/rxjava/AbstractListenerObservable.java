package de.adito.git.impl.rxjava;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Abstrake Implementierung eines Observables für RxJava.
 * Diese Abstraktion bildet die Brücke zwischen normalen Listenern und dem reaktiven Paradigma.
 *
 * @author w.glanzer, 23.04.2018
 */
public abstract class AbstractListenerObservable<LISTENER, MODEL, VALUE> implements ObservableOnSubscribe<VALUE> {
    private final Set<_Disposable> disposableRefs = new HashSet<>();
    private MODEL listenableValue;

    public AbstractListenerObservable(@NotNull MODEL pListenableValue) {
        listenableValue = pListenableValue;
    }

    @Override
    public final void subscribe(ObservableEmitter<VALUE> emitter) {
        LISTENER listener = registerListener(listenableValue, emitter::onNext);
        _Disposable disposable = new _Disposable(listener);
        emitter.setDisposable(disposable);
        disposableRefs.add(disposable);
    }

    /**
     * Erzeugt einen neuen Listener und fügt diesen dem VALUE hinzu.
     * Wird der VALUE geändert / springt der hinzugefügte Listener an, so ist pOnNext aufzurufen
     *
     * @param pListenableValue Value, auf den gehört wird
     * @param pOnNext          onNext-Funktion des Emitters, um einen neuen Value zu feuern
     * @return der Strong Listener, nicht <tt>null</tt>
     */
    @NotNull
    protected abstract LISTENER registerListener(@NotNull MODEL pListenableValue, @NotNull Consumer<VALUE> pOnNext);

    /**
     * Entfernt den Listener, der bei registerListener hinzugefügt wurde, von VALUE
     *
     * @param pListenableValue Listenable Value, auf dem der Listener sitzt
     * @param pLISTENER        Listener, der entfernt werden soll
     */
    protected abstract void removeListener(@NotNull MODEL pListenableValue, @NotNull LISTENER pLISTENER);

    /**
     * Disposable-Impl, die den Listener hält
     */
    private class _Disposable implements Disposable {
        private LISTENER listener;

        public _Disposable(LISTENER pListener) {
            listener = pListener;
        }

        @Override
        public void dispose() {
            if (listener != null) {
                removeListener(listenableValue, listener);
                listener = null;

                synchronized (disposableRefs) {
                    disposableRefs.remove(this);
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return listener == null;
        }
    }

}
