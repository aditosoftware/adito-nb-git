package de.adito.git.gui.swing;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.impl.observables.PropertyChangeObservable;
import de.adito.git.impl.util.BiNavigateAbleMap;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author m.kaspera, 25.07.2019
 */
public class SynchronizedBoundedRangeModel extends DefaultBoundedRangeModel implements IDiscardable
{

  // this is the height in where the changes should meet up, in parts of the visible window (0 means meet up at the very top, 1 at the very bottom)
  private static final double SYNCHRONIZE_ON_HEIGHT = 0.5;
  private final JScrollBar coupledBar;
  private BiNavigateAbleMap<Integer, Integer> map = new BiNavigateAbleMap<>();
  private Consumer<Integer> setOtherScrollBarValueFunction;
  private final Disposable disposable;
  private final Disposable propertyChangeDisposable;
  private final Disposable throttleDisposable;
  private final BehaviorSubject<Integer> throttler = BehaviorSubject.create();
  private final AtomicInteger throttleCounter = new AtomicInteger(0);

  public SynchronizedBoundedRangeModel(JScrollBar pCoupledBar, Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> refreshMappings,
                                       Observable<Optional<IFileChangesEvent>> pIFileChangesEventObs, boolean pUseInverseModel)
  {
    coupledBar = pCoupledBar;
    disposable = pIFileChangesEventObs.subscribe(pChangesEvent -> SwingUtilities.invokeLater(() -> map = pChangesEvent
        .map(refreshMappings)
        .orElse(new BiNavigateAbleMap<>())));
    propertyChangeDisposable = Observable.create(new PropertyChangeObservable(pCoupledBar, "model")).subscribe(pObj -> {
      if (pObj.isPresent() && pObj.get() instanceof SynchronizedBoundedRangeModel)
        setOtherScrollBarValueFunction = pInteger -> SwingUtilities.invokeLater(() -> ((SynchronizedBoundedRangeModel) pObj.get()).setValueWithoutFeedback(pInteger));
      else
        setOtherScrollBarValueFunction = pInteger -> SwingUtilities.invokeLater(() -> pCoupledBar.setValue(pInteger));
    });
    throttleDisposable = throttler.throttleLast(16, TimeUnit.MILLISECONDS).subscribe(pScrollAmount -> {
      throttleCounter.set(0);
      _valueChanged(getValue() + pScrollAmount, pUseInverseModel ? map.getInverse() : map);
    });
  }

  @Override
  public void setValue(int n)
  {
    throttleCounter.addAndGet(n - getValue());
    throttler.onNext(throttleCounter.get());
  }

  /**
   * set the value without going through the evaluation of scrolling the other scrollPane up/down, basically only notifying the listeners
   *
   * @param n new value for the model
   */
  private void setValueWithoutFeedback(int n)
  {
    super.setValue(n);
  }

  /**
   * @param pNewValue New Value of the scrollPane
   * @param pMap      the mapping of the heights that should be matched, has to be the BiNavigateAbleMap that maps from pChanged to pRemained
   */
  private void _valueChanged(int pNewValue, BiNavigateAbleMap<Integer, Integer> pMap)
  {
    int scrollAmount = pNewValue - getValue();
    if (scrollAmount == 0)
      return;
    if (!map.isEmpty())
    {
      double changedMidVisibleValue = getValue() + scrollAmount + getExtent() * SYNCHRONIZE_ON_HEIGHT;
      double remainedMidVisibleValue = coupledBar.getValue() + coupledBar.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT;
      Map.Entry<Integer, Integer> closestEntry = pMap.floorEntry((int) changedMidVisibleValue);
      if (closestEntry != null)
      {
        double desiredValue = changedMidVisibleValue + (closestEntry.getValue() - closestEntry.getKey());
        if (scrollAmount > 0)
        {
          _scrolledDown(scrollAmount, remainedMidVisibleValue, desiredValue);
        }
        else
        {
          _scrolledUp(scrollAmount, remainedMidVisibleValue, desiredValue);
        }
      }
    }
    else
    {
      SwingUtilities.invokeLater(() -> super.setValue(getValue() + scrollAmount));
    }
  }

