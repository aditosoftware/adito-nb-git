package de.adito.git.gui.dialogs;

import de.adito.git.api.data.diff.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author m.kaspera, 08.05.2020
 */
public class MergeConflictDialogTest
{

  @Test
  void testAdjustLineEndingsFromUnixToWindows()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.WINDOWS, ELineEnding.WINDOWS);
    String testString = "Hello there\nHow are you?\n";
    String adjustLineEndings = MergeConflictDialog._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\r\nHow are you?\r\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromUnixToMac()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.MAC, ELineEnding.MAC);
    String testString = "Hello there\nHow are you?\n";
    String adjustLineEndings = MergeConflictDialog._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\rHow are you?\r", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromWindowsToUnix()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.UNIX, ELineEnding.UNIX);
    String testString = "Hello there\r\nHow are you?\r\n";
    String adjustLineEndings = MergeConflictDialog._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\nHow are you?\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToUnix()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.UNIX, ELineEnding.UNIX);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictDialog._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\nHow are you?\nThere's another one\n", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToMac()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.MAC, ELineEnding.MAC);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictDialog._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\rHow are you?\rThere's another one\r", adjustLineEndings);
  }

  @Test
  void testAdjustLineEndingsFromMixedToWindows()
  {
    IMergeData mergeData = _createMergeData(ELineEnding.WINDOWS, ELineEnding.WINDOWS);
    String testString = "Hello there\r\nHow are you?\rThere's another one\n";
    String adjustLineEndings = MergeConflictDialog._adjustLineEndings(testString, mergeData);
    assertEquals("Hello there\r\nHow are you?\r\nThere's another one\r\n", adjustLineEndings);
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
    IFileContentInfo fileContentInfoTheirs = mock(IFileContentInfo.class);
    when(fileContentInfoTheirs.getLineEnding()).thenReturn(() -> pLineEndingTheirs);
    IFileDiff fileDiffTheirs = mock(IFileDiff.class);
    when(fileDiffTheirs.getFileContentInfo(EChangeSide.NEW)).thenReturn(fileContentInfoTheirs);
    IFileContentInfo fileContentInfoYours = mock(IFileContentInfo.class);
    when(fileContentInfoYours.getLineEnding()).thenReturn(() -> pLineEndingYours);
    IFileDiff fileDiffYours = mock(IFileDiff.class);
    when(fileDiffYours.getFileContentInfo(EChangeSide.NEW)).thenReturn(fileContentInfoYours);
    IMergeData mergeData = mock(IMergeData.class);
    when(mergeData.getDiff(EConflictSide.THEIRS)).thenReturn(fileDiffTheirs);
    when(mergeData.getDiff(EConflictSide.YOURS)).thenReturn(fileDiffYours);
    return mergeData;
  }
}
