package de.adito.git.gui.swing;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangesEvent;
import de.adito.git.impl.observables.PropertyChangeObservable;
import de.adito.git.impl.util.BiNavigateAbleMap;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 25.07.2019
 */
public class SynchronizedBoundedRangeModel extends DefaultBoundedRangeModel implements IDiscardable
{

  // this is the height in where the changes should meet up, in parts of the visible window (0 means meet up at the very top, 1 at the very bottom)
  private static final double SYNCHRONIZE_ON_HEIGHT = 0.5;
  private final Disposable throttleDisposable;
  private final List<_CoupledScrollbarInfo> coupledScrollbarInfos = new ArrayList<>();

  // variables used for implementing the "throttling", aka limiting the amount of scroll events per second. If scroll events happen too fast, they are
  // aggregated and released after a set amount of time. The resulting effect should be the same scroll speed with fewer events
  private final BehaviorSubject<Integer> throttler = BehaviorSubject.create();
  private final AtomicInteger throttleCounter = new AtomicInteger(0);
  private HashMap<BoundedRangeModel, List<_CoupledScrollbarInfo>> subListCache = new HashMap<>();

  public SynchronizedBoundedRangeModel(@NotNull JScrollBar pCoupledBar, @NotNull Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> pRefreshMappings,
                                       @NotNull Observable<Optional<IFileChangesEvent>> pFileChangesEventObs, boolean pUseInverseMap)
  {
    coupledScrollbarInfos.add(new _CoupledScrollbarInfo(pCoupledBar, pRefreshMappings, pFileChangesEventObs, pUseInverseMap));
    throttleDisposable = throttler.throttleLast(16, TimeUnit.MILLISECONDS).subscribe(pScrollAmount -> {
      throttleCounter.set(0);
      _valueChanged(getValue() + pScrollAmount, coupledScrollbarInfos);
    });
  }

  @Override
  public void setValue(int n)
  {
    throttleCounter.addAndGet(n - getValue());
    throttler.onNext(throttleCounter.get());
  }

  /**
   * @param pToCouple            ScrollBar that should be tied to this model/vice versa
   * @param pRefreshMappings     Function that is called to refresh the height mappings
   * @param pFileChangesEventObs Observable that fires on FileChangeEvents to the Editor
   * @param pUseInverseMap       whether or not the inverse mapping should be used to determine the height mappings
   */
  public void addCoupledScrollbar(@NotNull JScrollBar pToCouple, @NotNull Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> pRefreshMappings,
                                  @NotNull Observable<Optional<IFileChangesEvent>> pFileChangesEventObs, boolean pUseInverseMap)
  {
    _CoupledScrollbarInfo newInfo = new _CoupledScrollbarInfo(pToCouple, pRefreshMappings, pFileChangesEventObs, pUseInverseMap);
    coupledScrollbarInfos.add(newInfo);
  }

  /**
   * set the value without going through the evaluation of scrolling the other scrollPane up/down, basically only notifying the listeners
   *
   * @param n new value for the model
   */
  private boolean setValueWithoutFeedback(int n, BoundedRangeModel pSetter)
  {
    if (coupledScrollbarInfos.size() > 1)
    {
      return _valueChanged(n, subListCache
          .computeIfAbsent(pSetter, pKey -> coupledScrollbarInfos
              .stream()
              .filter(pCoupledScrollbarInfo -> !pCoupledScrollbarInfo.getToCouple().getModel().equals(pKey))
              .collect(Collectors.toList())));
    }
    else
    {
      SwingUtilities.invokeLater(() -> super.setValue(n));
      return true;
    }
  }

