package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.IFileDiff;
import de.adito.git.api.data.diff.ILinePartChangeDelta;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.diff.RawTextComparator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.adito.git.impl.data.diff.TestUtil._createFileDiff;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 27.02.2020
 */
public class LinePartChangeTest
{

  /**
   * Tests if a changed word in a line is treated correctly
   */
  @Test
  void testSingleChange()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hello there\nThis is the text\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("some ", "the ")), fileDiff, 0);
  }

  /**
   * Check if a removed word in a line is treated correctly
   */
  @Test
  void testSingleDelete()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hello there\nThis is text\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("some ", "")), fileDiff, 0);
  }

  /**
   * Check if a line that was completely changed works as intended
   */
  @Test
  void testCompleteLineChanged()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hello there\nHere be Dragons\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    assertEquals(1, fileDiff.getChangeDeltas().get(0).getLinePartChanges().size());
    _testResult(List.of(new MutablePair<>("This is some text\n", "Here be Dragons\n")), fileDiff, 0);
  }

  /**
   * Check if a line that was completely added works as intended
   */
  @Test
  void testCompleteLineAdded()
  {
    String originalLines = "Hello there\nfor testing\n";
    String changedLines = "Hello there\nHere be Dragons\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    assertEquals(1, fileDiff.getChangeDeltas().get(0).getLinePartChanges().size());
    _testResult(List.of(new MutablePair<>("", "Here be Dragons\n")), fileDiff, 0);
  }

  /**
   * Check if a line that was completely removed works as intended
   */
  @Test
  void testCompleteLineRemoved()
  {
    String originalLines = "Hello there\nHere be Dragons\nfor testing\n";
    String changedLines = "Hello there\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    assertEquals(1, fileDiff.getChangeDeltas().get(0).getLinePartChanges().size());
    _testResult(List.of(new MutablePair<>("Here be Dragons\n", "")), fileDiff, 0);
  }

  /**
   * Tests if the case where several words of different lines, that belong to the same delta, are changed works as intended
   */
  @Test
  void testSingleWordsMultiLineChange()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hello there\nThis is the text\nfor testing the functionality\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("some ", "the "), new MutablePair<>("testing\n", "testing the functionality\n")), fileDiff, 0);
  }

  /**
   * Tests if the case where multiple adjacent words in a single line are changed works as intended
   */
  @Test
  void testMultipleWordsLineChange()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hello there\nT'is the text\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("This is some ", "T'is the ")), fileDiff, 0);
  }

  /**
   * Tests if the case where non-adjacent lines in a single line are changed is treated correctly
   */
  @Test
  void testMultipleSeparateWordsLineChange()
  {
    String originalLines = "Hello there\nThis is some longer text\nfor testing\n";
    String changedLines = "Hello there\nT'is some text\nfor testing\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("This is ", "T'is "), new MutablePair<>("longer ", "")), fileDiff, 0);
  }

  /**
   * tests if the linePartChanges Index is moved accordingly if a delta that adds stuff earlier in the text is accepted
   */
  @Test
  void testOffsetsAfterAcceptChunkBefore()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hellou who is there\nThis is some text\nfor testing the functionality\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("testing\n", "testing the functionality\n")), fileDiff, 1);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0));
    _testResult(List.of(new MutablePair<>("testing\n", "testing the functionality\n")), fileDiff, 1);
  }

  /**
   * tests if the linePartChanges Index is moved accordingly if a delta that removes stuff earlier in the text is accepted
   */
  @Test
  void testOffsetsAfterAcceptChunkBeforeRemove()
  {
    String originalLines = "Hellou you there\nThis is some text\nfor testing\n";
    String changedLines = "Hello there\nThis is some text\nfor testing the functionality\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("testing\n", "testing the functionality\n")), fileDiff, 1);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0));
    _testResult(List.of(new MutablePair<>("testing\n", "testing the functionality\n")), fileDiff, 1);
  }

  /**
   * tests if accepting a change also removes the linePartChanges (since the lines of the change now match)
   */
  @Test
  void testOffsetsAfterAcceptChunk()
  {
    String originalLines = "Hello there\nThis is some text\nfor testing\n";
    String changedLines = "Hellou there\nThis is some text\nfor testing the functionality\n";
    IFileDiff fileDiff = _createFileDiff(LineIndexDiffUtil.getChangedLines(originalLines, changedLines, RawTextComparator.DEFAULT), originalLines, changedLines);
    _testResult(List.of(new MutablePair<>("Hello ", "Hellou ")), fileDiff, 0);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0));
    _testResult(List.of(), fileDiff, 0);
  }

  /**
   * Check if the passed pairs of changed words match the ones from the fileDiff linePart changes
   *
   * @param pChangedPairs     List of Pairs, pairs form old and new version of a change
   * @param pFileDiff         fileDiff to check
   * @param pChangeDeltaIndex the linePartChanges of which changeDelta should be analysed
   */
  private void _testResult(List<Pair<String, String>> pChangedPairs, IFileDiff pFileDiff, int pChangeDeltaIndex)
  {
    List<ILinePartChangeDelta> linePartChanges = pFileDiff.getChangeDeltas().get(pChangeDeltaIndex).getLinePartChanges();
    for (int index = 0; index < pChangedPairs.size(); index++)
    {
      assertEquals(pChangedPairs.get(index).getLeft(), pFileDiff.getText(EChangeSide.OLD)
          .substring(linePartChanges.get(index).getStartTextIndex(EChangeSide.OLD), linePartChanges.get(index).getEndTextIndex(EChangeSide.OLD)));
      assertEquals(pChangedPairs.get(index).getRight(), pFileDiff.getText(EChangeSide.NEW)
          .substring(linePartChanges.get(index).getStartTextIndex(EChangeSide.NEW), linePartChanges.get(index).getEndTextIndex(EChangeSide.NEW)));
    }
  }
}
