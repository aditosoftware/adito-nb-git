package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.gui.swing.LineNumber;
import de.adito.git.gui.swing.SwingUtil;
import de.adito.git.gui.swing.TextPaneUtil;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the LineNumberModel and its logic
 *
 * @author m.kaspera, 23.12.2022
 */
class LineNumberModelTest
{

  /**
   * Test if the LineNumbers and TextEvent are fired correctly in case of an empty document with no lineNumbers at all
   *
   * @throws Exception potential Exceptions should lead to the test failing, so they are not caught
   */
  @Test
  void isLineNumbersAndTextEventFired() throws Exception
  {
    AtomicReference<LineNumber[]> lineNumbersRef = new AtomicReference<>(null);
    AtomicReference<IDeltaTextChangeEvent> textEventRef = new AtomicReference<>(null);
    AtomicReference<MockedStatic<TextPaneUtil>> mockStatic = new AtomicReference<>();
    try
    {
      LineNumber[] lineNumbers = new LineNumber[]{};
      IDeltaTextChangeEvent deltaTextChangeEvent = Mockito.mock(IDeltaTextChangeEvent.class);
      LineNumberModel lineNumberModel = setUpLineNumberModelTest(textEventRef, lineNumbersRef, lineNumbers, deltaTextChangeEvent, mockStatic);

      assertAll(() -> assertArrayEquals(lineNumbers, lineNumbersRef.get()),
                () -> assertEquals(deltaTextChangeEvent, textEventRef.get()),
                // there are no lines here -> no Numbers to draw
                () -> assertEquals(0, lineNumberModel.getLineNumbersToDraw(0, 200).size()));
    }
    finally
    {
      mockStatic.get().close();
    }
  }

  /**
   * Test if the model returns the correct values for the line numbers that should be drawn if several exist
   *
   * @throws Exception potential Exceptions should lead to the test failing, so they are not caught
   */
  @Test
  void doesLineNumbersToDrawReturnCorrectValues() throws Exception
  {
    AtomicReference<IDeltaTextChangeEvent> textEventRef = new AtomicReference<>(null);
    AtomicReference<LineNumber[]> lineNumbersRef = new AtomicReference<>(null);
    LineNumber[] lineNumbers = new LineNumber[]{new LineNumber(1, 0, 0, 17), new LineNumber(2, 0, 17, 17)};
    IDeltaTextChangeEvent textChangeMock = Mockito.mock(IDeltaTextChangeEvent.class);
    // no try-with-resources here because the staticMock has to be invoked in the EDT Thread -> manual close at the end of the test via the AtomicReference
    AtomicReference<MockedStatic<TextPaneUtil>> mockStatic = new AtomicReference<>();
    try
    {
      LineNumberModel lineNumberModel = setUpLineNumberModelTest(textEventRef, lineNumbersRef, lineNumbers, textChangeMock, mockStatic);

      assertAll(() -> assertArrayEquals(lineNumbers, lineNumbersRef.get()),
                () -> assertEquals(textChangeMock, textEventRef.get()));
      assertAll(
          // this view area encompasses both lines -> getLineNumbersToDraw should include both lines
          () -> assertArrayEquals(lineNumbers, lineNumberModel.getLineNumbersToDraw(0, 18).toArray()),
          // view only encompasses the first line
          () -> assertEquals("1", lineNumberModel.getLineNumbersToDraw(0, 5).iterator().next().getNumber()),
          // view only encompasses the second line
          () -> assertEquals("2", lineNumberModel.getLineNumbersToDraw(18, 25).iterator().next().getNumber()),
          () -> assertEquals(0, lineNumberModel.getLineNumbersToDraw(35, 45).size()));
    }
    finally
    {
      mockStatic.get().close();
    }
  }

  /**
   * set up a LineNumberModel and feed it some values that are passed as arguments
   *
   * @param textEventRef   Reference to the TextEvent, will be set by the listener that is triggered by the model changing
   * @param lineNumbersRef Reference to the LineNumbers, will be set by the listener that is triggered by the model changing
   * @param lineNumbers    LineNumbers that should be returned when TextPaneUtil.calculateLineYPositions is invoked
   * @param textChangeMock IDeltaTextChangeEvent that is passed to the model as the latest value in the Observable of the TextEvents
   * @param mockStatic     Reference for the static mock of the TextPaneUtil, will be set in this method and has to be closed by the caller to avoid resource leaks.
   *                       We have to use the reference here because MockStatic only works for the current Thread, and the thing we want to mock happens inside the EDT Thread
   * @return LineNumberModel that is based on the passed values and a Mocked TextPaneUtil
   * @throws InterruptedException if the sleep or invokeAndWait method is interrupted while they wait for a result/timer
   * @throws Exception            if any exception would happen while setting up the mocks in the invokeSynchronouslyASAP method
   */
  @NotNull
  private static LineNumberModel setUpLineNumberModelTest(@NotNull AtomicReference<IDeltaTextChangeEvent> textEventRef, @NotNull AtomicReference<LineNumber[]> lineNumbersRef,
                                                          @NotNull LineNumber[] lineNumbers, @NotNull IDeltaTextChangeEvent textChangeMock,
                                                          @NotNull AtomicReference<MockedStatic<TextPaneUtil>> mockStatic) throws Exception
  {
    JEditorPane editorPane = new JEditorPane();
    BehaviorSubject<IDeltaTextChangeEvent> textEventObs = BehaviorSubject.createDefault(Mockito.mock(IDeltaTextChangeEvent.class));
    BehaviorSubject<Dimension> areaObs = BehaviorSubject.createDefault(new Dimension(0, 0));

    SwingUtil.invokeSynchronouslyASAP(() -> {
      mockStatic.set(Mockito.mockStatic(TextPaneUtil.class));
      mockStatic.get().when(() -> TextPaneUtil.calculateLineYPositions(editorPane, editorPane.getUI().getRootView(editorPane))).thenReturn(lineNumbers);
      return null;
    });

    LineNumberListener lineNumberListener = (pTextChangeEvent, pLineNumbers) -> {
      lineNumbersRef.set(pLineNumbers);
      textEventRef.set(pTextChangeEvent);
    };
    LineNumberModel lineNumberModel = new LineNumberModel(textEventObs, editorPane, areaObs);
    lineNumberModel.addListener(lineNumberListener);

    textEventObs.onNext(textChangeMock);
    Thread.sleep(500);
    return lineNumberModel;
  }
}