  /**
   * @param pNewValue       New Value of the scrollPane
   * @param pScrollbarInfos list of coupled Scrollbars with their informations
   */
  private boolean _valueChanged(int pNewValue, @NotNull List<_CoupledScrollbarInfo> pScrollbarInfos)
  {
    int scrollAmount = pNewValue - getValue();
    if (scrollAmount == 0)
      return false;
    double changedMidVisibleValue = getValue() + scrollAmount + getExtent() * SYNCHRONIZE_ON_HEIGHT;
    boolean isScrollPane = true;
    for (_CoupledScrollbarInfo scrollbarInfo : pScrollbarInfos)
    {
      if (!scrollbarInfo.getMap().isEmpty())
      {
        double remainedMidVisibleValue = scrollbarInfo.getToCouple().getValue() + scrollbarInfo.getToCouple().getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT;
        Map.Entry<Integer, Integer> closestEntry = scrollbarInfo.getMap().floorEntry((int) changedMidVisibleValue);
        if (closestEntry != null)
        {
          double desiredValue = changedMidVisibleValue + (closestEntry.getValue() - closestEntry.getKey());
          if (scrollAmount > 0)
          {
            isScrollPane = isScrollPane && _scrolledDown(scrollAmount, remainedMidVisibleValue, desiredValue, scrollbarInfo);
          }
          else
          {
            isScrollPane = isScrollPane && _scrolledUp(scrollAmount, remainedMidVisibleValue, desiredValue, scrollbarInfo);
          }
        }
      }
    }
    if (isScrollPane)
      SwingUtilities.invokeLater(() -> super.setValue(getValue() + scrollAmount));
    return isScrollPane;
  }

