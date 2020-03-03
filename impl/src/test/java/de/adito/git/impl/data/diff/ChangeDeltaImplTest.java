package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.IFileDiff;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit tests for the ChangeDeltaImpl of the IChangeDelta interface
 *
 * @author m.kaspera, 03.03.2020
 */
public class ChangeDeltaImplTest
{

  /**
   * Tests if no conflict is detected if two separate words on a line were changed
   */
  @Test
  void testIsConflictingFalse()
  {
    String originalVersion = "Hello there, this is a test\n";
    String changedVersion1 = "Hello there, this is some test\n";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hellou there, this is a test\n";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertFalse(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
  }

  /**
   * Tests if no conflict is detected if two word-changes happened directly adjacent to each other (but do not overlap)
   */
  @Test
  void testIsConflictingAdjacentFalse()
  {
    String originalVersion = "Hello there, this is a test\n";
    String changedVersion1 = "Hello there, this is some test\n";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, t'is a test\n";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertFalse(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
  }

  /**
   * Tests if a simple conflict in a line is detected
   */
  @Test
  void testIsConflicting()
  {
    String originalVersion = "Hello there, this is a test\n";
    String changedVersion1 = "Hello there, this is some test\n";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, t'is some test\n";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertTrue(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
  }

  /**
   * Tests multi-line changes that have several words changed, however there is no conflict on a word-level
   */
  @Test
  void testIsConflictingMultiLineMultiChangeFalse()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion1 = "Hello there, this is some test\nSo here are a few words\nNo use taking a break\nWe're not creating any turds";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, this is a test\nSo here are some Words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertFalse(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
    assertFalse(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(1)));
  }

  /**
   * Tests a change that spans several lines and has several changed words, including one conflict (line 3 at the end)
   */
  @Test
  void testIsConflictingMultiLineMultiChange()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion1 = "Hello there, this is some test\nSo here are a few words\nNo use taking a break\nWe're not creating any turds";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, this is a test\nSo here are some Words\nNo use taking a stop\nWe are not creating any turds";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertTrue(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
  }

  /**
   * Tests the case where changes occur on either side of a linebreak, but since one ends on the linebreak and one starts after, there is no conflict
   */
  @Test
  void testIsConflictingEOLFalse()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion1 = "Hello there, this is some test\nSo here are a few words\nno use taking a break\nWe're not creating any turds";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, this is a test\nSo here are some Words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertFalse(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
    assertFalse(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(1)));
  }

  /**
   * Tests the case of a conflicting change right at the very start
   */
  @Test
  void testIsConflictingStart()
  {
    String originalVersion = "Hello there, this is a test\n";
    String changedVersion1 = "Bello there, this is some test\n";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "hello there, this is a test\n";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertTrue(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
  }

  /**
   * Tests the case where both sides took the same change, thus there being an overlapping change but no conflict
   */
  @Test
  void testIsConflictingSameFalse()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion1 = "Bello there, this is some test\nSo here are some words\nNo use taking a rest\nWe are not creating a turd";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, this is test\nSo here are some words\nNo use taking a rest\nWe are not creating a turd";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    assertTrue(fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0)));
    assertFalse(fileDiff1.getChangeDeltas().get(1).isConflictingWith(fileDiff2.getChangeDeltas().get(1)));
  }
}
