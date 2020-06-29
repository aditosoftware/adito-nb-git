package de.adito.git.gui.actions;

import de.adito.git.api.IDiscardable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import javax.swing.*;
import java.util.Optional;

/**
 * Super-class for all actions that can have several rows selected
 *
 * @author m.kaspera 04.10.2018
 */
abstract class AbstractTableAction extends AbstractAction implements IDiscardable
{

  private final Disposable disposable;

  /**
   * @param pName the title of the action (is displayed in a menu)
   */
  public AbstractTableAction(String pName)
  {
    this(pName, Observable.just(Optional.of(true)));
  }

  /**
   * @param pName                the title of the action (is displayed in a menu)
   * @param pIsEnabledObservable an observable that indicates the current state of the action (dis/enabled)
   */
  AbstractTableAction(String pName, Observable<Optional<Boolean>> pIsEnabledObservable)
  {
    super(pName);
    disposable = pIsEnabledObservable.subscribe(isEnabled -> setEnabled(isEnabled.orElse(false)));
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