  /**
   * @param pScrollAmount            how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pRemainedMidVisibleValue the y value for the middle of the visible window of the pRemainedScrollbar
   * @param pDesiredValue            the value that pRemainedMidVisibleValue should have
   * @param pScrollbarInfo           object that holds information about the scrollPane that this Model is coupled to
   * @return true if this model should be moved the indicated scrolled amount, false if it should stay in place
   */
  private boolean _scrolledUp(int pScrollAmount, double pRemainedMidVisibleValue, double pDesiredValue, _CoupledScrollbarInfo pScrollbarInfo)
  {
    boolean isScrollPane = true;
    // do nothing if pRemained is scrolled all the way up to the top
    if (pScrollbarInfo.getToCouple().getValue() != 0)
    {
      double distanceFromDesired = pRemainedMidVisibleValue - pDesiredValue;
      if (Math.abs(pScrollAmount) >= Math.abs(distanceFromDesired))
      {
        isScrollPane = pScrollbarInfo.setValue((int) (pDesiredValue - pScrollbarInfo.getToCouple().getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
      }
      else
      {
        // scrolled up and distanceFromDesired is bigger than scrollAmount -> other scrollPane is more than the scrollAmount further down -> scroll up other scrollPane
        // only
        if (distanceFromDesired > pScrollAmount)
        {
          pScrollbarInfo.setValue(pScrollbarInfo.getToCouple().getValue() + pScrollAmount);
          isScrollPane = false;
        }
        // case pRemained is within a margin of error to pChanged, set pRemained to the desired value
        else if (distanceFromDesired >= -pScrollAmount)
        {
          // half the visible amount is subtracted because the position set is the top of the visible area, while pDesiredValue
          // specifies the middle of the visible area
          isScrollPane = pScrollbarInfo.setValue((int) (pDesiredValue - pScrollbarInfo.getToCouple().getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
        }
      }
    }
    return isScrollPane;
  }

  /**
   * Handles the synchronization if the user scrolled down
   *
   * @param pScrollAmount            how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pRemainedMidVisibleValue the y value for the middle of the visible window of the pRemainedScrollbar
   * @param pDesiredValue            the value that pRemainedMidVisibleValue should have
   * @param pScrollbarInfo           object that holds information about the scrollPane that this Model is coupled to
   * @return true if this model should be moved the indicated scrolled amount, false if it should stay in place
   */
  private boolean _scrolledDown(int pScrollAmount, double pRemainedMidVisibleValue, double pDesiredValue, _CoupledScrollbarInfo pScrollbarInfo)
  {
    boolean isScrollPane = true;
    // do nothing if pRemained is already scrolled down to the very bottom
    if (pScrollbarInfo.getToCouple().getValue() + pScrollbarInfo.getToCouple().getVisibleAmount() != pScrollbarInfo.getToCouple().getMaximum())
    {
      double distanceFromDesired = pRemainedMidVisibleValue - pDesiredValue;
      if (Math.abs(pScrollAmount) >= Math.abs(distanceFromDesired))
      {
        isScrollPane = pScrollbarInfo.setValue((int) (pDesiredValue - pScrollbarInfo.getToCouple().getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
      }
      else
      {
        // scrolled down and distanceFromDesired is smaller than scrollAmount -> other scrollPane is more than the scrollAmount further up -> scroll down other scrollPane
        // only
        if (distanceFromDesired < -pScrollAmount)
        {
          isScrollPane = false;
          pScrollbarInfo.setValue(pScrollbarInfo.getToCouple().getValue() + pScrollAmount);
        }
        // case pRemained is within a margin of error to pChanged, set pRemained to the desired value
        else if (distanceFromDesired <= pScrollAmount)
        {
          // half the visible amount is subtracted because the position set is the top of the visible area, while pDesiredValue
          // specifies the middle of the visible area
          isScrollPane = pScrollbarInfo.setValue((int) (pDesiredValue - pScrollbarInfo.getToCouple().getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
        }
      }
    }
    return isScrollPane;
  }

  @Override
  public void discard()
  {
    throttleDisposable.dispose();
    for (_CoupledScrollbarInfo coupledScrollbarInfo : coupledScrollbarInfos)
    {
      coupledScrollbarInfo.discard();
    }
  }

  /**
   * Contains information and proxy-calls to another JScrollPane. Automatically updates the height mappings if a FileChangesEvent is fired
   */
  private class _CoupledScrollbarInfo implements IDiscardable
  {

    private final Disposable disposable;
    private final Disposable propertyChangeDisposable;
    private final JScrollBar toCouple;
    private final Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> refreshMappings;
    private final boolean useInverseMap;
    private BiNavigateAbleMap<Integer, Integer> map = new BiNavigateAbleMap<>();
    private Function<Integer, Boolean> setOtherScrollBarValueFunction;

    _CoupledScrollbarInfo(@NotNull JScrollBar pToCouple, @NotNull Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> refreshMappings,
                          @NotNull Observable<Optional<IFileChangesEvent>> pFileChangesEventObs, boolean pUseInverseMap)
    {
      toCouple = pToCouple;
      this.refreshMappings = refreshMappings;
      useInverseMap = pUseInverseMap;
      propertyChangeDisposable = Observable.create(new PropertyChangeObservable<>(pToCouple, "model")).startWith(Optional.of(pToCouple.getModel()))
          .subscribe(pObj -> setOtherScrollBarValueFunction = _getSetOtherScrollbarFunction(pObj.orElse(null), pToCouple));
      disposable = pFileChangesEventObs
          .subscribe(pFileChangesEvent -> refreshMappings(pFileChangesEvent.orElse(null)));
    }

    /**
     * @return the coupled JScrollBar
     */
    JScrollBar getToCouple()
    {
      return toCouple;
    }

    /**
     * @return whether or not the inverse map should be used
     */
    boolean isUseInverseMap()
    {
      return useInverseMap;
    }

    /**
     * @return the map with the height mappings to be used
     */
    BiNavigateAbleMap<Integer, Integer> getMap()
    {
      return isUseInverseMap() ? map.getInverse() : map;
    }

    /**
     * refreshes the heightMappings determining if the Scrollbars should move on/wait for another
     *
     * @param pFileChangesEvent the FileChangesEvent, containing information to determine the height mappings
     */
    void refreshMappings(@Nullable IFileChangesEvent pFileChangesEvent)
    {
      if (pFileChangesEvent != null)
        map = refreshMappings.apply(pFileChangesEvent);
      else
        map = new BiNavigateAbleMap<>();
    }

    boolean setValue(int pNumber)
    {
      return setOtherScrollBarValueFunction.apply(pNumber);
    }

    @Override
    public void discard()
    {
      propertyChangeDisposable.dispose();
      disposable.dispose();
    }

    private Function<Integer, Boolean> _getSetOtherScrollbarFunction(@Nullable Object pOtherScrollbar, @NotNull JScrollBar pToCouple)
    {
      if (pOtherScrollbar instanceof SynchronizedBoundedRangeModel)
        return pInteger -> ((SynchronizedBoundedRangeModel) pOtherScrollbar)
            .setValueWithoutFeedback(pInteger, SynchronizedBoundedRangeModel.this);
      else
        return pInteger -> {
          SwingUtilities.invokeLater(() -> pToCouple.setValue(pInteger));
          return true;
        };
    }
  }
}
