package de.adito.git.impl.data.diff;

import com.google.inject.Guice;
import de.adito.git.api.data.diff.EConflictSide;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.api.data.diff.IFileDiffHeader;
import de.adito.git.impl.data.DataModule;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * JUnit tests for the ChangeDeltaImpl of the IChangeDelta interface
 *
 * @author m.kaspera, 03.03.2020
 */
public class ChangeDeltaImplTest
{

  private static final ResolveOptionsProvider RESOLVE_OPTIONS_PROVIDER = Guice.createInjector(new DataModule()).getInstance(ResolveOptionsProvider.class);

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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType.getConflictType());
    assertNotNull(conflictType.getResolveOption());
    assertEquals(WordBasedResolveOption.class, conflictType.getResolveOption().getClass());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType.getConflictType());
    assertNotNull(conflictType.getResolveOption());
    assertEquals(WordBasedResolveOption.class, conflictType.getResolveOption().getClass());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.CONFLICTING, conflictType.getConflictType());
    assertNull(conflictType.getResolveOption());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType.getConflictType());
    assertNotNull(conflictType.getResolveOption());
    assertEquals(WordBasedResolveOption.class, conflictType.getResolveOption().getClass());
    ConflictType conflictType2 = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(1), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                      fileDiffHeader);
    assertEquals(EConflictType.NONE, conflictType2.getConflictType());
    assertNull(conflictType2.getResolveOption());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.CONFLICTING, conflictType.getConflictType());
    assertNull(conflictType.getResolveOption());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType.getConflictType());
    assertNotNull(conflictType.getResolveOption());
    assertEquals(WordBasedResolveOption.class, conflictType.getResolveOption().getClass());
    ConflictType conflictType2 = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(1), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                      fileDiffHeader);
    assertEquals(EConflictType.NONE, conflictType2.getConflictType());
    assertNull(conflictType2.getResolveOption());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.CONFLICTING, conflictType.getConflictType());
    assertNull(conflictType.getResolveOption());
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
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.CONFLICTING, conflictType.getConflictType());
    assertNull(conflictType.getResolveOption());
    ConflictType conflictType2 = fileDiff1.getChangeDeltas().get(1).isConflictingWith(fileDiff2.getChangeDeltas().get(1), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                      fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType2.getConflictType());
    assertNotNull(conflictType2.getResolveOption());
    assertEquals(SameResolveOption.class, conflictType2.getResolveOption().getClass());
  }

  /**
   * Tests a change that spans several lines and has several changed words, including one conflict (line 3 at the end)
   */
  @Test
  void testIsEnclosedYours()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion1 = "Hello there, this is a test\nSo here are some words\nTesting enclosing\nSo here is an additional line\nNo use taking a rest\nWe're not creating any turds";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, this is a test\nSo here are some words\nTesting enclosing\nNo use taking a rest\nWe're not creating any turds";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType.getConflictType());
    assertNotNull(conflictType.getResolveOption());
    assertEquals(EnclosedResolveOption.class, conflictType.getResolveOption().getClass());
    assertEquals(EConflictType.ENCLOSED_BY_THEIRS, ((EnclosedResolveOption) conflictType.getResolveOption()).getConflictType());
  }

  /**
   * Tests a change that spans several lines and has several changed words, including one conflict (line 3 at the end)
   */
  @Test
  void testIsEnclosedTheirs()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion1 = "Hello there, this is a test\nSo here are some words\nTesting enclosing\nNo use taking a rest\nWe're not creating any turds";
    EditList changedLines1 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion1, RawTextComparator.DEFAULT);
    IFileDiff fileDiff1 = TestUtil._createFileDiff(changedLines1, originalVersion, changedVersion1);
    String changedVersion2 = "Hello there, this is a test\nSo here are some words\nTesting enclosing\nSo here is an additional line\nNo use taking a rest\nWe're not creating any turds";
    EditList changedLines2 = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion2, RawTextComparator.DEFAULT);
    IFileDiff fileDiff2 = TestUtil._createFileDiff(changedLines2, originalVersion, changedVersion2);
    IFileDiffHeader fileDiffHeader = Mockito.mock(IFileDiffHeader.class);
    ConflictType conflictType = fileDiff1.getChangeDeltas().get(0).isConflictingWith(fileDiff2.getChangeDeltas().get(0), EConflictSide.THEIRS, RESOLVE_OPTIONS_PROVIDER,
                                                                                     fileDiffHeader);
    assertEquals(EConflictType.RESOLVABLE, conflictType.getConflictType());
    assertNotNull(conflictType.getResolveOption());
    assertEquals(EnclosedResolveOption.class, conflictType.getResolveOption().getClass());
    assertEquals(EConflictType.ENCLOSED_BY_YOURS, ((EnclosedResolveOption) conflictType.getResolveOption()).getConflictType());
  }

  /**
   * Tests if the _validateLines method correctly changes the editList such that the false editList given is corrected
   */
  @Test
  void testFixEditList()
  {
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    editList.add(new Edit(2, 3, 2, 2));
    editList.add(new Edit(8, 9, 7, 10));
    editList.add(new Edit(10, 11, 11, 13));
    String oldVersion = "v1FFSk+ \n-rRG IaSE8m7' S(pm@jk_ (nAg\" \n>U._me3 $*>Ruo \nJ\\>yHK3x*2 \n";
    String newVersion = "-rRp[Sm$GFtDT4Z  IaSE8m7' S(pm@jk_ (nAg\" \n>U._me3 $*>Dv0--g-{ s5zWs o \nJ\\>i|dw2dk9aQrs6> 2 \n";
    EditList edits = ChangeDeltaImpl._validateLines(editList, oldVersion, newVersion, oldVersion.replace(" ", "\n"), newVersion.replace(" ", "\n"));
    assertEquals(3, edits.size());
    assertEquals(new Edit(0, 3, 0, 2), edits.get(0));
    assertEquals(editList.get(3), edits.get(1));
    assertEquals(editList.get(4), edits.get(2));
  }

  /**
   * Tests if the _getUnmodifiedLines method finds the correct unmodified lines
   */
  @Test
  void testGetUnmodifedLines()
  {
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    editList.add(new Edit(2, 3, 2, 2));
    editList.add(new Edit(8, 9, 7, 10));
    editList.add(new Edit(10, 11, 11, 13));
    String oldVersion = "v1FFSk+ \n-rRG IaSE8m7' S(pm@jk_ (nAg\" \n>U._me3 $*>Ruo \nJ\\>yHK3x*2 \n";
    List<Integer> list = ChangeDeltaImpl._getUnmodifiedLines(editList, oldVersion.replace(" ", "\n"), new ChangeDeltaImpl.OriginalEditSideInfo());
    assertEquals(List.of(1, 3, 4, 5, 6, 7, 9, 11), list);
  }
}
