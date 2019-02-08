package de.adito.git.gui.quickSearch;

import de.adito.git.api.IQuickSearch;
import de.adito.git.api.IQuickSearchProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

/**
 * Table that has QuickSearch attached to it.
 * The search field is displayed in a view, which is why the add/remove/revalidate/repaint methods all point to the view. The view also has to
 * contain this table, else the revalidate/repaint methods have no effect
 *
 * @author m.kaspera, 08.02.2019
 */
public class SearchableTable extends JTable
{

  private final SearchableView view;

  public SearchableTable(@NotNull IQuickSearchProvider pQuickSearchProvider, @NotNull List<Integer> pSearchableColumns, SearchableView pView)
  {
    this(null, pQuickSearchProvider, pSearchableColumns, pView);
  }

  /**
   * @param pTableModel          The TableModel for this table
   * @param pQuickSearchProvider QuickSearchProvider that can attach QuickSearch to this table
   * @param pSearchableColumns   List with the indexes of the columns that should be factored into the search
   * @param pView                Component that can accommodate the searchField (if it is added to the table, the field is not shown)
   */
  public SearchableTable(@Nullable TableModel pTableModel, @NotNull IQuickSearchProvider pQuickSearchProvider,
                         @NotNull List<Integer> pSearchableColumns, SearchableView pView)
  {
    super(pTableModel);
    view = pView;
    IQuickSearch quickSearch = pQuickSearchProvider.attach(this, BorderLayout.SOUTH, new QuickSearchCallbackImpl(this, pSearchableColumns));
    quickSearch.setEnabled(true);
  }

  @Override
  public void add(@NotNull Component pComp, Object pConstraints)
  {
    if (view != null)
      view.add(pComp, pConstraints);
  }

  @Override
  public void remove(Component pComp)
  {
    if (view != null)
      view.remove(pComp);
  }

  @Override
  public void revalidate()
  {
    if (view == null)
      super.revalidate();
    else
      view.revalidate();
  }

  @Override
  public void repaint()
  {
    if (view != null)
      view.repaint();
  }
}
