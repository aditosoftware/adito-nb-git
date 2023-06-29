package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.*;
import de.adito.git.gui.guice.dummies.SimpleFileSystemUtil;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.impl.StandAloneDiffProviderImpl;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static de.adito.git.gui.dialogs.panels.basediffpanel.diffpane.LineChangeMarkingModel.INSERT_LINE_HEIGHT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the LineChangeMarkingModel and its logic
 *
 * @author m.kaspera, 12.01.2023
 */
class LineChangeMarkingModelTest
{

  private static final int LINE_HEIGHT = 17;

  /**
   * Test if the correct LineNumberColor is returned if there is a single modification
   */
  @Test
  void isCorrectLineNumberColorsWhenSingleModify()
  {
    LineChangeMarkingModel lineChangeMarkingModel = getLineChangeMarkingModel(new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT),
                                                                                               new LineNumber(2, 0, 17, LINE_HEIGHT),
                                                                                               new LineNumber(3, 0, 34, LINE_HEIGHT),
                                                                                               new LineNumber(4, 0, 51, LINE_HEIGHT)},
                                                                              "test\nchangedLine\ntest\n",
                                                                              "test\nchangedLine1\ntest\n");

    assertEquals(1, lineChangeMarkingModel.getStaticLineNumberColors().size());
    assertAll(
        () -> assertEquals(new Rectangle(0, LINE_HEIGHT, Integer.MAX_VALUE, LINE_HEIGHT), lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColoredArea()),
        () -> assertEquals(EChangeType.MODIFY.getDiffColor(), lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColor()));
  }

  /**
   * Initialise a LineChangeMarkingModel for testing
   *
   * @param pLineNumbers LineNumbers that should be fed as setup to the lineChangeMarkingModel
   * @param pOldVersion  Old version of the text for the diff
   * @param pNewVersion  New version of the text for the diff
   * @return LineChangeMarkingModel set up with
   */
  @NonNull
  private static LineChangeMarkingModel getLineChangeMarkingModel(LineNumber @NonNull [] pLineNumbers, @NonNull String pOldVersion, @NonNull String pNewVersion)
  {
    IFileDiff fileDiff = new StandAloneDiffProviderImpl(new SimpleFileSystemUtil()).diffOffline(pOldVersion, pNewVersion);
    IDeltaTextChangeEvent deltaTextChangeEvent = mock(IDeltaTextChangeEvent.class);
    // this has to be initialised
    when(deltaTextChangeEvent.getFileDiff()).thenReturn(fileDiff);

    LineNumberModel lineNumberModel = mock(LineNumberModel.class);
    LineChangeMarkingModel lineChangeMarkingModel = new LineChangeMarkingModel(lineNumberModel, EChangeSide.OLD);
    lineChangeMarkingModel.lineNumbersChanged(deltaTextChangeEvent, pLineNumbers);

    return lineChangeMarkingModel;
  }

  /**
   * Test if the correct LineNumberColor is returned if there is a single delete
   */
  @Test
  void isCorrectLineNumberColorsWhenSingleDelete()
  {
    LineChangeMarkingModel lineChangeMarkingModel = getLineChangeMarkingModel(new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT),
                                                                                               new LineNumber(2, 0, 17, LINE_HEIGHT),
                                                                                               new LineNumber(3, 0, 34, LINE_HEIGHT),
                                                                                               new LineNumber(4, 0, 51, LINE_HEIGHT)},
                                                                              "test\nchangedLine\ntest\n",
                                                                              "test\ntest\n");
    assertEquals(1, lineChangeMarkingModel.getStaticLineNumberColors().size());
    assertAll(() -> assertEquals(new Rectangle(0, LINE_HEIGHT, Integer.MAX_VALUE, LINE_HEIGHT), lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColoredArea()),
              () -> assertEquals(EChangeType.DELETE.getDiffColor(), lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColor()));
  }

  /**
   * Test if the correct LineNumberColor is returned if there is a single insert change
   */
  @Test
  void isCorrectLineNumberColorsWhenSingleInsert()
  {
    LineChangeMarkingModel lineChangeMarkingModel = getLineChangeMarkingModel(new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT),
                                                                                               new LineNumber(2, 0, 17, LINE_HEIGHT),
                                                                                               new LineNumber(3, 0, 34, LINE_HEIGHT),
                                                                                               new LineNumber(4, 0, 51, LINE_HEIGHT)},
                                                                              "test\ntest\n",
                                                                              "test\nchangedLine\ntest\n");
    assertEquals(1, lineChangeMarkingModel.getStaticLineNumberColors().size());
    assertAll(() -> assertEquals(new Rectangle(0, LINE_HEIGHT - INSERT_LINE_HEIGHT / 2, Integer.MAX_VALUE, INSERT_LINE_HEIGHT),
                                 lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColoredArea()),
              () -> assertEquals(EChangeType.ADD.getDiffColor(), lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColor()));
  }

  /**
   * Test if the correct LineNumberColors are returned if there are several kinds of modification
   */
  @Test
  void isCorrectLineNumberColorsWhenMultiChange()
  {
    LineChangeMarkingModel lineChangeMarkingModel = getLineChangeMarkingModel(new LineNumber[]{new LineNumber(1, 0, 0, LINE_HEIGHT),
                                                                                               new LineNumber(2, 0, 17, LINE_HEIGHT),
                                                                                               new LineNumber(3, 0, 34, LINE_HEIGHT),
                                                                                               new LineNumber(4, 0, 51, LINE_HEIGHT),
                                                                                               new LineNumber(5, 0, 68, LINE_HEIGHT),
                                                                                               new LineNumber(6, 0, 85, LINE_HEIGHT)},
                                                                              "test\ntest\n\ntest\nchangedLine\ntest\n",
                                                                              "test\nchangedLine\ntest\n\ntest\nchangedLine1\ntest\n");
    assertEquals(2, lineChangeMarkingModel.getStaticLineNumberColors().size());
    assertAll(() -> assertEquals(new Rectangle(0, LINE_HEIGHT - INSERT_LINE_HEIGHT / 2, Integer.MAX_VALUE, INSERT_LINE_HEIGHT),
                                 lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColoredArea()),
              () -> assertEquals(EChangeType.ADD.getDiffColor(), lineChangeMarkingModel.getStaticLineNumberColors().get(0).getColor()),
              () -> assertEquals(new Rectangle(0, 68, Integer.MAX_VALUE, LINE_HEIGHT), lineChangeMarkingModel.getStaticLineNumberColors().get(1).getColoredArea()),
              () -> assertEquals(EChangeType.MODIFY.getDiffColor(), lineChangeMarkingModel.getStaticLineNumberColors().get(1).getColor()));
  }
}