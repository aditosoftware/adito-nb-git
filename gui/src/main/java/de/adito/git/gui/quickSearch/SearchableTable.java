package de.adito.git.gui.quickSearch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Table that is supposed to have QuickSearch attached to it. Since QuickSearch cannot attach itself to the table itself, QuickSearch is attached to
 * a JPanel instead, and all KeyEvents from this table are forwarded to the JPanel at which the QuickSearch is attached. The QuickSearch uses
 * the tableModel of this table for matching results
 *
 * @author m.kaspera, 08.02.2019
 */
public class SearchableTable extends JTable
{

  public SearchableTable(JPanel pView)
  {
    this(null, pView);
  }

  /**
   * @param pTableModel          The TableModel for this table
   * @param pView                Component that can accommodate the searchField (if it is added to the table, the field is not shown)
   */
  public SearchableTable(@Nullable TableModel pTableModel, @NotNull JPanel pView)
  {
    super(pTableModel);
    addKeyListener(new _KeyForwardAdapter(pView));
  }

  /**
   * KeyAdapter that forwards all KeyEvents to the Component that the QuickSearch is attached to
   */
  private class _KeyForwardAdapter extends KeyAdapter
  {

    private JPanel receiver;

    _KeyForwardAdapter(JPanel pReceiver)
    {
      receiver = pReceiver;
    }

    @Override
    public void keyTyped(KeyEvent pEvent)
    {
      receiver.dispatchEvent(pEvent);
    }
  }

}
