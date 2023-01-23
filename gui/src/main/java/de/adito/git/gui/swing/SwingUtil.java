package de.adito.git.gui.swing;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Contains utility methods that either invoke Swing methods or have something to do with Swing
 *
 * @author m.kaspera, 04.06.2020
 */
public class SwingUtil
{

  /**
   * Executes the given Runnable right now if the current Thread is already the EDT, else call invokeLater with the Runnable as argument
   *
   * @param pRunnable Runnbale to execute in the EDT as soon as possible
   */
  public static void invokeInEDT(Runnable pRunnable)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      pRunnable.run();
    }
    else
    {
      SwingUtilities.invokeLater(pRunnable);
    }
  }

  /**
   * This method checks if the current thread is the EDT. If that is the case, the method just returns the result of the supplier execution.
   * If the current thread is not the EDT, the supplier is executed synchronously in the EDT, and any occurring exceptions are thrown afterwards
   *
   * @param pSupplier Supplier that will be exectured in the EDT
   * @throws InterruptedException      if we're interrupted while waiting for the event dispatching thread to finish executing
   * @throws InvocationTargetException if an exception is thrown while running doRun in the EDT
   */
  public static <T> T invokeSynchronouslyASAP(ExceptionalSupplier<T> pSupplier) throws Exception
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      return pSupplier.get();
    }
    else
    {
      AtomicReference<T> returnRef = new AtomicReference<>();
      AtomicReference<Exception> throwableAtomicReference = new AtomicReference<>(null);
      SwingUtilities.invokeAndWait(() -> {
        try
        {
          returnRef.set(pSupplier.get());
        }
        catch (Exception pE)
        {
          throwableAtomicReference.set(pE);
        }
      });
      if (throwableAtomicReference.get() != null)
      {
        throw throwableAtomicReference.get();
      }

      return returnRef.get();
    }
  }

  /**
   * Checks if all the specified area by the rectangle is visible in the available monitor areas
   *
   * @param pRectangle the rectangle area to check
   * @return true if the monitor bounds cover the whole area of the given rectangle
   */
  public static boolean isCompletelyVisible(@NotNull Rectangle pRectangle)
  {
    return isCompletelyVisible(pRectangle, getSortedMonitorBounds());
  }

  /**
   * Checks if all the specified area by the rectangle is contained in the sum of the given window bounds
   *
   * @param pRectangle the rectangle area to check
   * @return true if the monitor bounds cover the whole area of the given rectangle
   */
  public static boolean isCompletelyVisible(@NotNull Rectangle pRectangle, @NotNull List<Rectangle> pSortedBounds)
  {
    Rectangle previousRect = null;
    for (Rectangle windowBound : pSortedBounds)
    {
      if (previousRect != null && !isAdjacentRectangles(previousRect, windowBound))
      {
        return false;
      }
      previousRect = windowBound;
    }

    if (pSortedBounds.isEmpty())
      return false;
    // transform the given rectangles into a polygon
    int[] xValues = new int[pSortedBounds.size() * 4 + 1];
    int[] yValues = new int[pSortedBounds.size() * 4 + 1];
    int counter = 0;
    // go through the rectangles and add all the upper corners
    for (Rectangle rectangle : pSortedBounds)
    {
      xValues[counter] = rectangle.x;
      yValues[counter] = rectangle.y;
      counter++;
      xValues[counter] = rectangle.x + rectangle.width;
      yValues[counter] = rectangle.y;
      counter++;
    }
    // go through the rectangles in reverse and add all the lower corners
    for (int i = pSortedBounds.size() - 1; i >= 0; i--)
    {
      Rectangle rectangle = pSortedBounds.get(i);
      xValues[counter] = rectangle.x + rectangle.width;
      yValues[counter] = rectangle.y + rectangle.height;
      counter++;
      xValues[counter] = rectangle.x;
      yValues[counter] = rectangle.y + rectangle.height;
      counter++;
    }
    // re-add the first point, to connect the last point to the first one and close the shape/polygon
    xValues[counter] = pSortedBounds.get(0).x;
    yValues[counter] = pSortedBounds.get(0).y;
    return new Polygon(xValues, yValues, xValues.length).contains(pRectangle);
  }

  /**
   * @return List of Rectangles representing the bounds of the different monitors available to this application, sorted from leftmost to rightmost monitor
   */
  @NotNull
  public static List<Rectangle> getSortedMonitorBounds()
  {
    GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    return Arrays.stream(screenDevices)
        .map(GraphicsDevice::getDefaultConfiguration)
        .map(GraphicsConfiguration::getBounds)
        .sorted(Comparator.comparingInt(pO -> pO.x))
        .collect(Collectors.toList());
  }

  /**
   * Check if the given two rectangles are adjacent. Because their orientation is given (left and right), only the two sides that should overlap when the two rectangles
   * are adjacent are compared.
   * Two rectangles that overlap in the x-coordinate space are not considered adjacent
   *
   * @param pLeftRectangle  coordinates of the left rectangle
   * @param pRightRectangle coordinates of the right rectangle
   * @return true if the right edge of the left Rectangle and the left edge of the right rectangle overlap
   */
  public static boolean isAdjacentRectangles(@NotNull Rectangle pLeftRectangle, @NotNull Rectangle pRightRectangle)
  {
    Line2D.Double leftRectRim = new Line2D.Double(new Point2D.Double(pLeftRectangle.x + pLeftRectangle.width, pLeftRectangle.y),
                                                  new Point2D.Double(pLeftRectangle.x + pLeftRectangle.width, pLeftRectangle.y + pLeftRectangle.height));
    Line2D.Double rightRectRim = new Line2D.Double(new Point2D.Double(pRightRectangle.x, pRightRectangle.y),
                                                   new Point2D.Double(pRightRectangle.x, pRightRectangle.y + pRightRectangle.height));
    return leftRectRim.intersectsLine(rightRectRim);
  }

  /**
   * Supplier that works with Exceptions
   *
   * @param <T> Type returned by the Supplier
   */
  @FunctionalInterface
  public interface ExceptionalSupplier<T>
  {
    T get() throws Exception;
  }


}
