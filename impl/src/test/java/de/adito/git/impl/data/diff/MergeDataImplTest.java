package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
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
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
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
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
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
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals(yourVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.reset();
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals(theirVersion, mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  @Test
  void testAcceptTwoInsertsSameLine()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nAn added line\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nAdditional lines\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals("Hello there, this is a test\nAdditional lines\nAn added line\nSo here are some words\nNo use taking a rest\nWe're not creating any turds",
                 mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  @Test
  void testAcceptTwoModifySameLines()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are some f words\nNo use taking a break\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are some additional words\nNo taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals("Hello there, this is a test\nSo here are some additional words\nNo taking a rest\nWe're not creating any turds",
                 mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  @Test
  void testAcceptTwoModifyAfterInsert()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are some additional words\nNo taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.modifyText("insert stuff", 0, "Hello there, this is a test\nSo".length());
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals("Hello there, this is a test\nSo here are some additional words\nNo taking a rest\nWe're not creating any turds",
                 mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }


  @Test
  void testAcceptDeleteAfterInsert()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.modifyText("insert stuff", 0, "Hello there, this is a test\nSo".length());
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals("Hello there, this is a test\nNo use taking a rest\nWe're not creating any turds",
                 mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  /**
   * tests if accepting a MODIFY chunk with some deleted words with a conflicting MODIFIED change results in the expected result
   */
  @Test
  void testAcceptTwoModifyDeltas()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are words\nNo use taking a rest\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a break\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(theirVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
  }

  /**
   * tests if accepting a MODIFIED change with conflicting DELETE change results in the expected result -> same as with only YOURS accepted, since
   * changes are conflicting and therefore the DELETE is appended -> does nothing
   */
  @Test
  void testAcceptDeltaModifyDelete()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String yourVersion = "Hello there, this is a test\nSo here are several words\nNo use taking a break\nWe're not creating any turds";
    String theirVersion = "Hello there, this is a test\nNo use taking a rest\nWe're not creating any turds";
    IMergeData mergeData = getMergeData(originalVersion, yourVersion, theirVersion);
    mergeData.markConflicting();
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(yourVersion, mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
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
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.discardChange(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertEquals(EChangeStatus.DISCARDED, mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0).getChangeStatus());
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0), EConflictSide.THEIRS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
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
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    assertNotEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW));
    mergeData.reset();
    assertEquals(originalVersion, mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    assertEquals(EChangeStatus.PENDING, mergeData.getDiff(EConflictSide.THEIRS).getChangeDeltas().get(0).getChangeStatus());
    assertEquals(EChangeStatus.PENDING, mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0).getChangeStatus());
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
    assertEquals("Pre-text:\n", mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD).substring(0, insertLen));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  /**
   * Tests if inserting some text into the area of an existing change works as expected
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
    assertEquals("more ", mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD).substring(insertOffset, insertOffset + "more ".length()));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(yourVersion, mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  /**
   * Tests if deleting some text from inside a an existing change works as expcted
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
    assertFalse(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD).contains("some"));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));

    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(yourVersion, mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));

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
    assertEquals("Hey you", mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD).substring(0, "Hey you".length()));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
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
                 mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD));
    assertEquals(mergeData.getDiff(EConflictSide.YOURS).getText(EChangeSide.OLD), mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
    mergeData.acceptDelta(mergeData.getDiff(EConflictSide.YOURS).getChangeDeltas().get(0), EConflictSide.YOURS);
    assertEquals(yourVersion, mergeData.getDiff(EConflictSide.THEIRS).getText(EChangeSide.OLD));
  }

  /**
   * Tests if getFilePath returns the correct file name for a chaned file
   */
  @Test
  void testGetFileHeader()
  {
    IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(null, "filea", "filea");
    IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE);
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
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
    IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(null, "filea", "filea");
    IDiffPathInfo diffPathInfo2 = new DiffPathInfoImpl(null, "filea", "fileb");
    IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE);
    IDiffDetails diffDetails2 = new DiffDetailsImpl("old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE);
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
    FileDiffHeaderImpl theirfileDiffHeader = new FileDiffHeaderImpl(diffPathInfo2, diffDetails2);
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
    IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(null, "filea", "fileb");
    IDiffPathInfo diffPathInfo2 = new DiffPathInfoImpl(null, "filea", "fileb");
    IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE);
    IDiffDetails diffDetails2 = new DiffDetailsImpl("old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE);
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
    FileDiffHeaderImpl theirfileDiffHeader = new FileDiffHeaderImpl(diffPathInfo2, diffDetails2);
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
    IDiffPathInfo diffPathInfo = new DiffPathInfoImpl(null, "filea", "fileb");
    IDiffPathInfo diffPathInfo2 = new DiffPathInfoImpl(null, "filea", "filec");
    IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE);
    IDiffDetails diffDetails2 = new DiffDetailsImpl("old", "new", EChangeType.RENAME, EFileType.FILE, EFileType.FILE);
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo, diffDetails);
    FileDiffHeaderImpl theirfileDiffHeader = new FileDiffHeaderImpl(diffPathInfo2, diffDetails2);
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
    EditList yoursChangedLines = LineIndexDiffUtil.getChangedLines(pOriginalVersion, pYourVersion, RawTextComparator.DEFAULT);
    EditList theirsChangedLines = LineIndexDiffUtil.getChangedLines(pOriginalVersion, pTheirVersion, RawTextComparator.DEFAULT);
    IFileDiff yourFileDiff = TestUtil._createFileDiff(yoursChangedLines, pOriginalVersion, pYourVersion);
    IFileDiff theirDiff = TestUtil._createFileDiff(theirsChangedLines, pOriginalVersion, pTheirVersion);
    return new MergeDataImpl(yourFileDiff, theirDiff);
  }

  /**
   * Simple test with some text to check if adjustEditListsForMerge works as expected
   */
  @Test
  void testAdjustEditListForMerge()
  {
    String pOriginalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String pYourVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    String pTheirVersion = "Hello there, this is a test\nSo here are some random words\nNo use taking a break\nWe're not creating any shit";
    EditList yoursChangedLines = LineIndexDiffUtil.getChangedLines(pOriginalVersion, pYourVersion, RawTextComparator.DEFAULT);
    EditList theirsChangedLines = LineIndexDiffUtil.getChangedLines(pOriginalVersion, pTheirVersion, RawTextComparator.DEFAULT);
    MergeDataImpl.adjustEditListForMerge(yoursChangedLines, theirsChangedLines);
    assertEquals(1, yoursChangedLines.size());
    assertEquals(1, theirsChangedLines.size());
    assertEquals(new Edit(1, 4, 1, 4), yoursChangedLines.get(0));
    assertEquals(new Edit(1, 4, 1, 4), theirsChangedLines.get(0));
  }

  /**
   * Another somewhat simple test for adjustEditListsForMerge, the last deltas have to be combined
   */
  @Test
  void testAdjustEditListForMerge2()
  {
    EditList yoursChangedLines = new EditList();
    EditList theirsChangedLines = new EditList();
    yoursChangedLines.add(new Edit(1, 1, 1, 2));
    theirsChangedLines.add(new Edit(1, 1, 1, 2));
    yoursChangedLines.add(new Edit(5, 8, 6, 9));
    theirsChangedLines.add(new Edit(7, 8, 8, 9));
    MergeDataImpl.adjustEditListForMerge(yoursChangedLines, theirsChangedLines);
    assertEquals(2, yoursChangedLines.size());
    assertEquals(2, theirsChangedLines.size());
    assertEquals(new Edit(1, 1, 1, 2), yoursChangedLines.get(0));
    assertEquals(new Edit(1, 1, 1, 2), theirsChangedLines.get(0));
    assertEquals(new Edit(5, 8, 6, 9), yoursChangedLines.get(1));
    assertEquals(new Edit(5, 8, 6, 9), theirsChangedLines.get(1));
  }

  /**
   * Tests if two adjacent or overlapping deltas are combined by adjustEditListsForMerge
   */
  @Test
  void testAdjustEditListForMerge3()
  {
    EditList yoursChangedLines = new EditList();
    EditList theirsChangedLines = new EditList();
    yoursChangedLines.add(new Edit(1, 1, 1, 2));
    yoursChangedLines.add(new Edit(5, 8, 6, 9));
    theirsChangedLines.add(new Edit(1, 1, 1, 2));
    theirsChangedLines.add(new Edit(5, 5, 5, 6));
    theirsChangedLines.add(new Edit(7, 8, 8, 9));
    MergeDataImpl.adjustEditListForMerge(yoursChangedLines, theirsChangedLines);
    assertEquals(2, yoursChangedLines.size());
    assertEquals(2, theirsChangedLines.size());
    assertEquals(new Edit(1, 1, 1, 2), yoursChangedLines.get(0));
    assertEquals(new Edit(1, 1, 1, 2), theirsChangedLines.get(0));
    assertEquals(new Edit(5, 8, 6, 9), yoursChangedLines.get(1));
    assertEquals(new Edit(5, 8, 5, 9), theirsChangedLines.get(1));
  }

  /**
   * The outcomne in this test is wrong if e.g. the min and max of two deltas is used when combining the deltas
   */
  @Test
  void testAdjustEditListForMerge4()
  {
    EditList yoursChangedLines = new EditList();
    EditList theirsChangedLines = new EditList();
    yoursChangedLines.add(new Edit(9, 15, 9, 25));
    yoursChangedLines.add(new Edit(16, 33, 26, 28));
    theirsChangedLines.add(new Edit(17, 23, 17, 21));
    theirsChangedLines.add(new Edit(25, 25, 23, 25));
    theirsChangedLines.add(new Edit(26, 28, 26, 26));
    MergeDataImpl.adjustEditListForMerge(yoursChangedLines, theirsChangedLines);
    assertEquals(2, yoursChangedLines.size());
    assertEquals(1, theirsChangedLines.size());
    assertEquals(new Edit(9, 15, 9, 25), yoursChangedLines.get(0));
    assertEquals(new Edit(16, 33, 26, 28), yoursChangedLines.get(1));
    assertEquals(new Edit(16, 33, 16, 31), theirsChangedLines.get(0));
  }

  /**
   * Tests if more than two deltas can be combined into one
   */
  @Test
  void testAdjustEditListForMerge5()
  {
    EditList yoursChangedLines = new EditList();
    EditList theirsChangedLines = new EditList();
    yoursChangedLines.add(new Edit(0, 0, 0, 1));
    yoursChangedLines.add(new Edit(4, 5, 5, 8));
    yoursChangedLines.add(new Edit(7, 11, 10, 11));
    yoursChangedLines.add(new Edit(12, 12, 12, 14));
    yoursChangedLines.add(new Edit(13, 13, 15, 16));
    yoursChangedLines.add(new Edit(21, 35, 24, 29));
    yoursChangedLines.add(new Edit(38, 39, 32, 63));
    theirsChangedLines.add(new Edit(9, 10, 9, 10));
    theirsChangedLines.add(new Edit(21, 26, 21, 26));
    theirsChangedLines.add(new Edit(27, 34, 27, 38));
    theirsChangedLines.add(new Edit(35, 36, 39, 41));
    MergeDataImpl.adjustEditListForMerge(yoursChangedLines, theirsChangedLines);
    assertEquals(7, yoursChangedLines.size());
    assertEquals(2, theirsChangedLines.size());
    assertEquals(new Edit(0, 0, 0, 1), yoursChangedLines.get(0));
    assertEquals(new Edit(4, 5, 5, 8), yoursChangedLines.get(1));
    assertEquals(new Edit(7, 11, 10, 11), yoursChangedLines.get(2));
    assertEquals(new Edit(12, 12, 12, 14), yoursChangedLines.get(3));
    assertEquals(new Edit(13, 13, 15, 16), yoursChangedLines.get(4));
    assertEquals(new Edit(21, 36, 24, 30), yoursChangedLines.get(5));
    assertEquals(new Edit(38, 39, 32, 63), yoursChangedLines.get(6));
    assertEquals(new Edit(7, 11, 7, 11), theirsChangedLines.get(0));
    assertEquals(new Edit(21, 36, 21, 41), theirsChangedLines.get(1));
  }
}
