package de.adito.git.api;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.*;

/**
 * Queue-backed list of colors that delivers colors/takes them back. Operates on FIFO principle
 *
 * @author m.kaspera 20.11.2018
 */
public class ColorRoulette
{

  private final List<Color> initialColors = Arrays.asList(
      new Color(101, 179, 46),
      new Color(241, 200, 23),
      new Color(229, 28, 40),
      new Color(45, 53, 140),
      new Color(18, 146, 69),
      new Color(4, 112, 184),
      new Color(243, 146, 32),
      new Color(153, 133, 117),
      new Color(158, 19, 95),
      new Color(250, 250, 60),
      new Color(49, 174, 114),
      new Color(117, 77, 36),
      new Color(30, 171, 227),
      new Color(235, 91, 38),
      new Color(13, 104, 56),
      new Color(141, 129, 188),
      new Color(39, 40, 91),
      new Color(215, 219, 45),
      new Color(193, 39, 47),
      new Color(231, 230, 230)
  );
  private final LinkedList<Color> availableColors = new LinkedList<>();

  ColorRoulette()
  {
    availableColors.addAll(initialColors);
  }

  /**
   * @return the next Color in the queue or null if an error occurs/the queue is empty
   */
  @NotNull
  public Color get()
  {
    try
    {
      return availableColors.remove();
    }
    catch (NoSuchElementException e)
    {
      return Color.red;
    }
  }

  /**
   * @param pColor the Color that should be returned to the queue
   */
  void returnColor(@NotNull Color pColor)
  {
    if (!availableColors.contains(pColor))
    {
      availableColors.add(pColor);
    }
  }

  /**
   * resets the contents of the Queue back to its original state
   */
  public void reset()
  {
    availableColors.clear();
    availableColors.addAll(initialColors);
  }

}
