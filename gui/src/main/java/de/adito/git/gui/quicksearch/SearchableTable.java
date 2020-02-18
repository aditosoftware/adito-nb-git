package de.adito.git.gui.quicksearch;

import de.adito.git.api.IDiscardable;
import de.adito.swing.KeyForwardAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * Table that is supposed to have QuickSearch attached to it. Since QuickSearch cannot attach itself to the table itself, QuickSearch is attached to
 * a JPanel instead, and all KeyEvents from this table are forwarded to the JPanel at which the QuickSearch is attached. The QuickSearch uses
 * the tableModel of this table for matching results
 *
 * @author m.kaspera, 08.02.2019
 */
public class SearchableTable extends JTable implements IDiscardable
{

  private KeyForwardAdapter keyForwardAdapter;

  public SearchableTable(JPanel pView)
  {
    this(null, pView);
  }

  /**
   * @param pTableModel The TableModel for this table
   * @param pView       Component that can accommodate the searchField (if it is added to the table, the field is not shown)
   */
  public SearchableTable(@Nullable TableModel pTableModel, @NotNull JPanel pView)
  {
    super(pTableModel);
    keyForwardAdapter = new KeyForwardAdapter(pView);
    addKeyListener(keyForwardAdapter);
  }

  @Override
  public void discard()
  {
    removeKeyListener(keyForwardAdapter);
  }
}
