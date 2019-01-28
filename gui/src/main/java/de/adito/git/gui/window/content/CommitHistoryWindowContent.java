package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.CommitHistoryTreeListItem;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.CommitHistoryTreeListTableModel;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
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
  private final JTable commitTable = new JTable();
  private final IActionProvider actionProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private JPopupMenu commitListPopupMenu = new JPopupMenu();

  @Inject
  CommitHistoryWindowContent(IActionProvider pActionProvider, @Assisted Observable<Optional<IRepository>> pRepository,
                             @Assisted TableModel pTableModel, @Assisted Runnable pLoadMoreCallback)
  {
    ObservableListSelectionModel observableCommitListSelectionModel = new ObservableListSelectionModel(commitTable.getSelectionModel());
    commitTable.setSelectionModel(observableCommitListSelectionModel);
    actionProvider = pActionProvider;
    repository = pRepository;
    commitTable.setModel(pTableModel);
    selectedCommitObservable = observableCommitListSelectionModel.selectedRows().map(selectedRows -> {
      List<ICommit> selectedCommits = new ArrayList<>();
      for (int selectedRow : selectedRows)
      {
        selectedCommits.add(((CommitHistoryTreeListItem) commitTable.getValueAt(selectedRow, 0)).getCommit());
      }
      return Optional.of(selectedCommits);
    });
    commitDetailsPanel = new CommitDetailsPanel(pRepository, selectedCommitObservable);
    _initGUI(pLoadMoreCallback);
  }

  @Override
  public void discard()
  {
    commitDetailsPanel.discard();
  }

  private void _initGUI(Runnable pLoadMoreCallback)
  {
    setLayout(new BorderLayout());
    _setUpCommitTable();

    JScrollPane commitScrollPane = new JScrollPane(commitTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, commitScrollPane, commitDetailsPanel.getPanel());
    mainSplitPane.setResizeWeight(MAIN_SPLIT_PANE_SIZE_RATIO);
    add(mainSplitPane);
  }

  private void _setUpCommitTable()
  {
    commitTable.setDefaultRenderer(CommitHistoryTreeListItem.class, new CommitHistoryTreeListItemRenderer());
    commitTable.setRowHeight(21);
    commitListPopupMenu.add(actionProvider.getResetAction(repository, selectedCommitObservable));
    commitTable.addMouseListener(new PopupMouseListener(commitListPopupMenu));

    // cannot set preferred width of only last columns, so have to set a width for the first one as well
    // since the total width is not know the width for the first one has to be a guess that works for most screens
    // and makes it so the last two columns get approx. the desired space (less if guess is too high, more if guess is too low)
    commitTable.getColumnModel()
        .getColumn(CommitHistoryTreeListTableModel.getColumnIndex(CommitHistoryTreeListTableModel.BRANCHING_COL_NAME))
        .setPreferredWidth(BRANCHING_AREA_PREF_WIDTH);
    commitTable.getColumnModel()
        .getColumn(CommitHistoryTreeListTableModel.getColumnIndex(CommitHistoryTreeListTableModel.DATE_COL_NAME))
        .setPreferredWidth(DATE_COL_PREF_WIDTH);
    commitTable.getColumnModel()
        .getColumn(CommitHistoryTreeListTableModel.getColumnIndex(CommitHistoryTreeListTableModel.AUTHOR_COL_NAME))
        .setPreferredWidth(AUTHOR_COL_PREF_WIDTH);
  }

}
