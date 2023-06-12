package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import lombok.NonNull;

import javax.swing.*;
import java.awt.event.AdjustmentListener;

/**
 * Observable that fires each time the maximum size of the ScrollBar changes
 *
 * @author m.kaspera, 20.08.2019
 */
public class ScrollBarExtentObservable extends AbstractListenerObservable<AdjustmentListener, JScrollPane, Integer>
{
  private int cachedMaxValue = 0;

  public ScrollBarExtentObservable(JScrollPane pTarget)
  {
    super(pTarget);
  }

  @NonNull
  @Override
  protected AdjustmentListener registerListener(@NonNull JScrollPane pTarget, @NonNull AbstractListenerObservable.IFireable<Integer> pFireable)
  {
    AdjustmentListener adjustmentListener = e -> {
      if (cachedMaxValue != pTarget.getVerticalScrollBar().getMaximum())
      {
        cachedMaxValue = pTarget.getVerticalScrollBar().getMaximum();
        pFireable.fireValueChanged(pTarget.getVerticalScrollBar().getMaximum());
      }
    };
    pTarget.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
    return adjustmentListener;
  }

  @Override
  protected void removeListener(@NonNull JScrollPane pListenableValue, @NonNull AdjustmentListener pAdjustmentListener)
  {
    pListenableValue.getVerticalScrollBar().removeAdjustmentListener(pAdjustmentListener);
  }
}
