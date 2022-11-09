package de.adito.git.impl.data.diff;

import org.eclipse.jgit.diff.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link LineIndexDiffUtil}.
 *
 * @author r.hartinger
 */
class LineIndexDiffUtilTest
{

  private static @NotNull Stream<Arguments> getTextOffsetsDeletedText()
  {
    return Stream.of(
        // deleted a line
        Arguments.of(new ChangeDeltaTextOffsets(12, 19, 12, 12), new Edit(1, 2, 1, 1), "first line.\nfifth line.\n"),

        // added content to the line
        Arguments.of(new ChangeDeltaTextOffsets(12, 19, 12, 25), new Edit(1, 2, 1, 2), "first line.\nsecond line.\nfifth line.\n"),

        // added multiple line content
        Arguments.of(new ChangeDeltaTextOffsets(12, 19, 12, 49), new Edit(1, 2, 1, 4), "first line.\nsecond line.\nthird line.\nforth line.\nfifth line.\n"),

        // replaced content of a line
        Arguments.of(new ChangeDeltaTextOffsets(12, 19, 12, 19), new Edit(1, 2, 1, 2), "first line.\n000000\nfifth line.\n"),

        // added a new line
        Arguments.of(new ChangeDeltaTextOffsets(19, 30, 19, 20), new Edit(2, 2, 2, 3), "first line.\nsecne.\n\nfifth line.\n")
    );
  }

  /**
   * Tests the method call with a simple original content and one element in the edit list. The change factory will return the passed ChangeDeltaTextOffsets.
   *
   * @param pExpected       the expected value that is returned by the method. The method would return a list, but this list will in this case contain only one element, so this is the single element of the list
   * @param pEdit           the content of the edit list. The edit list will only contain one element
   * @param pChangedContent the changed content that will be passed to the method
   * @see LineIndexDiffUtil#getTextOffsets(String, String, EditList, IChangeDeltaFactory)
   */
  @ParameterizedTest
  @MethodSource
  void getTextOffsetsDeletedText(@NotNull ChangeDeltaTextOffsets pExpected, @NotNull Edit pEdit, @NotNull String pChangedContent)
  {
    EditList pEditList = EditList.singleton(pEdit);

    IChangeDeltaFactory<ChangeDeltaTextOffsets> pDeltaChangeDeltaFactory = (pUnusedEdit, pDeltaTextOffsets) -> pDeltaTextOffsets;

    String originalContent = "first line.\nsecne.\nfifth line.";

    assertEquals(List.of(pExpected), LineIndexDiffUtil.getTextOffsets(originalContent, pChangedContent, pEditList, pDeltaChangeDeltaFactory));
  }

  /**
   * Tests the method call with two entries in the edit list. The change factory will return the passed ChangeDeltaTextOffsets.
   *
   * @see LineIndexDiffUtil#getTextOffsets(String, String, EditList, IChangeDeltaFactory)
   */
  @Test
  void getTextOffsetsDeletedTextMultiEdits()
  {
    EditList pEditList = new EditList();
    pEditList.add(new Edit(0, 1, 0, 1));
    pEditList.add(new Edit(2, 3, 2, 3));

    IChangeDeltaFactory<ChangeDeltaTextOffsets> pDeltaChangeDeltaFactory = (pUnusedEdit, pDeltaTextOffsets) -> pDeltaTextOffsets;

    String originalContent = "first line.\nsecne.\nfifth line.";
    String changedContent = "first line!\nsecne.\nfifth line!";

    assertEquals(List.of(new ChangeDeltaTextOffsets(0, 12, 0, 12),
                         new ChangeDeltaTextOffsets(19, 30, 19, 30)),
                 LineIndexDiffUtil.getTextOffsets(originalContent, changedContent, pEditList, pDeltaChangeDeltaFactory));
  }


  private static @NotNull Stream<Arguments> testGetStartIndexSafely()
  {
    return Stream.of(
        // getting the first start index
        Arguments.of(1, 0),
        // getting the second start index
        Arguments.of(3, 1),
        // getting the third start index
        Arguments.of(5, 2),
        // out of the size of the list. Get the last end index + 1
        Arguments.of(7, 3)
    );
  }


  /**
   * Tests with a simple line info. There are three elements in this line info and the indexes are incrementing by 1 each.
   *
   * @param pExpected the expected result the method should return
   * @param pIndex    the given index to the method
   * @see LineIndexDiffUtil#getStartIndexSafely(List, int)
   */
  @ParameterizedTest
  @MethodSource
  void testGetStartIndexSafely(int pExpected, int pIndex)
  {
    List<LineInfo> pLineInfos = List.of(new LineInfo(1, 2), new LineInfo(3, 4), new LineInfo(5, 6));

    assertEquals(pExpected, LineIndexDiffUtil.getStartIndexSafely(pLineInfos, pIndex));
  }

  /**
   * Tests the method call with a single line string.
   *
   * @see LineIndexDiffUtil#getLineInfos(String)
   */
  @Test
  void getLineInfosSimpleText()
  {
    assertEquals(List.of(new LineInfo(0, 5)), LineIndexDiffUtil.getLineInfos("lorem"));
  }

  /**
   * Tests the method call with a multi line string.
   *
   * @see LineIndexDiffUtil#getLineInfos(String)
   */
  @Test
  void getLineInfosMultiLineText()
  {
    String pText = "lorem\nipsum\ndolor\namar\nsit";

    List<LineInfo> expected = List.of(new LineInfo(0, 6), new LineInfo(6, 12), new LineInfo(12, 18), new LineInfo(18, 23), new LineInfo(23, 26));

    assertEquals(expected, LineIndexDiffUtil.getLineInfos(pText));
  }

}