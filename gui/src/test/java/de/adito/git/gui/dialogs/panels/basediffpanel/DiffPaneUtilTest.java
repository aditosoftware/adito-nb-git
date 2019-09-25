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
    _assertValues(startValInitialMoved, startValResultingMoved, intialMovedModel, resultingMovedModel);

    // Test moving the first model
    _setAndTest(intialMovedModel, resultingMovedModel, targetValue, targetValue, targetValue);

    // Test moving the second model
    _setAndTest(resultingMovedModel, intialMovedModel, secondTargetValue, secondTargetValue, secondTargetValue);
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
    _assertValues(startValInitialMoved, startValResultingMoved, intialMovedModel, resultingMovedModel);
    intialMovedModel.setValue(targetValue);
    _setAndTest(intialMovedModel, resultingMovedModel, targetValue, targetValue, 20);
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
    _assertValues(startValInitialMoved, startValResultingMoved, intialMovedModel, resultingMovedModel);
    _setAndTest(intialMovedModel, resultingMovedModel, targetValue, targetValue, startValResultingMoved);
  }

  /**
   * Test two different maximum sizes where the first is set to a value bigger than the maximum of the second
   */
  @Test
  public void testBridgeBigger()
  {
    int startValInitialMoved = 40;
    int startValResultingMoved = 40;
    int targetValue = 150;
    BoundedRangeModel intialMovedModel = new DefaultBoundedRangeModel(startValInitialMoved, 50, 0, 200);
    BoundedRangeModel resultingMovedModel = new DefaultBoundedRangeModel(startValResultingMoved, 50, 0, 100);
    IDiffPaneUtil.bridge(List.of(intialMovedModel, resultingMovedModel));
    _assertValues(startValInitialMoved, startValResultingMoved, intialMovedModel, resultingMovedModel);
    intialMovedModel.setValue(targetValue);
    _setAndTest(intialMovedModel, resultingMovedModel, targetValue, targetValue, 50);
  }

  /**
   * Test two different maximum sizes where the first is decreased to a value still bigger than the maximum of the second, the second should not move back in that case
   */
  @Test
  public void testBridgeBiggerBackwards()
  {
    int startValInitialMoved = 150;
    int startValResultingMoved = 50;
    int targetValue = 100;
    BoundedRangeModel intialMovedModel = new DefaultBoundedRangeModel(startValInitialMoved, 50, 0, 200);
    BoundedRangeModel resultingMovedModel = new DefaultBoundedRangeModel(startValResultingMoved, 50, 0, 100);
    IDiffPaneUtil.bridge(List.of(intialMovedModel, resultingMovedModel));
    _assertValues(startValInitialMoved, startValResultingMoved, intialMovedModel, resultingMovedModel);
    _setAndTest(intialMovedModel, resultingMovedModel, targetValue, targetValue, 50);
  }

  /**
   * Assert that the given values are the ones that the models do have
   *
   * @param pValFirstModel       value for the first model
   * @param pValSecondModel      value for the second model
   * @param pFirstModel          first model
   * @param pResultingMovedModel second model
   */
  private void _assertValues(int pValFirstModel, int pValSecondModel, BoundedRangeModel pFirstModel, BoundedRangeModel pResultingMovedModel)
  {
    assertEquals(pValFirstModel, pFirstModel.getValue());
    assertEquals(pValSecondModel, pResultingMovedModel.getValue());
  }

  /**
   * sets pModelToSet to the given targetValue and then checks if both models have the passed expected values
   *
   * @param pModelToSet     model whose value gets set
   * @param pSecondModel    model whose value will be set by the listener of the _bridge
   * @param pTargetValue    value pModelToSet is set to
   * @param pExpectedFirst  expected outcome for pModelToSet
   * @param pExpectedSecond expected outcome for pSecondModel
   */
  private void _setAndTest(BoundedRangeModel pModelToSet, BoundedRangeModel pSecondModel, int pTargetValue, int pExpectedFirst, int pExpectedSecond)
  {
    pModelToSet.setValue(pTargetValue);
    _assertValues(pExpectedFirst, pExpectedSecond, pModelToSet, pSecondModel);
  }

}
