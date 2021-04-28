package de.adito.git.impl.data.diff;

import de.adito.git.api.data.EFileType;
import de.adito.git.api.data.diff.*;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.junit.jupiter.api.Test;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import static de.adito.git.impl.data.diff.TestUtil._createFileDiff;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    fileDiff.revertDelta(changeDeltas.get(0), true);
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
    fileDiff.revertDelta(changeDeltas.get(0), true);
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
    fileDiff.revertDelta(changeDeltas.get(0), true);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(1), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the front results in the original text
   */
  @Test
  void testRevertDeltasInsertFront()
  {
    String oldVersion = "Hello there!\n";
    String additionalLine = "There is some additional stuff here\n";
    String newVersion = additionalLine + "Hello there!\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(1), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(1), true);
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
  }

  /*
   *************************************  TESTS THAT CHECK THE CHANGEEVENT MECHANISM  *************************************
   * These tests use the same data and cases as the acceptDelta tests, however they only check if the DeltaChangeEvents applied to a string and document
   * result in the expected outcome (in short - they test a different aspect of the acceptDelta method)
   */

  /**
   * Tests if accepting all changes for a one-line text that is modified results in the original text
   */
  @Test
  void testChangeEventDeltaModifySimple() throws BadLocationException
  {
    String oldVersion = "Hello there! Some changes in the first and only line";
    String newVersion = "Hello there! Some changes in the first and only line!";
    EditList editList = new EditList();
    editList.add(new Edit(0, 1, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(changeDeltas.get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a modified multi-line text results in the original text
   */
  @Test
  void testChangeEventModifyAll() throws BadLocationException
  {
    String oldVersion = "Hello there! Some changes in the first and only line\nOh\nOne More line\n";
    String newVersion = "Hello there! Some changes in the first and only line!\nOh my\nOne More line\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 2, 0, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    assertEquals(EChangeType.CHANGED, fileDiff.getChangeType());
    List<IChangeDelta> changeDeltas = fileDiff.getChangeDeltas();
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = fileDiff.revertDelta(changeDeltas.get(0), true);
    String firstChangeApplied = deltaTextChangeEvents.get(0).apply(newVersion);
    assertEquals(oldVersion, firstChangeApplied);
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvents.get(0).apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a mutli-line text that has several non-connected modifications results in the original text
   */
  @Test
  void testChangeEventsModifyMultiple() throws BadLocationException
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
    IDeltaTextChangeEvent deltaTextChangeEvent1 = fileDiff.revertDelta(changeDeltas.get(0), true).get(0);
    IDeltaTextChangeEvent deltaTextChangeEvent2 = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(1), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent2.apply(deltaTextChangeEvent1.apply(newVersion)));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent1.apply(document);
    deltaTextChangeEvent2.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a multi-line text that has several of its lines removed (removed lines are continous) results in the original text
   */
  @Test
  void testChangeEventRemovalMultiLine() throws BadLocationException
  {
    String oldVersion = "Hello there!\n There was some stuff here\nAnd here?\n";
    String newVersion = "Hello there!";
    EditList editList = new EditList();
    editList.add(new Edit(1, 3, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a multi-line text that has one of its lines in the middle removed results in the original text
   */
  @Test
  void testChangeEventRemovalInBetween() throws BadLocationException
  {
    String oldVersion1 = "Hello there!\n";
    String oldVersion2 = "There was some stuff here\n";
    String oldVersion3 = "And here?\n";
    String oldVersion = oldVersion1 + oldVersion2 + oldVersion3;
    String newVersion = oldVersion1 + oldVersion3;
    EditList editList = new EditList();
    editList.add(new Edit(1, 2, 1, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a multi-line text that has all of its lines removed results in the original text
   */
  @Test
  void testChangeEventRemovalComplete() throws BadLocationException
  {
    String oldVersion = "Hello there!\n There was some stuff here\nAnd here?\n";
    String newVersion = "";
    EditList editList = new EditList();
    editList.add(new Edit(0, 3, 0, 0));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the back results in the original text
   */
  @Test
  void testChangeEventInsert() throws BadLocationException
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Hello there!\n There is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the back results in the original text
   */
  @Test
  void testChangeEventInsertEmptyLine() throws BadLocationException
  {
    String oldVersion = "Hello there!\n\nThere is some additional stuff here";
    String newVersion = "Hello there!\n\n\nThere is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(2, 2, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the front results in the original text
   */
  @Test
  void testChangeEventInsertFront() throws BadLocationException
  {
    String oldVersion = "Hello there!\n";
    String additionalLine = "There is some additional stuff here\n";
    String newVersion = additionalLine + "Hello there!\n";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes for a one-line text that has an additional line added at the front and the back results in the original text
   */
  @Test
  void testChangeEventsInsert2() throws BadLocationException
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Title?\nHello there!\nThere is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    editList.add(new Edit(1, 1, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent1 = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    IDeltaTextChangeEvent deltaTextChangeEvent2 = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(1), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent2.apply(deltaTextChangeEvent1.apply(newVersion)));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent1.apply(document);
    deltaTextChangeEvent2.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }

  /**
   * Tests if accepting all changes in reverse for a one-line text that has an additional line added at the front and the back results in the original text
   */
  @Test
  void testChangeEventsInsert2ReverseOrder() throws BadLocationException
  {
    String oldVersion = "Hello there!\n";
    String newVersion = "Title?\nHello there!\nThere is some additional stuff here";
    EditList editList = new EditList();
    editList.add(new Edit(0, 0, 0, 1));
    editList.add(new Edit(1, 1, 2, 3));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent1 = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(1), true).get(0);
    IDeltaTextChangeEvent deltaTextChangeEvent2 = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent2.apply(deltaTextChangeEvent1.apply(newVersion)));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent1.apply(document);
    deltaTextChangeEvent2.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
  }


  /**
   * Tests if accepting all changes for a multi-line text that has an additional line inserted in the middle results in the original text
   */
  @Test
  void testChangeEventInsertBetween() throws BadLocationException
  {
    String oldLine1 = "Hello there!\n";
    String oldLine2 = "This was here before though";
    String newLine = " There is some additional stuff here\n";
    String oldVersion = oldLine1 + oldLine2;
    String newVersion = oldLine1 + newLine + oldLine2;
    EditList editList = new EditList();
    editList.add(new Edit(1, 1, 1, 2));
    IFileDiff fileDiff = _createFileDiff(editList, oldVersion, newVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true).get(0);
    assertEquals(oldVersion, deltaTextChangeEvent.apply(newVersion));
    Document document = new DefaultStyledDocument();
    document.insertString(0, newVersion, null);
    deltaTextChangeEvent.apply(document);
    assertEquals(oldVersion, document.getText(0, document.getLength()));
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
    fileDiff.revertDelta(fileDiff.getChangeDeltas().get(0), true);
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.ACCEPTED, fileDiff.getChangeDeltas().get(0).getChangeStatus());
    fileDiff.reset();
    assertEquals(oldVersion, fileDiff.getText(EChangeSide.OLD));
    assertEquals(newVersion, fileDiff.getText(EChangeSide.NEW));
    assertEquals(EChangeStatus.PENDING, fileDiff.getChangeDeltas().get(0).getChangeStatus());
  }

  /*
   *************************************  PROCESS TEXT EVENTS DELETE TESTS  *************************************
   */

  /**
   * Tests if deleting some text before a moves the indices of the delta by the appropriate amount
   */
  @Test
  void testProcessTextEventDeleteBeforeChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String versionBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                       fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent(0, 5, null, EChangeSide.NEW, false);
    assertEquals(versionBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                            fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(EChangeStatus.PENDING, fileDiff.getChangeDeltas().get(0).getChangeStatus());
  }

  /**
   * Tests if deleting some text between two deltas leaves the first delta untouchted but moves the second delta's indices by the appropriate amount
   */
  @Test
  void testProcessTextEventDeleteBetweenChunks()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String versionBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                       fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a test\nSo here are a few words\nNo ".length(), "use ".length(), null, EChangeSide.NEW, false);
    assertEquals(versionBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                            fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(EChangeStatus.PENDING, fileDiff.getChangeDeltas().get(0).getChangeStatus());
  }

  /**
   * Tests if deleting some text inside a delta pushes back the endTextIndex of the delta and all indices of the following delta by the appropriate amount
   */
  @Test
  void testProcessTextEventDeleteInChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String versionBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                       fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a test\n".length(), 5, null, EChangeSide.NEW, false);
    assertEquals(versionBefore.substring(5), fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                                         fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
  }

  /**
   * Tests if deleting text that is inside a delta and a bit after (case DELETE 4) results in the indices of the delta
   * and the following deltas are adjusted accordingly
   */
  @Test
  void testProcessTextEventDeleteInChunkAndAfter()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    Edit firstDeltaEdit = ChangeDeltaImpl.getLineInfo(fileDiff.getChangeDeltas().get(0));
    Edit secondDeltaEdit = ChangeDeltaImpl.getLineInfo(fileDiff.getChangeDeltas().get(1));
    fileDiff.processTextEvent("Hello there, this is a test\nSo here are ".length(), "a few words\nNo use ".length(), null, EChangeSide.NEW, false);
    assertEquals("So here are ", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                             fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    _checkLineEndings(fileDiff.getChangeDeltas().get(0), firstDeltaEdit, 0, -1);
    _checkLineEndings(fileDiff.getChangeDeltas().get(1), secondDeltaEdit, -1, -1);
  }

  /**
   * Tests if deleting exactly the text contained in the delta (case DELETE 7) results in
   * all its startIndices being equal to the endIndices (has size/length of 0) and the following chunk adjusting its indices accordingly
   */
  @Test
  void testProcessTextEventDeleteChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a test\n".length(), "So here are a few words\n".length(), null, EChangeSide.NEW, false);
    assertEquals("", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                 fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW), fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartLine(EChangeSide.NEW), fileDiff.getChangeDeltas().get(0).getEndLine(EChangeSide.NEW));
  }

  /**
   * Tests if deleting more than the text contained in the delta (this time mix of case 1 and 7) results in
   * all its startIndices being equal to the endIndices (has size/length of 0) and the following chunk adjusting its indices accordingly
   */
  @Test
  void testProcessTextEventDeleteMoreThanChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a".length(), " test\nSo here are a few words\n".length(), null, EChangeSide.NEW, false);
    assertEquals("", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                 fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW), fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartLine(EChangeSide.NEW), fileDiff.getChangeDeltas().get(0).getEndLine(EChangeSide.NEW));
  }

  /**
   * Tests if deleting more than the text contained in the delta (DELETE case 2) results in all its startIndices being equal to
   * the endIndices (has size/length of 0) and the following chunk adjusting its indices accordingly
   */
  @Test
  void testProcessTextEventDeleteMoreThanChunk2()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a".length(), " test\nSo here are a few words\nNo".length(), null, EChangeSide.NEW, false);
    assertEquals("", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                 fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW), fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartLine(EChangeSide.NEW), fileDiff.getChangeDeltas().get(0).getEndLine(EChangeSide.NEW));
  }

  /**
   * Tests if deleting more than the text contained in the delta (this time mix of case 1 and 7) results in
   * all its startIndices being equal to the endIndices (has size/length of 0) and the following chunk adjusting its indices accordingly
   */
  @Test
  void testProcessTextEventDeletePartOfChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a".length(), " test\nSo here are a few ".length(), null, EChangeSide.NEW, false);
    assertEquals("words\n", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                        fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW) + "words\n".length(), fileDiff.getChangeDeltas().get(0)
        .getEndTextIndex(EChangeSide.NEW));
    assertEquals(fileDiff.getChangeDeltas().get(0).getStartLine(EChangeSide.NEW) + 1, fileDiff.getChangeDeltas().get(0).getEndLine(EChangeSide.NEW));
  }

  /*
   *************************************  PROCESS TEXT EVENTS INSERT TESTS  *************************************
   */

  /**
   * Tests if inserting a few additional words in front of a delta results in both startTextIndex and endTextIndex being moved back accordingly
   */
  @Test
  void testProcessTextEventInsertBeforeChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String versionBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                       fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent(6, 4, "you ", EChangeSide.NEW, false);
    assertEquals(versionBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                            fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(EChangeStatus.PENDING, fileDiff.getChangeDeltas().get(0).getChangeStatus());
  }

  /**
   * Tests if inserting a few words inside a delta results in the endTextIndex being moved back accordingly
   */
  @Test
  void testProcessTextEventInsertInChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    fileDiff.processTextEvent("Hello there, this is a test\nSo here are a few ".length(), 0, "more ", EChangeSide.NEW, false);
    assertEquals("So here are a few more words\n", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                                               fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
  }

  /**
   * Tests if inserting a text with a newline before a delta moves the indices back
   */
  @Test
  void testProcessTextEventInsertNewline()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String versionBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                       fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW));
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    Edit firstDeltaEdit = ChangeDeltaImpl.getLineInfo(fileDiff.getChangeDeltas().get(0));
    Edit secondDeltaEdit = ChangeDeltaImpl.getLineInfo(fileDiff.getChangeDeltas().get(1));
    fileDiff.processTextEvent(6, 4, "you\n", EChangeSide.NEW, false);
    assertEquals(versionBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                            fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(EChangeStatus.PENDING, fileDiff.getChangeDeltas().get(0).getChangeStatus());
    _checkLineEndings(fileDiff.getChangeDeltas().get(0), firstDeltaEdit, 1, 1);
    _checkLineEndings(fileDiff.getChangeDeltas().get(1), secondDeltaEdit, 1, 1);
  }

  /**
   * Tests if inserting several new lines into a delta modifies the content and start/end lines as should be
   */
  @Test
  void testProcessTextEventInsertNewLinesInChunk()
  {
    String originalVersion = "Hello there, this is a test\nSo here are some words\nNo use taking a rest\nWe're not creating any turds";
    String changedVersion = "Hello there, this is a test\nSo here are a few words\nNo use taking a rest\nWe are not creating any turds";
    EditList changedLines = LineIndexDiffUtil.getChangedLines(originalVersion, changedVersion, RawTextComparator.DEFAULT);
    IFileDiff fileDiff = TestUtil._createFileDiff(changedLines, originalVersion, changedVersion);
    String secondChunkBefore = fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                           fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW));
    Edit firstDeltaEdit = ChangeDeltaImpl.getLineInfo(fileDiff.getChangeDeltas().get(0));
    Edit secondDeltaEdit = ChangeDeltaImpl.getLineInfo(fileDiff.getChangeDeltas().get(1));
    fileDiff.processTextEvent("Hello there, this is a test\nSo here are a few ".length(), 0, "\nmore\n", EChangeSide.NEW, false);
    assertEquals("So here are a few \nmore\nwords\n", fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(0).getStartTextIndex(EChangeSide.NEW),
                                                                                                  fileDiff.getChangeDeltas().get(0).getEndTextIndex(EChangeSide.NEW)));
    assertEquals(secondChunkBefore, fileDiff.getText(EChangeSide.NEW).substring(fileDiff.getChangeDeltas().get(1).getStartTextIndex(EChangeSide.NEW),
                                                                                fileDiff.getChangeDeltas().get(1).getEndTextIndex(EChangeSide.NEW)));
    _checkLineEndings(fileDiff.getChangeDeltas().get(0), firstDeltaEdit, 0, 2);
    _checkLineEndings(fileDiff.getChangeDeltas().get(1), secondDeltaEdit, 2, 2);
  }

  /**
   * Tests the diverse methods that have anything to do with retrieving the file paths
   */
  @Test
  void testGetFilePath()
  {
    IDiffDetails diffDetails = new DiffDetailsImpl("old", "new", EChangeType.CHANGED, EFileType.FILE, EFileType.FILE);
    IDiffPathInfo diffPathInfo1 = new DiffPathInfoImpl(null, "filea", "fileb");
    FileDiffHeaderImpl fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo1, diffDetails);
    IFileContentInfo oldFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    IFileContentInfo newFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    FileDiffImpl fileDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, newFileContent);
    assertEquals("filea", fileDiff.getFileHeader().getFilePath(EChangeSide.OLD));
    assertEquals("fileb", fileDiff.getFileHeader().getFilePath(EChangeSide.NEW));
    assertNull(fileDiff.getFileHeader().getAbsoluteFilePath());
    // set up a new fileDiffHeader that has a topLevel folder set
    IDiffPathInfo diffPathInfo2 = new DiffPathInfoImpl(new File("/test"), "filea", "fileb");
    fileDiffHeader = new FileDiffHeaderImpl(diffPathInfo2, diffDetails);
    oldFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    newFileContent = new FileContentInfoImpl(() -> "", () -> StandardCharsets.UTF_8);
    fileDiff = new FileDiffImpl(fileDiffHeader, new EditList(), oldFileContent, newFileContent);
    String absoluteFilePath = fileDiff.getFileHeader().getAbsoluteFilePath();
    assertEquals(new File("/test/fileb").getAbsolutePath(), absoluteFilePath == null ? null : Paths.get(absoluteFilePath).toString());
  }

  /**
   * checks if the ChangeDelta has the specified indices for start/end line
   *
   * @param pChangeDelta ChangeDelta whose line numbers should be checked
   * @param pEdit        Edit with the line numbers as were before the change/processed event
   * @param pOffsetStart offset from the edit that the new start line should have
   * @param pOffsetEnd   offset from the edit that the new end line should have
   */
  private void _checkLineEndings(IChangeDelta pChangeDelta, Edit pEdit, int pOffsetStart, int pOffsetEnd)
  {
    assertEquals(pEdit.getBeginB() + pOffsetStart, pChangeDelta.getStartLine(EChangeSide.NEW));
    assertEquals(pEdit.getEndB() + pOffsetEnd, pChangeDelta.getEndLine(EChangeSide.NEW));
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
    assertEquals(pChangeType, pChangeDelta.getChangeType());
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
