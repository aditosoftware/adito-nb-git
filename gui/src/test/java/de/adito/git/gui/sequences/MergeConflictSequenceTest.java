package de.adito.git.gui.sequences;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.data.diff.MergeDataImpl;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.generate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author m.kaspera, 08.05.2020
 */
public class MergeConflictSequenceTest
{

  /**
   * @return Stream with arguments used for the lineNumChanges test
   */
  public static Stream<Arguments> lineNumChangesSource()
  {
    return Stream.of(Arguments.of(List.of(), List.of(), List.of(), List.of(), 0), // emtpy lists, should be able to handle this -> result is 0
                     Arguments.of(List.of(1), List.of(1), List.of(1), List.of(1), 0),
                     Arguments.of(List.of(1), List.of(2), List.of(1), List.of(1), 1),
                     Arguments.of(List.of(2), List.of(2), List.of(2), List.of(5), 3),
                     Arguments.of(List.of(3), List.of(1), List.of(2), List.of(2), -2), // this value does not make sense in a git world, but the method should be able to handle it
                     Arguments.of(List.of(1, 5), List.of(2, 7), List.of(1, 6), List.of(1, 7), 4),
                     Arguments.of(List.of(1, 100), List.of(60, 140), List.of(1, 100), List.of(1, 100), 99)); // several changeDeltas
  }

  /**
   * @return Stream with arguments used for all tests that are connected to the isSkipMergeData method
   */
  public static Stream<Arguments> skipMergeTestSources()
  {
    return Stream.of(Arguments.of(createFileDiff(List.of(), List.of(), List.of(), List.of(), 0, 0),
                                  createFileDiff(List.of(), List.of(), List.of(), List.of(), 0, 0),
                                  false, false), // empty changeset, so no huge file and no changed lines
                     Arguments.of(createFileDiff(List.of(1), List.of(1), List.of(1), List.of(1), 2, 80001),
                                  createFileDiff(List.of(1), List.of(1), List.of(1), List.of(1), 2, 2),
                                  true, false), // changeset with a huge file, but not many changed lines
                     Arguments.of(createFileDiff(List.of(1, 100), List.of(60, 140), List.of(1, 100), List.of(1, 100), 160, 160),
                                  createFileDiff(List.of(1), List.of(1), List.of(1), List.of(1), 60, 60),
                                  false, true), // small file, but quite a few lines were changed
                     Arguments.of(createFileDiff(List.of(1), List.of(200), List.of(1), List.of(160), 500, 80001),
                                  createFileDiff(List.of(2, 600), List.of(200, 900), List.of(2, 500), List.of(1, 10), 1000, 99999),
                                  true, true)); // huge file, and lots of lines changed
  }

