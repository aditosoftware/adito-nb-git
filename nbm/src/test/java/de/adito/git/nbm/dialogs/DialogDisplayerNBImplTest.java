package de.adito.git.nbm.dialogs;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Rectangle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 03.08.2022
 */
class DialogDisplayerNBImplTest
{

  public static Stream<Arguments> rectangleTestValues()
  {
    return Stream.of(Arguments.of(0, 0, 0, 0),
                     // potential hazard: negative values
                     Arguments.of(0, -1, 0, 0),
                     // normal rectangle
                     Arguments.of(10, 20, 30, 40),
                     // potential hazard: max values
                     Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
                     // potential hazard: min values
                     Arguments.of(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                     // potential hazard: min values/negative values as size
                     Arguments.of(0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE));
  }

  public static Stream<Arguments> parseRectangleTestValues()
  {
    return Stream.of(
        // empty string -> no rect
        Arguments.of("", null),
        // a word as input -> no rect
        Arguments.of("test", null),
        // , instead of ; as separator -> no rect
        Arguments.of("1;1;2,1", null),
        // not enough numbers for a rect -> no rect
        Arguments.of("1;1;2", null),
        // wrong separator again -> no rect
        Arguments.of("1.1.2.1", null),
        // a word nested between separators -> no rect
        Arguments.of("0;test;0;1", null),
        // spaces are removed before conversion -> all good
        Arguments.of("0;0; 0;0", new Rectangle(0, 0, 0, 0)),
        // normal rectangle -> all good
        Arguments.of("0;0;0;0", new Rectangle(0, 0, 0, 0)),
        // includes additional spaces, which are ignored -> all good
        Arguments.of("0; 0; 0; 0", new Rectangle(0, 0, 0, 0)),
        // normal rect with zero height -> all good
        Arguments.of("20;20;20;0", new Rectangle(20, 20, 20, 0)),
        // normal rect -> all good
        Arguments.of("20;20;20;100", new Rectangle(20, 20, 20, 100))
    );
  }

  /**
   * Tests if DialogDisplayerNBImpl.rectangleToString and DialogDisplayerNBImpl.parseRectangle work together as expected by testing if converting to a string and back
   * to a rectangle yields the same rectangle as was put in
   *
   * @param pX      x value of the rectangle
   * @param pY      y value of the rectangle
   * @param pWidth  width of the rectangle
   * @param pHeight height of the rectangle
   */
  @ParameterizedTest
  @MethodSource("rectangleTestValues")
  void convertRevertRectangleToStringTest(int pX, int pY, int pWidth, int pHeight)
  {
    Rectangle rectangle = new Rectangle(pX, pY, pWidth, pHeight);
    String rectangleString = DialogDisplayerNBImpl.rectangleToString(rectangle);
    assertEquals(rectangle, DialogDisplayerNBImpl.parseRectangle(rectangleString));
  }

  /**
   * Tests if string representations are parsed into rectangles in the expected way
   *
   * @param pValue    String to parse
   * @param pExpected expected rectangle
   */
  @ParameterizedTest
  @MethodSource("parseRectangleTestValues")
  void parseRectangleTest(@NonNull String pValue, @Nullable Rectangle pExpected)
  {
    assertEquals(pExpected, DialogDisplayerNBImpl.parseRectangle(pValue));
  }
}