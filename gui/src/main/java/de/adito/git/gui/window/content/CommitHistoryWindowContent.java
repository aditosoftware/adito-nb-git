package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IQuickSearchProvider;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.quickSearch.QuickSearchCallbackImpl;
import de.adito.git.gui.quickSearch.SearchableTable;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.CommitHistoryTreeListTableModel;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to display all commits
 *
 * @author A.Arnold 01.10.2018
 */
class CommitHistoryWindowContent extends JPanel implements IDiscardable
{

  private static final int SCROLL_SPEED_INCREMENT = 16;
  private static final int BRANCHING_AREA_PREF_WIDTH = 1600;
  private static final int DATE_COL_PREF_WIDTH = 250;
  private static final int AUTHOR_COL_PREF_WIDTH = 160;
  private static final double MAIN_SPLIT_PANE_SIZE_RATIO = 0.75;
  private final CommitDetailsPanel commitDetailsPanel;
  private final JPanel commitTableView = new JPanel(new BorderLayout());
  private final JTable commitTable;
  private final CommitHistoryTreeListTableModel commitTableModel;
  private final JToolBar toolBar = new JToolBar();
  private final IActionProvider actionProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private JPopupMenu commitListPopupMenu = new JPopupMenu();

  /**
   * @param pActionProvider         IActionProvider from which actions can be retrieved
   * @param pRepository             Observable with the Repository of the current project
   * @param pTableModel             TableModel used for the table with all commits. Should already be filled with information about the commits
   * @param pLoadMoreCallback       Runnable that puts addition entries into the tableModel, if any more are available
   * @param pRefreshContentCallBack Runnable that resets the current entries in the tableModel and fills them with the latest values
   */
  @Inject
  CommitHistoryWindowContent(IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider,
                             @Assisted Observable<Optional<IRepository>> pRepository, @Assisted TableModel pTableModel,
                             @Assisted("loadMore") Runnable pLoadMoreCallback,
                             @Assisted("refreshContent") Runnable pRefreshContentCallBack)
  {
    actionProvider = pActionProvider;
    repository = pRepository;
    commitTableModel = (CommitHistoryTreeListTableModel) pTableModel;
    commitTable = new _SearchableCommitTable(commitTableModel, commitTableView);
    List<Integer> searchAbleColumns = new ArrayList<>();
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.BRANCHING_COL_NAME));
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.AUTHOR_COL_NAME));
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.DATE_COL_NAME));
    pQuickSearchProvider.attach(commitTableView, BorderLayout.SOUTH, new QuickSearchCallbackImpl(commitTable, searchAbleColumns));
    ObservableListSelectionModel observableCommitListSelectionModel = new ObservableListSelectionModel(commitTable.getSelectionModel());
    commitTable.setSelectionModel(observableCommitListSelectionModel);
    selectedCommitObservable = observableCommitListSelectionModel.selectedRows().map(selectedRows -> {
      List<ICommit> selectedCommits = new ArrayList<>();
      for (int selectedRow : selectedRows)
      {
        selectedCommits.add(((CommitHistoryTreeListItem) commitTable.getValueAt(selectedRow, 0)).getCommit());
      }
      return Optional.of(selectedCommits);
    });
    commitDetailsPanel = new CommitDetailsPanel(actionProvider, pRepository, selectedCommitObservable);
    _initGUI(pLoadMoreCallback, pRefreshContentCallBack);
  }

  @Override
  public void discard()
  {
    commitDetailsPanel.discard();
  }

  private void _initGUI(Runnable pLoadMoreCallback, Runnable pRefreshContentCallBack)
  {
    setLayout(new BorderLayout());
    _setUpCommitTable();
    _setUpToolbar(pRefreshContentCallBack);

    JScrollPane commitScrollPane = new JScrollPane(commitTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    commitTableView.add(commitScrollPane, BorderLayout.CENTER);
    // Listener on the vertical scrollbar to check if the user has reached the bottom. In that , load the next batch of commits into the list
    commitScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
      // check if the scrollBar is still being dragged
      if (!e.getValueIsAdjusting())
      {
        JScrollBar scrollBar = (JScrollBar) e.getAdjustable();
        int extent = scrollBar.getModel().getExtent();
        int maximum = scrollBar.getModel().getMaximum();
        if (extent + e.getValue() == maximum)
        {
          pLoadMoreCallback.run();
        }
      }
    });
    commitScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED_INCREMENT);
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, commitTableView, commitDetailsPanel.getPanel());
    mainSplitPane.setResizeWeight(MAIN_SPLIT_PANE_SIZE_RATIO);
    add(mainSplitPane, BorderLayout.CENTER);
    add(toolBar, BorderLayout.NORTH);
  }

  private void _setUpToolbar(Runnable pRefreshContentCallBack)
  {
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getRefreshContentAction(pRefreshContentCallBack));
  }

  private void _setUpCommitTable()
  {
    commitTable.setDefaultRenderer(CommitHistoryTreeListItem.class, new CommitHistoryTreeListItemRenderer());
    commitTable.setRowHeight(21);
    commitListPopupMenu.add(actionProvider.getResetAction(repository, selectedCommitObservable));
    commitListPopupMenu.add(actionProvider.getAddTagAction(repository, selectedCommitObservable));
    commitTable.addMouseListener(new PopupMouseListener(commitListPopupMenu));

    // cannot set preferred width of only last columns, so have to set a width for the first one as well
    // since the total width is not know the width for the first one has to be a guess that works for most screens
    // and makes it so the last two columns get approx. the desired space (less if guess is too high, more if guess is too low)
    commitTable.getColumnModel()
        .getColumn(commitTableModel.findColumn(CommitHistoryTreeListTableModel.BRANCHING_COL_NAME))
        .setPreferredWidth(BRANCHING_AREA_PREF_WIDTH);
    commitTable.getColumnModel()
        .getColumn(commitTableModel.findColumn(CommitHistoryTreeListTableModel.DATE_COL_NAME))
        .setPreferredWidth(DATE_COL_PREF_WIDTH);
    commitTable.getColumnModel()
        .getColumn(commitTableModel.findColumn(CommitHistoryTreeListTableModel.AUTHOR_COL_NAME))
        .setPreferredWidth(AUTHOR_COL_PREF_WIDTH);
  }

  /**
   * JTable whose tooltip depends on the component that the mouse is hovering over (so if one cell consists of more than one component, the tooltip
   * of the component that the mouse is over is shown)
   */
  private class _SearchableCommitTable extends SearchableTable
  {

    _SearchableCommitTable(@Nullable TableModel pTableModel, @NotNull JPanel pView)
    {
      super(pTableModel, pView);
    }

    @Override
    public String getToolTipText(@NotNull MouseEvent pEvent)
    {
      Point p = pEvent.getPoint();

      // Locate the renderer under the event location
      int hitColumnIndex = columnAtPoint(p);
      int hitRowIndex = rowAtPoint(p);

      if (hitColumnIndex != -1 && hitRowIndex != -1)
      {
        TableCellRenderer renderer = getCellRenderer(hitRowIndex, hitColumnIndex);
        Component component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex);
        Rectangle cellRect = getCellRect(hitRowIndex, hitColumnIndex, false);
        component.setBounds(cellRect);
        component.validate();
        component.doLayout();
        p.translate(-cellRect.x, -cellRect.y);
        Component comp = component.getComponentAt(p);
        if (comp instanceof JComponent)
        {
          return ((JComponent) comp).getToolTipText();
        }
      }

      // No tip from the renderer get our own tip
      return getToolTipText();

    }
  }

}