  @Test
  void testAdjustLineEndingsFromUnixToWindows()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.WINDOWS, ELineEnding.WINDOWS);
    String testString = "Hello there\nHow are you?\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\r\nHow are you?\r\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromUnixToMac()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.MAC, ELineEnding.MAC);
    String testString = "Hello there\nHow are you?\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\rHow are you?\r", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromWindowsToUnix()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.UNIX, ELineEnding.UNIX);
    String testString = "Hello there\r\nHow are you?\r\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\nHow are you?\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToUnix()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.UNIX, ELineEnding.UNIX);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\nHow are you?\nThere's another one\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToMac()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.MAC, ELineEnding.MAC);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\rHow are you?\rThere's another one\r", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToWindows()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.WINDOWS, ELineEnding.WINDOWS);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictSequence._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\r\nHow are you?\r\nThere's another one\r\n", adjustLineEndings);
  }

  /**
   * @param pYours               IFileDiff of the "yours" Side
   * @param pTheirs              IFileDiff of the "theirs" Side
   * @param pisHugeFile          whether the file is a huge file or not
   * @param pContainsManyChanges whether the diffs contain many changed lines or not
   */
  @ParameterizedTest
  @MethodSource("skipMergeTestSources")
  void isSkipMergeData(@NonNull IFileDiff pYours, @NonNull IFileDiff pTheirs, boolean pisHugeFile, boolean pContainsManyChanges)
  {
    assertEquals(pisHugeFile && pContainsManyChanges, MergeConflictSequence.isSkipMergeData(new MergeDataImpl(pYours, pTheirs)));
  }

  /**
   * @param pYours               IFileDiff of the "yours" Side
   * @param pTheirs              IFileDiff of the "theirs" Side
   * @param pisHugeFile          whether the file is a huge file or not
   * @param pContainsManyChanges unused for this test
   */
  @SuppressWarnings("unused") // in order to use the same test setup for all tests that
  @ParameterizedTest
  @MethodSource("skipMergeTestSources")
  void isSkipMergeDataIfEnvironmentIsSet(@NonNull IFileDiff pYours, @NonNull IFileDiff pTheirs, boolean pisHugeFile, boolean pContainsManyChanges)
  {
    System.setProperty("de.adito.git.resolve.maxlines", "0");
    assertEquals(pisHugeFile, MergeConflictSequence.isSkipMergeData(new MergeDataImpl(pYours, pTheirs)));
  }

  /**
   * @param pYours               IFileDiff of the "yours" Side
   * @param pTheirs              IFileDiff of the "theirs" Side
   * @param pisHugeFile          whether the file is a huge file or not
   * @param pContainsManyChanges unused for this test
   */
  @ParameterizedTest
  @MethodSource("skipMergeTestSources")
  void isHugeFile(@NonNull IFileDiff pYours, @NonNull IFileDiff pTheirs, boolean pisHugeFile, boolean pContainsManyChanges)
  {
    assertEquals(pisHugeFile, MergeConflictSequence.isHugeFile(pYours, pTheirs));
  }

  /**
   * @param pYours               IFileDiff of the "yours" Side
   * @param pTheirs              IFileDiff of the "theirs" Side
   * @param pisHugeFile          unused for this test
   * @param pContainsManyChanges whether the diffs contain many changed lines or not
   */
  @ParameterizedTest
  @MethodSource("skipMergeTestSources")
  void containsManyChanges(@NonNull IFileDiff pYours, @NonNull IFileDiff pTheirs, boolean pisHugeFile, boolean pContainsManyChanges)
  {
    assertEquals(pContainsManyChanges, MergeConflictSequence.containsManyChanges(pYours, pTheirs));
  }

  /**
   * @param pLineStartOld   List with values that are used as the startLine of the old side change. The size of the list determines the number of ChangeDeltas created
   * @param pLineEndOld     List with values that are used as the endLine of the old side change. The size of the list determines the number of ChangeDeltas created
   * @param pLineStartNew   List with values that are used as the startLine of the new side change. The size of the list determines the number of ChangeDeltas created
   * @param pLineEndNew     List with values that are used as the endLine of the new side change. The size of the list determines the number of ChangeDeltas created
   * @param pExpectedResult the expected number of changed lines
   */
  @ParameterizedTest
  @MethodSource("lineNumChangesSource")
  void getNumLineChanges(@NonNull List<Integer> pLineStartOld, @NonNull List<Integer> pLineEndOld,
                         @NonNull List<Integer> pLineStartNew, @NonNull List<Integer> pLineEndNew,
                         int pExpectedResult)
  {
    assertEquals(pExpectedResult, MergeConflictSequence.getNumLineChanges(createFileDiff(pLineStartOld, pLineEndOld, pLineStartNew, pLineEndNew, 0, 0)));
  }

  /**
   * Create a mocked IFileDiff that has the methods used for getting the start and endLines of its ChangeDeltas and the text of its sides mocked with passed values
   *
   * @param pLineStartOld  List with values that are used as the startLine of the old side change. The size of the list determines the number of ChangeDeltas created
   * @param pLineEndOld    List with values that are used as the endLine of the old side change. The size of the list determines the number of ChangeDeltas created
   * @param pLineStartNew  List with values that are used as the startLine of the new side change. The size of the list determines the number of ChangeDeltas created
   * @param pLineEndNew    List with values that are used as the endLine of the new side change. The size of the list determines the number of ChangeDeltas created
   * @param pTextLengthOld number of characters that the text version of the old side should have. The text is filled with spaces
   * @param pTextLengthNew number of characters that the text version of the new side should have. The text is filled with spaces
   * @return MockedIFileDiff with passed valued as mocked return values
   */
  @NonNull
  private static IFileDiff createFileDiff(@NonNull List<Integer> pLineStartOld, @NonNull List<Integer> pLineEndOld,
                                          @NonNull List<Integer> pLineStartNew, @NonNull List<Integer> pLineEndNew,
                                          int pTextLengthOld, int pTextLengthNew)
  {
    IFileDiff fileDiff = Mockito.mock(IFileDiff.class);
    List<IChangeDelta> changeDeltas = new ArrayList<>();
    for (int index = 0; index < pLineStartOld.size(); index++)
    {
      IChangeDelta changeDelta = Mockito.mock(IChangeDelta.class);
      when(changeDelta.getStartLine(EChangeSide.OLD)).thenReturn(pLineStartOld.get(index));
      when(changeDelta.getEndLine(EChangeSide.OLD)).thenReturn(pLineEndOld.get(index));
      when(changeDelta.getStartLine(EChangeSide.NEW)).thenReturn(pLineStartNew.get(index));
      when(changeDelta.getEndLine(EChangeSide.NEW)).thenReturn(pLineEndNew.get(index));
      changeDeltas.add(changeDelta);
    }
    when(fileDiff.getText(EChangeSide.OLD)).thenReturn(generate(() -> " ").limit(pTextLengthOld).collect(joining()));
    when(fileDiff.getText(EChangeSide.NEW)).thenReturn(generate(() -> " ").limit(pTextLengthNew).collect(joining()));
    when(fileDiff.getChangeDeltas()).thenReturn(changeDeltas);
    return fileDiff;
  }

  /**
   * create a IMergeData object with mocked method calls such that when the lineEndings for the two NEW sides are called, the given ELineEndings are returned
   *
   * @param pLineEndingTheirs ELineEnding for the NEW side of the THEIRS conflictSide
   * @param pLineEndingYours  ELineEnding for the NEW side of the YOURS conflictSide
   * @return mocked IMergeData object
   */
  private static IMergeData _createMergeData(ELineEnding pLineEndingTheirs, ELineEnding pLineEndingYours)
  {
    IFileContentInfo fileContentInfoTheirs = Mockito.mock(IFileContentInfo.class);
    when(fileContentInfoTheirs.getLineEnding()).thenReturn(() -> pLineEndingTheirs);
    IFileDiff fileDiffTheirs = Mockito.mock(IFileDiff.class);
    when(fileDiffTheirs.getFileContentInfo(EChangeSide.NEW)).thenReturn(fileContentInfoTheirs);
    IFileContentInfo fileContentInfoYours = Mockito.mock(IFileContentInfo.class);
    when(fileContentInfoYours.getLineEnding()).thenReturn(() -> pLineEndingYours);
    IFileDiff fileDiffYours = Mockito.mock(IFileDiff.class);
    when(fileDiffYours.getFileContentInfo(EChangeSide.NEW)).thenReturn(fileContentInfoYours);
    IMergeData mergeData = Mockito.mock(IMergeData.class);
    when(mergeData.getDiff(EConflictSide.THEIRS)).thenReturn(fileDiffTheirs);
    when(mergeData.getDiff(EConflictSide.YOURS)).thenReturn(fileDiffYours);
    return mergeData;
  }
}
