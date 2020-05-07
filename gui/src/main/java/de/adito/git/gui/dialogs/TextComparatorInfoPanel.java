package de.adito.git.gui.dialogs;

import de.adito.git.impl.util.GitRawTextComparator;
import de.adito.swing.TableLayoutUtil;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Vector;

/**
 * Panel showing the selected TextComparator. Does not allow changing the selected TextComparator, only displays it
 *
 * @author m.kaspera, 07.05.2020
 */
public class TextComparatorInfoPanel extends JPanel
{
  public TextComparatorInfoPanel()
  {
    super();
    double pref = TableLayout.PREFERRED;
    final double gap = 15;
    double[] cols = {gap, pref, gap, pref};
    double[] rows = {pref};
    setLayout(new TableLayout(cols, rows));
    TableLayoutUtil tlu = new TableLayoutUtil(this);
    JComboBox<GitRawTextComparator> rawTextComparatorBox = TextComparatorInfoPanel.getTextComparatorComboBox();
    tlu.add(3, 0, rawTextComparatorBox);
    tlu.add(1, 0, new JLabel("Whitespace Treatment:"));
  }

  @NotNull
  private static JComboBox<GitRawTextComparator> getTextComparatorComboBox()
  {
    JComboBox<GitRawTextComparator> rawTextComparatorBox = new JComboBox<>(new Vector<>(GitRawTextComparator.INSTANCES));
    rawTextComparatorBox.setSelectedItem(GitRawTextComparator.CURRENT);
    rawTextComparatorBox.setEnabled(false);
    return rawTextComparatorBox;
  }
}