  /**
   * @param pScrollAmount            how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pRemainedMidVisibleValue the y value for the middle of the visible window of the pRemainedScrollbar
   * @param pDesiredValue            the value that pRemainedMidVisibleValue should have
   */
  private void _scrolledUp(int pScrollAmount, double pRemainedMidVisibleValue, double pDesiredValue)
  {
    // do nothing if pRemained is scrolled all the way up to the top
    if (coupledBar.getValue() != 0)
    {
      double distanceFromDesired = pRemainedMidVisibleValue - pDesiredValue;
      if (Math.abs(pScrollAmount) >= Math.abs(distanceFromDesired))
      {
        SwingUtilities.invokeLater(() -> super.setValue(getValue() + pScrollAmount));
        setOtherScrollBarValueFunction.accept((int) (pDesiredValue - coupledBar.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
      }
      else
      {
        // scrolled up and distanceFromDesired is bigger than scrollAmount -> other scrollPane is more than the scrollAmount further down -> scroll up other scrollPane
        // only
        if (distanceFromDesired > pScrollAmount)
        {
          setOtherScrollBarValueFunction.accept(coupledBar.getValue() + pScrollAmount);
        }
        // case pRemained is within a margin of error to pChanged, set pRemained to the desired value
        else if (distanceFromDesired >= -pScrollAmount)
        {
          SwingUtilities.invokeLater(() -> super.setValue(getValue() + pScrollAmount));
          // half the visible amount is subtracted because the position set is the top of the visible area, while pDesiredValue
          // specifies the middle of the visible area
          setOtherScrollBarValueFunction.accept((int) (pDesiredValue - coupledBar.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
        }
        else
        {
          SwingUtilities.invokeLater(() -> super.setValue(getValue() + pScrollAmount));
        }
      }
    }
  }

  /**
   * Handles the synchronization if the user scrolled down
   *
   * @param pScrollAmount            how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pRemainedMidVisibleValue the y value for the middle of the visible window of the pRemainedScrollbar
   * @param pDesiredValue            the value that pRemainedMidVisibleValue should have
   */
  private void _scrolledDown(int pScrollAmount, double pRemainedMidVisibleValue, double pDesiredValue)
  {
    // do nothing if pRemained is already scrolled down to the very bottom
    if (coupledBar.getValue() + coupledBar.getVisibleAmount() != coupledBar.getMaximum())
    {
      double distanceFromDesired = pRemainedMidVisibleValue - pDesiredValue;
      if (Math.abs(pScrollAmount) >= Math.abs(distanceFromDesired))
      {
        SwingUtilities.invokeLater(() -> super.setValue(getValue() + pScrollAmount));
        setOtherScrollBarValueFunction.accept((int) (pDesiredValue - coupledBar.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
      }
      else
      {
        // scrolled down and distanceFromDesired is smaller than scrollAmount -> other scrollPane is more than the scrollAmount further up -> scroll down other scrollPane
        // only
        if (distanceFromDesired < -pScrollAmount)
        {
          setOtherScrollBarValueFunction.accept(coupledBar.getValue() + pScrollAmount);
        }
        // case pRemained is within a margin of error to pChanged, set pRemained to the desired value
        else if (distanceFromDesired <= pScrollAmount)
        {
          SwingUtilities.invokeLater(() -> super.setValue(getValue() + pScrollAmount));
          // half the visible amount is subtracted because the position set is the top of the visible area, while pDesiredValue
          // specifies the middle of the visible area
          setOtherScrollBarValueFunction.accept((int) (pDesiredValue - coupledBar.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
        }
        else
        {
          SwingUtilities.invokeLater(() -> super.setValue(getValue() + pScrollAmount));
        }
      }
    }
  }

  /**
   * Runs each <code>ChangeListener</code>'s <code>stateChanged</code> method.
   *
   * @see #setRangeProperties
   * @see EventListenerList
   */
  @Override
  protected void fireStateChanged()
  {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2)
    {
      if (listeners[i] == ChangeListener.class)
      {
        if (changeEvent == null)
        {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    propertyChangeDisposable.dispose();
    throttleDisposable.dispose();
  }
}
