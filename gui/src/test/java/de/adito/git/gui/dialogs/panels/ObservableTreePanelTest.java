package de.adito.git.gui.dialogs.panels;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.swing.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link ObservableTreePanel}.
 *
 * @author r.hartinger
 */
class ObservableTreePanelTest
{

  /**
   * Tests the creation of the ObservableTreePanel.
   * Checks for the three components (loading, noLocalChanges, treeScrollPane) and their visibility. Also checks for the label texts of the labels
   */
  @Test
  void testCreation()
  {
    ObservableTreePanel observableTreePanel = new ObservableTreePanel()
    {
      @Override
      protected JTree getTree()
      {
        return null;
      }
    };

    Component actualLoadingPanel = observableTreePanel.treeViewPanel.getComponent(0);
    Component actualNoLocalChangesPanel = observableTreePanel.treeViewPanel.getComponent(1);
    Component actualTreeScrollPanel = observableTreePanel.treeViewPanel.getComponent(2);

    assertAll(() -> assertEquals(3, observableTreePanel.treeViewPanel.getComponentCount(), "componentCount"),
              // check the loading panel
              () -> {
                assertTrue(actualLoadingPanel instanceof JPanel, "loadingPanel is a Panel");
                assertEquals("Loading . . .", ((JLabel) ((JPanel) actualLoadingPanel).getComponent(0)).getText(), "loadingLabel Text");
              },
              () -> assertTrue(actualLoadingPanel.isVisible(), "loadingPanel visible"),
              // check the no local changes Panel
              () -> {
                assertTrue(actualNoLocalChangesPanel instanceof JPanel, "noLocalChanges is a Panel");
                assertEquals("No local changes", ((JLabel) ((JPanel) actualNoLocalChangesPanel).getComponent(0)).getText(), "noChanges Text");
              },
              () -> assertFalse(actualNoLocalChangesPanel.isVisible(), "noChanges not visible"),
              // check the treeScroll Panel
              () -> assertTrue(actualTreeScrollPanel instanceof JScrollPane, "treeScrollPanel is a ScrollPanel"),
              () -> assertTrue(actualTreeScrollPanel.isVisible(), "TreeScrollPanel visible"));
  }

  /**
   * Checks for the visibility change of the noLocalChanges panel.
   *
   * @param pNoLocalChanges the boolean to change the visibility. Depending on the given value, the visibility needs to change.
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void setNoLocalChangesPanelVisible(boolean pNoLocalChanges)
  {
    ObservableTreePanel observableTreePanel = new ObservableTreePanel()
    {
      @Override
      protected JTree getTree()
      {
        return null;
      }
    };

    assertFalse(observableTreePanel.treeViewPanel.getComponent(1).isVisible(), "not visible before calling the set");

    observableTreePanel.setNoLocalChangesPanelVisible(pNoLocalChanges);

    assertEquals(pNoLocalChanges, observableTreePanel.treeViewPanel.getComponent(1).isVisible(), "visible");
  }
}