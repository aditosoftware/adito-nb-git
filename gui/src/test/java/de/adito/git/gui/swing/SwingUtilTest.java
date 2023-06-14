package de.adito.git.gui.swing;

import lombok.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Rectangle;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 03.08.2022
 */
class SwingUtilTest
{

  public static Stream<Arguments> visibilityTestValues()
  {
    return Stream.of(
        // 1x1 rectangle inside the window area
        Arguments.of(new Rectangle(1, 1, 1, 1), List.of(new Rectangle(0, 0, 1920, 1080)), true),
        // size of the rectangle is 0, contains returns false -> result here is false
        Arguments.of(new Rectangle(1, 1, 0, 0), List.of(new Rectangle(0, 0, 1920, 1080)), false),
        // 10x10 rectangle starting at the 10, 10 point, but because there is no window area defined the result here is false
        Arguments.of(new Rectangle(10, 10, 10, 10), List.of(), false),
        // both the rectangle and the window are offset to negative coordinates, the rectangles is inside the defined window area however
        Arguments.of(new Rectangle(-10, -10, 10, 10), List.of(new Rectangle(-20, -20, 1920, 1080)), true),
        // a rectangle of negative size is not considered to be part of another rectangle, even if its starting corner is well inside another rectangle -> false
        Arguments.of(new Rectangle(100, 100, -20, -20), List.of(new Rectangle(0, 0, 1920, 1080)), false),
        // 1x1 rectangle placed on the origin, the origin however is not part of the window areas -> false
        Arguments.of(new Rectangle(0, 0, 1, 1), List.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 1440), new Rectangle(4480, 0, 2560, 1440)), false),
        // 1x1 rectangle placed on the upper left of the leftmost window -> is contained
        Arguments.of(new Rectangle(0, 360, 1, 1), List.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 1440), new Rectangle(4480, 0, 2560, 1440)), true),
        // rectangle that starts outside the window area, but intersects it -> result is still false, because not the whole rectangle is inside the window area
        Arguments.of(new Rectangle(-10, 20, 100, 100), List.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 1440), new Rectangle(4480, 0, 2560, 1440)), false),
        // rectangle starts in one window and ends in another, all of its area is enclosed by the total defined window bounds however
        Arguments.of(new Rectangle(500, 500, 2000, 100), List.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 1440), new Rectangle(4480, 0, 2560, 1440)), true),
        // rectangle starts in the first window, runs the entire length of the second window and ends in the third. However, all of its area is once more enclosed by the total defined window bounds
        Arguments.of(new Rectangle(500, 500, 4500, 100), List.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 1440), new Rectangle(4480, 0, 2560, 1440)), true),
        // the first and second window rectangles are not adjacent -> false
        Arguments.of(new Rectangle(500, 500, 4500, 100), List.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1921, 0, 2560, 1440), new Rectangle(4481, 0, 2560, 1440)), false));
  }

  public static Stream<Arguments> adjacentRectanglesTestValues()
  {
    return Stream.of(
        // adjacent rectangles
        Arguments.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 1440), true),
        // there is a space of 1 pixel between those two rectangles
        Arguments.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1921, 0, 2560, 1440), false),
        // these two rectangles overlap -> false
        Arguments.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1919, 0, 2560, 1440), false),
        // this is the same point -> lines "overlap"
        Arguments.of(new Rectangle(0, 0, 0, 0), new Rectangle(0, 0, 0, 0), true),
        // rectangles would in theory intersect in the x-coordinate space, the left rectangle is below the right rectangle however
        Arguments.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 100), false),
        // the corner of these two rectangles touches -> considered adjacent
        Arguments.of(new Rectangle(0, 360, 1920, 1080), new Rectangle(1920, 0, 2560, 360), true)
    );
  }

  /**
   * Tests if the rectangle is considered visible in the correct cases
   *
   * @param pRectangle       Rectangle to check for visibility
   * @param pWindowBounds    Bounds for the windows
   * @param pExcpectedResult true if the rectangle is expected to be considered visible, false otherwise
   */
  @ParameterizedTest
  @MethodSource("visibilityTestValues")
  void isCompletelyVisible(@NonNull Rectangle pRectangle, @NonNull List<Rectangle> pWindowBounds, boolean pExcpectedResult)
  {
    assertEquals(pExcpectedResult, SwingUtil.isCompletelyVisible(pRectangle, pWindowBounds));
  }

  @ParameterizedTest
  @MethodSource("adjacentRectanglesTestValues")
  void isAdjacentRectanglesTest(@NonNull Rectangle pLeftRectangle, @NonNull Rectangle pRightRectangle, boolean pExpectedResult)
  {
    assertEquals(pExpectedResult, SwingUtil.isAdjacentRectangles(pLeftRectangle, pRightRectangle));
  }
}