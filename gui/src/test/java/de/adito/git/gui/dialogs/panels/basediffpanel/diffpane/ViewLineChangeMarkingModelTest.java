package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ViewLineChangeMarkinsModel and its logic
 *
 * @author m.kaspera, 12.01.2023
 */
class ViewLineChangeMarkingModelTest
{

  /**
   * Test if an empty list is returned if there are no LineNumberColors returned by the LineChangeMarkingModel
   */
  @Test
  void isEmptyListReturnedWhenNoColorsExist()
  {
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = getLineChangeMarkingModel(List.of(), List.of(new Rectangle()));

    assertEquals(List.of(), viewLineChangeMarkingModel.getLineNumberColors());
  }

  /**
   * Test if the returned LineNumberColor is correctly offset if the viewPort has been scrolled down
   */
  @Test
  void isValueCorrectWithOffsetViewArea()
  {
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = getLineChangeMarkingModel(List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17))),
                                                                                      List.of(new Rectangle(0, 40, 900, 800)));

    assertAll(() -> assertEquals(1, viewLineChangeMarkingModel.getLineNumberColors().size()),
              () -> assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y));
  }

  /**
   * Test if the model updates after a scroll event and the LineNumberColor is offset correctly after that change
   */
  @Test
  void isValueAdjustedAfterScroll()
  {
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = getLineChangeMarkingModel(List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17))),
                                                                                      List.of(new Rectangle(0, 0, 900, 800),
                                                                                              new Rectangle(0, 40, 900, 800)));

    assertAll(() -> assertEquals(1, viewLineChangeMarkingModel.getLineNumberColors().size()),
              () -> assertEquals(50, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y));

    // trigger change event, the model should ask for the rectangle area again and receive the second value -> returned lineNumberColors should move
    viewLineChangeMarkingModel.stateChanged(new ChangeEvent(new Object()));

    assertAll(() -> assertEquals(1, viewLineChangeMarkingModel.getLineNumberColors().size()),
              () -> assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y));
  }

  /**
   * Test if the returned LineNumberColors are correctly offset if the viewPort has been scrolled down
   */
  @Test
  void isValuesCorrectWithOffsetViewArea()
  {
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = getLineChangeMarkingModel(List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17)),
                                                                                              new LineNumberColor(Color.GREEN, new Rectangle(0, 123, 10, 34))),
                                                                                      List.of(new Rectangle(0, 40, 900, 800)));

    assertAll(() -> assertEquals(2, viewLineChangeMarkingModel.getLineNumberColors().size()),
              () -> assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y),
              () -> assertEquals(83, viewLineChangeMarkingModel.getLineNumberColors().get(1).getColoredArea().y));
  }

  /**
   * Test if the model updates after a scroll event and the returned LineNumberColors are offset correctly after that change
   */
  @Test
  void isValuesAdjustedAfterScroll()
  {
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = getLineChangeMarkingModel(List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17)),
                                                                                              new LineNumberColor(Color.GREEN, new Rectangle(0, 123, 10, 34))),
                                                                                      List.of(new Rectangle(0, 0, 900, 800),
                                                                                              new Rectangle(0, 40, 900, 800)));

    assertAll(() -> assertEquals(2, viewLineChangeMarkingModel.getLineNumberColors().size()),
              () -> assertEquals(50, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y),
              () -> assertEquals(123, viewLineChangeMarkingModel.getLineNumberColors().get(1).getColoredArea().y));

    // trigger change event, the model should ask for the rectangle area again and receive the second value -> returned lineNumberColors should move
    viewLineChangeMarkingModel.stateChanged(new ChangeEvent(new Object()));

    assertAll(() -> assertEquals(2, viewLineChangeMarkingModel.getLineNumberColors().size()),
              () -> assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y),
              () -> assertEquals(83, viewLineChangeMarkingModel.getLineNumberColors().get(1).getColoredArea().y));
  }

  /**
   * Set up a new ViewLineChangeMarkingModel that is fed mocked values
   *
   * @param pLineNumberColors List of LineNumberColors the LineChangeMarkingModel should return when queried
   * @param pViewRectangles   List of Rectangles that form the mocked return values for the viewPort if queried for its current position
   * @return ViewLineChangeMarkingModel that is set up with its LineChangeMarkingModel and viewPort providing mocked values
   */
  @NonNull
  private static ViewLineChangeMarkingModel getLineChangeMarkingModel(@NonNull List<LineNumberColor> pLineNumberColors, @NonNull List<Rectangle> pViewRectangles)
  {
    LineChangeMarkingModel lineChangeMarkingModel = mock(LineChangeMarkingModel.class);
    when(lineChangeMarkingModel.getStaticLineNumberColors()).thenReturn(pLineNumberColors);

    JViewport viewport = mock(JViewport.class);
    if (pViewRectangles.size() == 1)
      when(viewport.getViewRect()).thenReturn(pViewRectangles.get(0));
    else if (pViewRectangles.size() == 2)
      when(viewport.getViewRect()).thenReturn(pViewRectangles.get(0)).thenReturn(pViewRectangles.get(1));

    ViewLineChangeMarkingModel viewLineChangeMarkingModel = new ViewLineChangeMarkingModel(lineChangeMarkingModel, viewport);
    viewLineChangeMarkingModel.lineNumberColorsChanged(pLineNumberColors);
    return viewLineChangeMarkingModel;
  }
}