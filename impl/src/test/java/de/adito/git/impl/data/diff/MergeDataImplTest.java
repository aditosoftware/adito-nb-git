package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author m.kaspera, 06.03.2020
 */
public class MergeDataImplTest
{

  /**
   * test if accepting a change is applied to both sides of the merge as intended
   */
  @Test
  void testAcceptDelta()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are some additional words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are some more words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
  }

  /**
   * tests if accepting non-conflicting changes is applied to both sides of the merge
   */
  @Test
  void testAcceptDeltaNoConflict()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a break\nWe are not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are some more words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
  }

  /**
   * tests if accepting a DELETE change with a conflicting ADD change works as intended, and the ADD change gets status UNDEFINED
   */
  @Test
  void testAcceptDeltaDeleteAdd()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are some more words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertThrows(IllegalArgumentException.class, () -> mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS));
  }

  /**
   * tests if accepting a DELETE change that removes an entire line with conflicting ADD change yields the expected result
   */
  @Test
  void testAcceptDeltaDeleteLineAdd()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nAdditional lines\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals("Hello there, this is a test\nAdditional lines\nNo use taking a rest\nWe're not creating any turds",
                 mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
  }

  /**
   * tests if accepting a MODIFY chunk with some deleted words with a conflicting MODIFIED change results in the expected result
   */
  @Test
  void testAcceptDeltaDeleteModify()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertThrows(IllegalArgumentException.class, () -> mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS));
  }

  /**
   * tests if accepting a MODIFIED change with conflicting DELETE change results in the expected result
   */
  @Test
  void testAcceptDeltaModifyDelete()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertThrows(IllegalArgumentException.class, () -> mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS));
  }

  /**
   * tests if discard works as intended, and also checks if a conflicting change of the discarded delta can be accepted
   */
  @Test
  void testAcceptDiscardChange()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.discardChange(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.DISCARDED, mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0).getChangeStatus().getChangeStatus());
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
  }

  /**
   * tests if reset reverts all changes done so far (both in terms of chunks and the text)
   */
  @Test
  void testReset()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertNotEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.UNDEFINED, mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0).getChangeStatus().getChangeStatus());
    mergeData.reset();
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.PEDNING, mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0).getChangeStatus().getChangeStatus());
    assertEquals(EChangeStatus.PEDNING, mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0).getChangeStatus().getChangeStatus());
  }

  /**
   * tests if inserting some text at the beginning of a the text works as intended and non-affected chunks can still be accepted afterwards (and also work as intended)
   */
  @Test
  void testInsertText()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    int insertLen = "Pre-text:\n".length();
    mergeData.modifyText("Pre-text:\n", insertLen, 0);
    assertEquals("Pre-text:\n", mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW).substring(0, insertLen));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
  }

  /**
   * Tests if inserting some text into the area of an existing change works, and puts that chunk to the UNDEFINED state
   */
  @Test
  void testInsertTextInChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    int insertOffset = "Hello there, this is a test\nSo here are some ".length();
    mergeData.modifyText("more ", 0, insertOffset);
    assertEquals("more ", mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW).substring(insertOffset, insertOffset + "more ".length()));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertThrows(IllegalArgumentException.class, () -> mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS));
  }

  /**
   * Tests if deleting some text from inside a an existing change works and puts that chunk to the UNDEFINED state
   */
  @Test
  void testDeleteTextInChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    int insertOffset = "Hello there, this is a test\nSo here are ".length();
    mergeData.modifyText(null, "some ".length(), insertOffset);
    assertFalse(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW).contains("some"));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertThrows(IllegalArgumentException.class, () -> mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS));
  }

  /**
   * tests if modyfing some text at the beginning of a the text works as intended and non-affected chunks can still be accepted afterwards (and also work as intended)
   */
  @Test
  void testModifyText()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    int insertLen = "Hello there".length();
    mergeData.modifyText("Hey you", insertLen, 0);
    assertEquals("Hey you", mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW).substring(0, "Hey you".length()));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
  }

  /**
   * test if modifying some text inside an existing change works as expected
   */
  @Test
  void testModifyTextInChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    int insertOffset = "Hello there, this is a test\nSo here are ".length();
    mergeData.modifyText("many ", "some ".length(), insertOffset);
    assertEquals("Hello there, this is a test\nSo here are many words\nNo use taking a rest\nWe're not creating any turds",
                 mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.NEW));
    assertThrows(IllegalArgumentException.class, () -> mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS));
  }

  /**
   * Tests if getFilePath returns the correct file name for a chaned file
   */
  @Test
  void testGetFileHeader()
  {
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE, "filea", "filea");
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    IFileDiff yourFileDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IFileDiff theirDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IMergeData mergeData = new MergeDataImpl(yourFileDiff, theirDiff);
    assertEquals("filea", mergeData.getFilePath());
  }

  /**
   * Tests if getFilePath returns the "old" version of the file name if one side has been renamed
   */
  @Test
  void testGetFileHeaderRenameOne()
  {
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE, "filea", "filea");
    FileDiffHeaderImpl theirfileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE, "filea", "fileb");
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    IFileDiff yourFileDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IFileDiff theirDiff = new FileDiffImpl(theirfileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IMergeData mergeData = new MergeDataImpl(yourFileDiff, theirDiff);
    assertEquals("filea", mergeData.getFilePath());
  }

  /**
   * Tests if getFilePath returns the "old" version of the file name if both sides have the file renamed
   */
  @Test
  void testGetFileHeaderRenameTwo()
  {
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE, "filea", "fileb");
    FileDiffHeaderImpl theirfileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE, "filea", "fileb");
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    IFileDiff yourFileDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IFileDiff theirDiff = new FileDiffImpl(theirfileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IMergeData mergeData = new MergeDataImpl(yourFileDiff, theirDiff);
    assertEquals("filea", mergeData.getFilePath());
  }

  /**
   * Tests if getFilePath returns the "old" version of the file name if both sides have the file renamed, but with different names
   */
  @Test
  void testGetFileHeaderRenameDifferent()
  {
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE, "filea", "fileb");
    FileDiffHeaderImpl theirfileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE, "filea", "filec");
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    IFileDiff yourFileDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IFileDiff theirDiff = new FileDiffImpl(theirfileDiffHeader, new EditList(), oldFileContent, oldFileContent);
    IMergeData mergeData = new MergeDataImpl(yourFileDiff, theirDiff);
    assertEquals("filea", mergeData.getFilePath());
  }


  /**
   * create an MergeDataImpl from the different text versions (with default file name etc)
   *
   * @param pOriginalVersion ForkPoint version of the merge
   * @param pYourVersion     "Yours" version of the merge
   * @param pTheirVersion    "Theirs" version of the merge
   * @return IMergeData object containing the merge information of the passed strings
   */
  @NotNull
  private IMergeData getMergeData(String pOriginalVersion, String pYourVersion, String pTheirVersion)
  {
    EditList yoursChangedLines = LineIndexDiffUtil.getChangedLines(pYourVersion, pOriginalVersion, RawTextComparator.DEFAULT);
    EditList theirsChangedLines = LineIndexDiffUtil.getChangedLines(pTheirVersion, pOriginalVersion, RawTextComparator.DEFAULT);
    IFileDiff yourFileDiff = TestUtil._createFileDiff(yoursChangedLines, pYourVersion, pOriginalVersion);
    IFileDiff theirDiff = TestUtil._createFileDiff(theirsChangedLines, pTheirVersion, pOriginalVersion);
    return new MergeDataImpl(yourFileDiff, theirDiff);
  }
}
