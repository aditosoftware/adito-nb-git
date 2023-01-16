package de.adito.git.gui.tablemodels;

import de.adito.git.api.data.IMergeDetails;
import de.adito.git.api.data.diff.IMergeData;
import de.adito.git.impl.data.MergeDetailsImpl;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link MergeDiffStatusModel}.
 *
 * @author r.hartinger, 16.01.2023
 */
class MergeDiffStatusModelTest
{

  /**
   * Tests the update of the remaining conflicts label
   */
  @Nested
  class RemainingConflictsLabel
  {

    /**
     * Tests that the label will be updated after removing elements.
     * The change needs to be triggered with {@code mergeDiffStatusModel.getTableModelListeners()[0].tableChanged(null);}.
     */
    @Test
    void shouldUpdate()
    {
      List<IMergeData> mergeData = new ArrayList<>();
      // adding 10 dummy elements
      for (int i = 0; i < 10; i++)
      {
        mergeData.add(Mockito.spy(IMergeData.class));
      }

      IMergeDetails mergeDetails = new MergeDetailsImpl(mergeData, "2022.2", "2022.0");

      JLabel label = new JLabel();


      MergeDiffStatusModel mergeDiffStatusModel = new MergeDiffStatusModel(Observable.just(mergeData), mergeDetails, label);

      assertEquals(1, mergeDiffStatusModel.getTableModelListeners().length, "model has one listener");

      assertEquals("", label.getText(), "text of label after constructing");

      // trigger a change
      mergeDiffStatusModel.getTableModelListeners()[0].tableChanged(null);
      assertEquals("10 remaining conflicts", label.getText(), "text of first trigger with all elements");

      // remove an entry
      mergeData.remove(0);
      mergeDiffStatusModel.getTableModelListeners()[0].tableChanged(null);
      assertEquals("9 remaining conflicts", label.getText(), "text after removing an element");


      // remove three entries
      mergeData.remove(0);
      mergeData.remove(0);
      mergeData.remove(0);
      mergeDiffStatusModel.getTableModelListeners()[0].tableChanged(null);
      assertEquals("6 remaining conflicts", label.getText(), "text after removing an element");
    }
  }


}