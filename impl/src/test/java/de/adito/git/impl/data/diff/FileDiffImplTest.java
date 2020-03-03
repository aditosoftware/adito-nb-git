package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 24.02.2020
 */
public class FileDiffImplTest
{

  //StandAloneDiffProviderImpl diffProvider = new StandAloneDiffProviderImpl(new FileSystemUtilTestStub());
  //  diffProvider.diff(oldVersion, newVersion, true);


  /*
   *************************************  GET DELTAS TESTS  *************************************
   */

  /**
   * Tests if the line and text indices are as expected for a simple text with a single line that has changed
   */
  @Test
  void testGetChangeDeltasOneLine()
  {
    String oldVersion = "Hello there! Some changes in the first and only line";
    String newVersion = "Hello there! Some changes in the first and only line!";
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.MODIFY, editList.get(0), 0, oldVersion.length(), 0, newVersion.length());
  }

  /**
   * Tests if the line and text indices are as expected for a multi-line text that has changed on every line
   */
  @Test
  void testGetChangeDeltasModifyAll()
  {
    String oldVersion = "Hello there! Some changes in the first and only line\nOh\n";
    String newVersion = "Hello there! Some changes in the first and only line!\nOh my\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 2, 0, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.MODIFY, editList.get(0), 0, oldVersion.length(), 0, newVersion.length());
  }

  /**
   * Tests if the line and text indices are as expected for a multi-line text that has several of its lines modified (though not continous lines)
   */
  @Test
  void testGetChangeDeltasModifyMultiple()
  {
    String oldPart1 = "Hello there! Some changes in the first and only line\n";
    String oldPart2 = "Filler line\n";
    String oldPart3 = "Oh\n";
    String newPart1 = "Hello there! Some changes in the first and only line!\n";
    String newPart3 = "Oh my\n";
    String oldVersion = oldPart1 + oldPart2 + oldPart3;
    String newVersion = newPart1 + oldPart2 + newPart3;
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    editList.add(new Edit(2, 3, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.MODIFY, editList.get(0), 0, oldPart1.length(), 0, newPart1.length());
    _checkChangeDelta(changeDeltas.get(1), EChangeType.MODIFY, editList.get(1), oldPart1.length() + oldPart2.length(), oldVersion.length(),
                      newPart1.length() + oldPart2.length(), newVersion.length());
  }

  /**
   * Tests if the line and text indices are as expected for the case when one line is removed from a multi-line text
   */
  @Test
  void testGetChangeDeltasRemoval()
  {
    String oldVersion = "Hello there!\n There was some stuff here\n";
    String newVersion = "Hello there!\n";
    EditList editList = new EditList();
    editList.add(new Edit(1, 2, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.DELETE, editList.get(0), 13, oldVersion.length(), 13, newVersion.length());
  }

  /**
   * Tests if the line and text indices are as expected for a multi-line text which has more than one line removed (continous in this case, and not all lines are removed)
   */
  @Test
  void testGetChangeDeltasRemovalMultiLine()
  {
    String oldVersion = "Hello there!\n There was some stuff here\nAnd here?\n";
    String newVersion = "Hello there!";
    EditList editList = new EditList();
    editList.add(new Edit(1, 3, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.DELETE, editList.get(0), 13, oldVersion.length(), 13, 13);
  }

  /**
   * Tests if the line and text indices are as expected for  a multi-line text that has a line removed in between other lines
   */
  @Test
  void testGetChangeDeltasRemovalInBetween()
  {
    String oldVersion1 = "Hello there!\n";
    String oldVersion2 = "There was some stuff here\n";
    String oldVersion3 = "And here?\n";
    String oldVersion = oldVersion1 + oldVersion2 + oldVersion3;
    String newVersion = oldVersion1 + oldVersion3;
    EditList editList = new EditList();
    editList.add(new Edit(1, 2, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.DELETE, editList.get(0), 13, oldVersion1.length() + oldVersion2.length(), 13, 13);
  }

  /**
   * Tests if the line and text indices are as expected for a multi-line text that is completely deleted
   */
  @Test
  void testGetChangeDeltasRemovalComplete()
  {
    String oldVersion = "Hello there!\n There was some stuff here\nAnd here?\n";
    String newVersion = "";
    EditList editList = new EditList();
    editList.add(new Edit(0, 3, 0, 0));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.DELETE, editList.get(0), 0, oldVersion.length(), 0, 0);
  }

  /**
   * Tests if the line and text indices are as expected for a text that has a line added at the end
   */
  @Test
  void testGetChangeDeltasInsert()
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Hello there!\n There is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.ADD, editList.get(0), 13, oldVersion.length(), 13, newVersion.length());
  }

  /**
   * Tests if the line and text indices are as expected for a text that has a line added at the start
   */
  @Test
  void testGetChangeDeltasInsertFront()
  {
    String oldVersion = "Hello there!\n";
    String additionalLine = "There is some additional stuff here\n";
    String newVersion = additionalLine + "Hello there!\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.ADD, editList.get(0), 0, 13, 0, additionalLine.length());
  }

  /**
   * Tests if the line and text indices are as expected for an insert of a new line if the first line didn't end on a newline
   */
  @Test
  void testGetChangeDeltasInsert2()
  {
    String oldVersion = "Hello there!";
    String newVersion = "Hello there!\n There is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.ADD, editList.get(0), 13, oldVersion.length(), 13, newVersion.length());
  }

  /**
   * Tests if the line and text indices are as expected for an additional line in a mulit-line text, the additional line is inserted between other lines
   */
  @Test
  void testGetChangeDeltasInsertBetween()
  {
    String oldLine1 = "Hello there!\n";
    String oldLine2 = "This was here before though";
    String newLine = " There is some additional stuff here\n";
    String oldVersion = oldLine1 + oldLine2;
    String newVersion = oldLine1 + newLine + oldLine2;
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    _checkChangeDelta(changeDeltas.get(0), EChangeType.ADD, editList.get(0), 13, oldVersion.length(), 13, oldLine1.length() + newLine.length());
  }

  /*
   *************************************  ACCEPT DELTAS TESTS  *************************************
   */

  /**
   * Tests if accepting all changes for a one-line text that is modified results in the original text
   */
  @Test
  void testAcceptChangeDeltaModifySimple()
  {
    String oldVersion = "Hello there! Some changes in the first and only line";
    String newVersion = "Hello there! Some changes in the first and only line!";
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    fileDiff.acceptDelta(changeDeltas.get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a modified multi-line text results in the original text
   */
  @Test
  void testAcceptDeltaModifyAll()
  {
    String oldVersion = "Hello there! Some changes in the first and only line\nOh\n";
    String newVersion = "Hello there! Some changes in the first and only line!\nOh my\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 2, 0, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    fileDiff.acceptDelta(changeDeltas.get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a mutli-line text that has several non-connected modifications results in the original text
   */
  @Test
  void testAcceptDeltasModifyMultiple()
  {
    String oldPart1 = "Hello there! Some changes in the first and only line\n";
    String oldPart2 = "Filler line\n";
    String oldPart3 = "Oh\n";
    String newPart1 = "Hello there! Some changes in the first and only line!\n";
    String newPart3 = "Oh my\n";
    String oldVersion = oldPart1 + oldPart2 + oldPart3;
    String newVersion = newPart1 + oldPart2 + newPart3;
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    editList.add(new Edit(2, 3, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    fileDiff.acceptDelta(changeDeltas.get(0));
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(1));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a multi-line text that has several of its lines removed (removed lines are continous) results in the original text
   */
  @Test
  void testAcceptDeltasRemovalMultiLine()
  {
    String oldVersion = "Hello there!\n There was some stuff here\nAnd here?\n";
    String newVersion = "Hello there!";
    EditList editList = new EditList();
    editList.add(new Edit(1, 3, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a multi-line text that has one of its lines in the middle removed results in the original text
   */
  @Test
  void testAcceptDeltasRemovalInBetween()
  {
    String oldVersion1 = "Hello there!\n";
    String oldVersion2 = "There was some stuff here\n";
    String oldVersion3 = "And here?\n";
    String oldVersion = oldVersion1 + oldVersion2 + oldVersion3;
    String newVersion = oldVersion1 + oldVersion3;
    EditList editList = new EditList();
    editList.add(new Edit(1, 2, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a multi-line text that has all of its lines removed results in the original text
   */
  @Test
  void testAcceptDeltasRemovalComplete()
  {
    String oldVersion = "Hello there!\n There was some stuff here\nAnd here?\n";
    String newVersion = "";
    EditList editList = new EditList();
    editList.add(new Edit(0, 3, 0, 0));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the back results in the original text
   */
  @Test
  void testAcceptDeltasInsert()
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Hello there!\n There is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the front results in the original text
   */
  @Test
  void testAcceptDeltasInsertFront()
  {
    String oldVersion = "Hello there!\n";
    String additionalLine = "There is some additional stuff here\n";
    String newVersion = additionalLine + "Hello there!\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the front and the back results in the original text
   */
  @Test
  void testAcceptDeltasInsert2()
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Title?\nHello there!\nThere is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    editList.add(new Edit(1, 1, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(1));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes in reverse for a one-line text that has an additional line added at the front and the back results in the original text
   */
  @Test
  void testAcceptDeltasInsert2Reverse()
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Title?\nHello there!\nThere is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    editList.add(new Edit(1, 1, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(1));
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }


  /**
   * Tests if accepting all changes for a multi-line text that has an additional line inserted in the middle results in the original text
   */
  @Test
  void testAcceptDeltasInsertBetween()
  {
    String oldLine1 = "Hello there!\n";
    String oldLine2 = "This was here before though";
    String newLine = " There is some additional stuff here\n";
    String oldVersion = oldLine1 + oldLine2;
    String newVersion = oldLine1 + newLine + oldLine2;
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /*
   *************************************  RESET TESTS  *************************************
   */

  /**
   * Tests if accepting some changes and then calling reset results in the starting state
   */
  @Test
  void testReset()
  {
    String oldLine1 = "Hello there!\n";
    String oldLine2 = "This was here before though";
    String newLine = "There is some additional stuff here\n";
    String oldVersion = oldLine1 + oldLine2;
    String newVersion = oldLine1 + newLine + oldLine2;
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.acceptDelta(fileDiff.getChangeDeltas().get(0));
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.ACCEPTED, fileDiff.getChangeDeltas().get(0).getChangeStatus().getChangeStatus());
    fileDiff.reset();
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.OLD));
    assertEquals(newVersion, fileDiff.getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.PEDNING, fileDiff.getChangeDeltas().get(0).getChangeStatus().getChangeStatus());
  }

  /**
   * Convenience method to shorten the amount of code for setting up a diff for a test
   *
   * @param pEditList   List with edits describing the changed lines
   * @param pOldVersion Text before changes
   * @param pNewVersion Text after changes
   * @return IFileDiff with some default parameters and the passed arguments
   */
  private IFileDiff _createFileDiff(@NotNull EditList pEditList, @NotNull String pOldVersion, @NotNull String pNewVersion)
  {
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(null, "old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE, "filea", "fileb");
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> pOldVersion, () -> StandardCharsets.UTF_8);
    IFileContentInfo newFileContent = new FileContentInfoImpl(() -> pNewVersion, () -> StandardCharsets.UTF_8);
    return new FileDiffImpl(fileDiffHeader, pEditList, oldFileContent, newFileContent);
  }

  /**
   * Check if the passed characteristics match the characteristics of the passed changeDelta
   *
   * @param pChangeDelta       IChangeDelta whose characteristics should be checked
   * @param pChangeType        EChangeType pChangeDelta is
   * @param pEdit              Edit with information about start and end-lines
   * @param pOldTextStartIndex Index of the first character of the change in the old version
   * @param pOldTextEndIndex   Index of the last character of the change in the old version
   * @param pNewTextStartIndex Index of the first character of the change in the new version
   * @param pNewTextEndIndex   Index of the last character of the change in the new version
   */
  private void _checkChangeDelta(IChangeDelta pChangeDelta, EChangeType pChangeType, Edit pEdit, int pOldTextStartIndex, int pOldTextEndIndex,
                                 int pNewTextStartIndex, int pNewTextEndIndex)
  {
    assertEquals(pChangeType, pChangeDelta.getChangeStatus().getChangeType());
    assertEquals(pEdit.getBeginA(), pChangeDelta.getStartLine(EChangeSide.OLD));
    assertEquals(pEdit.getEndA(), pChangeDelta.getEndLine(EChangeSide.OLD));
    assertEquals(pOldTextStartIndex, pChangeDelta.getStartTextIndex(EChangeSide.OLD));
    assertEquals(pOldTextEndIndex, pChangeDelta.getEndTextIndex(EChangeSide.OLD));
    assertEquals(pEdit.getBeginB(), pChangeDelta.getStartLine(EChangeSide.NEW));
    assertEquals(pEdit.getEndB(), pChangeDelta.getEndLine(EChangeSide.NEW));
    assertEquals(pNewTextStartIndex, pChangeDelta.getStartTextIndex(EChangeSide.NEW));
    assertEquals(pNewTextEndIndex, pChangeDelta.getEndTextIndex(EChangeSide.NEW));
  }

}
