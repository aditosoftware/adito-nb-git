package de.adito.git.impl.util;

import de.adito.git.api.IDiscardable;
import de.adito.git.api.data.IFileChangesEvent;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Couples two scrollBars in such a way that the provided heightMappings are always fulfilled in the middle of the visible screen
 *
 * @author m.kaspera, 04.03.2019
 */
public class DifferentialScrollBarCoupling implements AdjustmentListener, IDiscardable
{

  // this is the height in where the changes should meet up, in parts of the visible window (0 means meet up at the very top, 1 at the very bottom)
  private static final double SYNCHRONIZE_ON_HEIGHT = 0.5;
  private BiNavigateAbleMap<Integer, Integer> map = new BiNavigateAbleMap<>();
  private final Disposable disposable;
  private final JScrollBar scrollBar1;
  private final JScrollBar scrollBar2;
  private int scrollBar1ValueCache = 0;
  private int scrollBar2ValueCache = 0;
  private boolean isEnabled = true;

  private DifferentialScrollBarCoupling(JScrollBar pScrollBar1, JScrollBar pScrollBar2,
                                        Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> refreshMappings,
                                        Observable<Optional<IFileChangesEvent>> pIFileChangesEventObs)
  {
    scrollBar1 = pScrollBar1;
    scrollBar2 = pScrollBar2;
    disposable = pIFileChangesEventObs.subscribe(pChangesEvent -> SwingUtilities.invokeLater(() -> map = pChangesEvent
        .map(refreshMappings)
        .orElse(new BiNavigateAbleMap<>())));
    scrollBar1.addAdjustmentListener(this);
    scrollBar2.addAdjustmentListener(this);
  }

  /**
   * @param pScrollBar1           the first scrollBar
   * @param pScrollBar2           the second scrollBar
   * @param pRefreshMappings      Function that maps
   * @param pIFileChangesEventObs Observable that has the up-to-date version of the List of IFileChangeChunks
   * @return an instance of this class to discard the Observable or remove the coupling
   */
  public static DifferentialScrollBarCoupling coupleScrollBars(JScrollBar pScrollBar1, JScrollBar pScrollBar2,
                                                               Function<IFileChangesEvent, BiNavigateAbleMap<Integer, Integer>> pRefreshMappings,
                                                               Observable<Optional<IFileChangesEvent>> pIFileChangesEventObs)
  {
    return new DifferentialScrollBarCoupling(pScrollBar1, pScrollBar2, pRefreshMappings, pIFileChangesEventObs);
  }

  public void removeCoupling()
  {
    scrollBar1.removeAdjustmentListener(this);
    scrollBar2.removeAdjustmentListener(this);
  }


  @Override
  public void discard()
  {
    removeCoupling();
    disposable.dispose();
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent pEvent)
  {
    if (isEnabled && map.size() > 1)
    {
      isEnabled = false;
      if (System.identityHashCode(pEvent.getSource()) == System.identityHashCode(scrollBar1))
      {
        _valueChanged(scrollBar1, scrollBar2, pEvent.getValue() - scrollBar1ValueCache, map);
      }
      else
      {
        _valueChanged(scrollBar2, scrollBar1, pEvent.getValue() - scrollBar2ValueCache, map.getInverse());
      }
      scrollBar1ValueCache = scrollBar1.getValue();
      scrollBar2ValueCache = scrollBar2.getValue();
      isEnabled = true;
    }
  }

