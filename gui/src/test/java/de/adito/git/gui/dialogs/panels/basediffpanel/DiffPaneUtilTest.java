package de.adito.git.gui.dialogs.panels.basediffpanel;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author m.kaspera, 24.09.2019
 */
public class DiffPaneUtilTest
{

  /**
   * Make sure that if one model moves, the other moves as well
   */
  @Test
  public void testBridge()
  {
    int startValInitialMoved = 0;
    int startValResultingMoved = 0;
    int targetValue = 50;
    int secondTargetValue = 30;
    BoundedRangeModel intialMovedModel = new DefaultBoundedRangeModel(startValInitialMoved, 0, 0, 100);
    BoundedRangeModel resultingMovedModel = new DefaultBoundedRangeModel(startValResultingMoved, 0, 0, 100);
    IDiffPaneUtil.bridge(List.of(intialMovedModel, resultingMovedModel));
    assertEquals(startValInitialMoved, intialMovedModel.getValue());
    assertEquals(startValResultingMoved, resultingMovedModel.getValue());

    // Test moving the first model
    intialMovedModel.setValue(targetValue);
    assertEquals(targetValue, intialMovedModel.getValue());
    assertEquals(targetValue, resultingMovedModel.getValue());

    // Test moving the second model
    resultingMovedModel.setValue(secondTargetValue);
    assertEquals(secondTargetValue, intialMovedModel.getValue());
    assertEquals(secondTargetValue, resultingMovedModel.getValue());
  }

  /**
   * Make sure the model with the bigger extent moves to max - extent if the other model is moved beyond that value
   */
  @Test
  public void testBridgeSmaller()
  {
    int startValInitialMoved = 0;
    int startValResultingMoved = 0;
    int targetValue = 50;
    BoundedRangeModel intialMovedModel = new DefaultBoundedRangeModel(startValInitialMoved, 50, 0, 100);
    BoundedRangeModel resultingMovedModel = new DefaultBoundedRangeModel(startValResultingMoved, 80, 0, 100);
    IDiffPaneUtil.bridge(List.of(intialMovedModel, resultingMovedModel));
    assertEquals(startValInitialMoved, intialMovedModel.getValue());
    assertEquals(startValResultingMoved, resultingMovedModel.getValue());
    intialMovedModel.setValue(targetValue);
    intialMovedModel.setValue(targetValue);
    assertEquals(targetValue, intialMovedModel.getValue());
    assertEquals(20, resultingMovedModel.getValue());
  }

  /**
   * Make sure the rangeModel with the bigger extend does not move backward too soon
   */
  @Test
  public void testBridgeSmallerBackwards()
  {
    int startValInitialMoved = 40;
    int startValResultingMoved = 20;
    int targetValue = 30;
    BoundedRangeModel intialMovedModel = new DefaultBoundedRangeModel(startValInitialMoved, 50, 0, 100);
    BoundedRangeModel resultingMovedModel = new DefaultBoundedRangeModel(startValResultingMoved, 80, 0, 100);
    IDiffPaneUtil.bridge(List.of(intialMovedModel, resultingMovedModel));
    assertEquals(startValInitialMoved, intialMovedModel.getValue());
    assertEquals(startValResultingMoved, resultingMovedModel.getValue());
    intialMovedModel.setValue(targetValue);
    assertEquals(targetValue, intialMovedModel.getValue());
    assertEquals(startValResultingMoved, resultingMovedModel.getValue());
  }

}
