package de.adito.git.impl.util;

import de.adito.git.api.IDiscardable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Map;

/**
 * Couples two scrollBars in such a way that the provided heightMappings are always fulfilled in the middle of the visible screen
 *
 * @author m.kaspera, 04.03.2019
 */
public class DifferentialScrollBarCoupling implements AdjustmentListener, IDiscardable
{

  private final BiNavigateAbleMap<Integer, Integer> map = new BiNavigateAbleMap<>();
  private final Disposable disposable;
  private final JScrollBar scrollBar1;
  private final JScrollBar scrollBar2;
  private int scrollBar1ValueCache = 0;
  private int scrollBar2ValueCache = 0;
  private boolean isEnabled = true;

  private DifferentialScrollBarCoupling(JScrollBar pScrollBar1, JScrollBar pScrollBar2,
                                        Observable<BiNavigateAbleMap<Integer, Integer>> heightMappings)
  {
    scrollBar1 = pScrollBar1;
    scrollBar2 = pScrollBar2;
    disposable = heightMappings.subscribe(pMappings -> {
      // Todo: proper synchronization here
      isEnabled = false;
      map.clear();
      map.putAll(pMappings);
      isEnabled = true;
    });
    scrollBar1.addAdjustmentListener(this);
    scrollBar2.addAdjustmentListener(this);
  }

  /**
   * @param pScrollBar1     the first scrollBar
   * @param pScrollBar2     the second scrollBar
   * @param pHeightMappings the different heights that should be equal in the middle fo the visible screen
   * @return an instance of this class to discard the Observable or remove the coupling
   */
  public static DifferentialScrollBarCoupling coupleScrollBars(JScrollBar pScrollBar1, JScrollBar pScrollBar2,
                                                               Observable<BiNavigateAbleMap<Integer, Integer>> pHeightMappings)
  {
    return new DifferentialScrollBarCoupling(pScrollBar1, pScrollBar2, pHeightMappings);
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
    if (isEnabled)
    {
      isEnabled = false;
      if (pEvent.getSource().equals(scrollBar1))
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
    double changedMidVisibleValue = pChanged.getValue() + pChanged.getVisibleAmount() / (double) 2;
    double remainedMidVisibleValue = pRemained.getValue() + pRemained.getVisibleAmount() / (double) 2;
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
        pRemained.setValue((int) (pDesiredValue - pRemained.getVisibleAmount() / (double) 2));
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
        pRemained.setValue((int) (pDesiredValue - pRemained.getVisibleAmount() / (double) 2));
      }
    }
  }
}
