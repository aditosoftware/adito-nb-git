package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

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
    LineChangeMarkingModel lineChangeMarkingModel = mock(LineChangeMarkingModel.class);
    List<LineNumberColor> lineNumberColors = List.of();
    when(lineChangeMarkingModel.getStaticLineNumberColors()).thenReturn(lineNumberColors);
    JViewport viewport = mock(JViewport.class);
    when(viewport.getViewRect()).thenReturn(new Rectangle());
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = new ViewLineChangeMarkingModel(lineChangeMarkingModel, viewport);
    viewLineChangeMarkingModel.lineNumberColorsChanged(lineNumberColors);
    assertEquals(List.of(), viewLineChangeMarkingModel.getLineNumberColors());
  }

  /**
   * Test if the returned LineNumberColor is correctly offset if the viewPort has been scrolled down
   */
  @Test
  void isValueCorrectWithOffsetViewArea()
  {
    LineChangeMarkingModel lineChangeMarkingModel = mock(LineChangeMarkingModel.class);
    List<LineNumberColor> lineNumberColors = List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17)));
    when(lineChangeMarkingModel.getStaticLineNumberColors()).thenReturn(lineNumberColors);
    JViewport viewport = mock(JViewport.class);
    when(viewport.getViewRect()).thenReturn(new Rectangle(0, 40, 900, 800));
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = new ViewLineChangeMarkingModel(lineChangeMarkingModel, viewport);
    viewLineChangeMarkingModel.lineNumberColorsChanged(lineNumberColors);
    assertEquals(1, viewLineChangeMarkingModel.getLineNumberColors().size());
    assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y);
  }

  /**
   * Test if the model updates after a scroll event and the LineNumberColor is offset correctly after that change
   */
  @Test
  void isValueAdjustedAfterScroll()
  {
    LineChangeMarkingModel lineChangeMarkingModel = mock(LineChangeMarkingModel.class);
    List<LineNumberColor> lineNumberColors = List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17)));
    when(lineChangeMarkingModel.getStaticLineNumberColors()).thenReturn(lineNumberColors);
    JViewport viewport = mock(JViewport.class);
    when(viewport.getViewRect()).thenReturn(new Rectangle(0, 0, 900, 800)).thenReturn(new Rectangle(0, 40, 900, 800));
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = new ViewLineChangeMarkingModel(lineChangeMarkingModel, viewport);
    viewLineChangeMarkingModel.lineNumberColorsChanged(lineNumberColors);
    assertEquals(1, viewLineChangeMarkingModel.getLineNumberColors().size());
    assertEquals(50, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y);
    // trigger change event, the model should ask for the rectangle area again and receive the second value -> returned lineNumberColors should move
    viewLineChangeMarkingModel.stateChanged(new ChangeEvent(new Object()));
    assertEquals(1, viewLineChangeMarkingModel.getLineNumberColors().size());
    assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y);
  }

  /**
   * Test if the returned LineNumberColors are correctly offset if the viewPort has been scrolled down
   */
  @Test
  void isValuesCorrectWithOffsetViewArea()
  {
    LineChangeMarkingModel lineChangeMarkingModel = mock(LineChangeMarkingModel.class);
    List<LineNumberColor> lineNumberColors = List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17)),
                                                     new LineNumberColor(Color.GREEN, new Rectangle(0, 123, 10, 34)));
    when(lineChangeMarkingModel.getStaticLineNumberColors()).thenReturn(lineNumberColors);
    JViewport viewport = mock(JViewport.class);
    when(viewport.getViewRect()).thenReturn(new Rectangle(0, 40, 900, 800));
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = new ViewLineChangeMarkingModel(lineChangeMarkingModel, viewport);
    viewLineChangeMarkingModel.lineNumberColorsChanged(lineNumberColors);
    assertEquals(2, viewLineChangeMarkingModel.getLineNumberColors().size());
    assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y);
    assertEquals(83, viewLineChangeMarkingModel.getLineNumberColors().get(1).getColoredArea().y);
  }

  /**
   * Test if the model updates after a scroll event and the returned LineNumberColors are offset correctly after that change
   */
  @Test
  void isValuesAdjustedAfterScroll()
  {
    LineChangeMarkingModel lineChangeMarkingModel = mock(LineChangeMarkingModel.class);
    List<LineNumberColor> lineNumberColors = List.of(new LineNumberColor(Color.RED, new Rectangle(0, 50, 10, 17)),
                                                     new LineNumberColor(Color.GREEN, new Rectangle(0, 123, 10, 34)));
    when(lineChangeMarkingModel.getStaticLineNumberColors()).thenReturn(lineNumberColors);
    JViewport viewport = mock(JViewport.class);
    when(viewport.getViewRect()).thenReturn(new Rectangle(0, 0, 900, 800)).thenReturn(new Rectangle(0, 40, 900, 800));
    ViewLineChangeMarkingModel viewLineChangeMarkingModel = new ViewLineChangeMarkingModel(lineChangeMarkingModel, viewport);
    viewLineChangeMarkingModel.lineNumberColorsChanged(lineNumberColors);
    assertEquals(2, viewLineChangeMarkingModel.getLineNumberColors().size());
    assertEquals(50, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y);
    assertEquals(123, viewLineChangeMarkingModel.getLineNumberColors().get(1).getColoredArea().y);
    // trigger change event, the model should ask for the rectangle area again and receive the second value -> returned lineNumberColors should move
    viewLineChangeMarkingModel.stateChanged(new ChangeEvent(new Object()));
    assertEquals(2, viewLineChangeMarkingModel.getLineNumberColors().size());
    assertEquals(10, viewLineChangeMarkingModel.getLineNumberColors().get(0).getColoredArea().y);
    assertEquals(83, viewLineChangeMarkingModel.getLineNumberColors().get(1).getColoredArea().y);
  }
}