  /**
   * @param pChanged      The JScrollbar whose value changed
   * @param pRemained     The coupled JScrollbar
   * @param pScrollAmount how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pMap          the mapping of the heights that should be matched, has to be the BiNavigateAbleMap that maps from pChanged to pRemained
   */
  private void _valueChanged(JScrollBar pChanged, JScrollBar pRemained, int pScrollAmount, BiNavigateAbleMap<Integer, Integer> pMap)
  {
    if (pScrollAmount == 0)
      return;
    double changedMidVisibleValue = pChanged.getValue() + pChanged.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT;
    double remainedMidVisibleValue = pRemained.getValue() + pRemained.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT;
    Map.Entry<Integer, Integer> closestEntry = pMap.floorEntry((int) changedMidVisibleValue);
    if (closestEntry != null)
    {
      double desiredValue = changedMidVisibleValue + (closestEntry.getValue() - closestEntry.getKey());
      if (pScrollAmount > 0)
      {
        _scrolledDown(pChanged, pRemained, pScrollAmount, remainedMidVisibleValue, desiredValue);
      }
      else
      {
        _scrolledUp(pChanged, pRemained, pScrollAmount, remainedMidVisibleValue, desiredValue);
      }
    }
  }

  /**
   * @param pChanged                 The JScrollbar whose value changed
   * @param pRemained                The coupled JScrollbar
   * @param pScrollAmount            how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pRemainedMidVisibleValue the y value for the middle of the visible window of the pRemainedScrollbar
   * @param pDesiredValue            the value that pRemainedMidVisibleValue should have
   */
  private void _scrolledUp(JScrollBar pChanged, JScrollBar pRemained, int pScrollAmount, double pRemainedMidVisibleValue, double pDesiredValue)
  {
    // do nothing if pRemained is scrolled all the way up to the top
    if (pRemained.getValue() != 0)
    {
      if (Math.abs(pScrollAmount) > pChanged.getUnitIncrement())
      {
        pRemained.setValue((int) (pDesiredValue - pRemained.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
      }
      else
      {
        // case pRemained has to catch up, so scroll pRemained up and set pChanged back to the original position
        if (pRemainedMidVisibleValue - pDesiredValue > pRemained.getUnitIncrement())
        {
          pRemained.setValue(pRemained.getValue() + pScrollAmount);
          pChanged.setValue(pChanged.getValue() - pScrollAmount);
        }
        // case pRemained is within a margin of error to pChanged, set pRemained to the desired value
        else if (pRemainedMidVisibleValue - pDesiredValue >= -pRemained.getUnitIncrement())
        {
          // half the visible amount is subtracted because the position set is the top of the visible area, while pDesiredValue
          // specifies the middle of the visible area
          pRemained.setValue((int) (pDesiredValue - pRemained.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
        }
      }
    }
  }

  /**
   * Handles the synchronization if the user scrolled down
   *
   * @param pChanged                 The JScrollbar whose value changed
   * @param pRemained                The coupled JScrollbar
   * @param pScrollAmount            how far the user scrolled down (usually the value of the UnitIncrement)
   * @param pRemainedMidVisibleValue the y value for the middle of the visible window of the pRemainedScrollbar
   * @param pDesiredValue            the value that pRemainedMidVisibleValue should have
   */
  private void _scrolledDown(JScrollBar pChanged, JScrollBar pRemained, int pScrollAmount, double pRemainedMidVisibleValue, double pDesiredValue)
  {
    // do nothing if pRemained is already scrolled down to the very bottom
    if (pRemained.getValue() + pRemained.getVisibleAmount() != pRemained.getMaximum())
    {
      if (Math.abs(pScrollAmount) > pChanged.getUnitIncrement())
      {
        pRemained.setValue((int) (pDesiredValue - pRemained.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
      }
      else
      {
        // case pRemained has to catch up, so scroll pRemained down and set pChanged back to the original position
        if (pRemainedMidVisibleValue - pDesiredValue < -pRemained.getUnitIncrement())
        {
          pRemained.setValue(pRemained.getValue() + pScrollAmount);
          pChanged.setValue(pChanged.getValue() - pScrollAmount);
        }
        // case pRemained is within a margin of error to pChanged, set pRemained to the desired value
        else if (pRemainedMidVisibleValue - pDesiredValue <= pRemained.getUnitIncrement())
        {
          // half the visible amount is subtracted because the position set is the top of the visible area, while pDesiredValue
          // specifies the middle of the visible area
          pRemained.setValue((int) (pDesiredValue - pRemained.getVisibleAmount() * SYNCHRONIZE_ON_HEIGHT));
        }
      }
    }
  }
}
