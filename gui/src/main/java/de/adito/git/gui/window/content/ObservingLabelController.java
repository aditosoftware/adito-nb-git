package de.adito.git.gui.window.content;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.Optional;

/**
 * Contains logic that changes the text of a label if an observable changes
 *
 * @author m.kaspera, 03.01.2020
 */
public abstract class ObservingLabelController<TYPE> implements Disposable
{

  protected CompositeDisposable disposable = new CompositeDisposable();
  protected final JLabel label;
  protected static final MouseListener voidListener = new MouseAdapter()
  {
  };

  protected ObservingLabelController(@NotNull String pLabelText, @NotNull Observable<Optional<TYPE>> pObservable)
  {
    label = new JLabel(pLabelText);
    disposable.add(pObservable.subscribe(pOpt -> pOpt.ifPresentOrElse(this::updateLabel, () -> updateLabel(null))));
  }

  /**
   * @return the label that is updated in sync with the observable
   */
  public JLabel getLabel()
  {
    return label;
  }

  @Override
  public void dispose()
  {
    disposable.dispose();
  }

  @Override
  public boolean isDisposed()
  {
    return disposable.isDisposed();
  }

  /**
   * value of the observable changed, update the text
   *
   * @param pNewValue value fired by the observable
   */
  protected abstract void updateLabel(@Nullable TYPE pNewValue);

}
