package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.menu.IMenuProvider;
import de.adito.git.gui.quicksearch.QuickSearchCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTable;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tablemodels.CommitHistoryTreeListTableModel;
import de.adito.git.impl.data.CommitFilterImpl;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
  private final IMenuProvider menuProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Runnable loadMoreCallback;
  private final Observable<Optional<List<CommitHistoryTreeListItem>>> selectedCommitHistoryItems;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final Observable<Optional<IFileStatus>> statusObservable;
  private final QuickSearchCallbackImpl quickSearchCallback;
  private JPopupMenu commitListPopupMenu = new JPopupMenu();

  // Variables for filtering the shown entries
  private JTextField authorField = new JTextField();
  private JComboBox<IBranch> branchSelectionBox = new JComboBox<>();
  private final List<File> chosenFiles = new ArrayList<>();
  private Observable<ICommitFilter> commitFilterObs;
  private Disposable commitFilterDisposable;
  private Disposable branchObservable;

  /**
   * @param pActionProvider         IActionProvider from which actions can be retrieved
   * @param pRepository             Observable with the Repository of the current project
   * @param pTableModel             TableModel used for the table with all commits. Should already be filled with information about the commits
   * @param pLoadMoreCallback       Runnable that puts addition entries into the tableModel, if any more are available
   * @param pRefreshContentCallBack Runnable that resets the current entries in the tableModel and fills them with the latest values
   */
  @Inject
  CommitHistoryWindowContent(IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider, IMenuProvider pMenuProvider,
                             CommitDetailsPanel.IPanelFactory pPanelFactory,
                             @Assisted Observable<Optional<IRepository>> pRepository, @Assisted TableModel pTableModel,
                             @Assisted Runnable pLoadMoreCallback, @Assisted Consumer<ICommitFilter> pRefreshContentCallBack, @Assisted ICommitFilter pStartFilter)
  {
    actionProvider = pActionProvider;
    menuProvider = pMenuProvider;
    repository = pRepository;
    loadMoreCallback = pLoadMoreCallback;
    if (pStartFilter.getAuthor() != null) authorField.setText(pStartFilter.getAuthor());
    if (!pStartFilter.getFiles().isEmpty()) chosenFiles.addAll(pStartFilter.getFiles());
    commitTableModel = (CommitHistoryTreeListTableModel) pTableModel;
    commitTable = new _SearchableCommitTable(commitTableModel, commitTableView);
    commitTable.removeColumn(commitTable.getColumn(CommitHistoryTreeListTableModel.COMMIT_ID_COL_NAME));
    List<Integer> searchAbleColumns = new ArrayList<>();
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.BRANCHING_COL_NAME));
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.AUTHOR_COL_NAME));
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.DATE_COL_NAME));
    searchAbleColumns.add(commitTableModel.findColumn(CommitHistoryTreeListTableModel.COMMIT_ID_COL_NAME));
    quickSearchCallback = new QuickSearchCallbackImpl(commitTable, searchAbleColumns);
    pQuickSearchProvider.attach(commitTableView, BorderLayout.SOUTH, quickSearchCallback);
    ObservableListSelectionModel observableCommitListSelectionModel = new ObservableListSelectionModel(commitTable.getSelectionModel());
    commitTable.setSelectionModel(observableCommitListSelectionModel);
    selectedCommitHistoryItems = observableCommitListSelectionModel.selectedRows().map(selectedRows -> {
      List<CommitHistoryTreeListItem> selectedCommits = new ArrayList<>();
      for (int selectedRow : selectedRows)
      {
        selectedCommits.add(((CommitHistoryTreeListItem) commitTable.getValueAt(selectedRow, 0)));
      }
      return Optional.of(selectedCommits);
    });
    branchObservable = repository.switchMap(pOptRepo -> pOptRepo.map(IRepository::getBranches).orElse(Observable.just(Optional.of(List.of()))))
        .subscribe(pOptBranches -> pOptBranches.ifPresent(pIBranches -> _resetSelectableBranches(pStartFilter, pIBranches)));
    selectedCommitObservable = selectedCommitHistoryItems.map(pOptSelectedItems ->
                                                                  pOptSelectedItems
                                                                      .map(pSelectedItems -> pSelectedItems
                                                                          .stream()
                                                                          .map(CommitHistoryTreeListItem::getCommit).collect(Collectors.toList())));
    commitFilterObs = Observable.combineLatest(
        Observable.create(new _ComboBoxObservable(branchSelectionBox))
            .startWith(branchSelectionBox.getSelectedItem() == null ? Optional.empty() : Optional.of((IBranch) branchSelectionBox.getSelectedItem())),
        Observable.create(new _JTextFieldObservable(authorField)).startWith("").debounce(500, TimeUnit.MILLISECONDS),
        (pBranch, pAuthor) -> (ICommitFilter) new CommitFilterImpl()
            .setAuthor(pAuthor.isEmpty() ? null : pAuthor)
            .setBranch(pBranch.orElse(null))
            .setFileList(chosenFiles))
        .share()
        .subscribeWith(BehaviorSubject.create());
    statusObservable = pRepository.switchMap(pOptRepo -> pOptRepo.map(IRepository::getStatus).orElse(Observable.just(Optional.empty())));
    commitDetailsPanel = pPanelFactory.createCommitDetailsPanel(pRepository, selectedCommitObservable, pStartFilter);
    _initGUI(pLoadMoreCallback, pRefreshContentCallBack);
  }

  /**
   * Resets/Reloads the branches that are shown in the branch selection combobox
   *
   * @param pStartFilter filter that can be used to get a branch to pre-select/re-select
   * @param pBranches    list of branches in the repository
   */
  private void _resetSelectableBranches(@Assisted ICommitFilter pStartFilter, List<IBranch> pBranches)
  {
    Object tmpSelectedItem = branchSelectionBox.getSelectedItem();
    if (branchSelectionBox.getItemCount() == 0 && pStartFilter.getBranch() != null)
      tmpSelectedItem = pStartFilter.getBranch();
    branchSelectionBox.removeAllItems();
    branchSelectionBox.addItem(IBranch.ALL_BRANCHES);
    pBranches.forEach(pBranch -> branchSelectionBox.addItem(pBranch));
    branchSelectionBox.setSelectedItem(tmpSelectedItem == null ? IBranch.ALL_BRANCHES : tmpSelectedItem);
  }

  @Override
  public void discard()
  {
    commitFilterDisposable.dispose();
    branchObservable.dispose();
    commitDetailsPanel.discard();
  }

  private void _initGUI(Runnable pLoadMoreCallback, Consumer<ICommitFilter> pRefreshContentCallBack)
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
    JPanel commitTableViewPanel = new JPanel(new BorderLayout());
    commitTableViewPanel.add(commitTableView, BorderLayout.CENTER);
    commitTableViewPanel.add(toolBar, BorderLayout.NORTH);
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, commitTableViewPanel, commitDetailsPanel.getPanel());
    mainSplitPane.setResizeWeight(MAIN_SPLIT_PANE_SIZE_RATIO);
    add(mainSplitPane, BorderLayout.CENTER);
  }

  private void _setUpToolbar(Consumer<ICommitFilter> pRefreshContentCallBack)
  {
    toolBar.setOrientation(JToolBar.HORIZONTAL);
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getRefreshContentAction(() -> pRefreshContentCallBack.accept(commitFilterObs.blockingFirst())));
    toolBar.addSeparator();
    toolBar.add(actionProvider.getCherryPickAction(repository, selectedCommitObservable));
    toolBar.add(actionProvider.getShowTagWindowAction(pCommit -> _selectCommit(pCommit, 0), repository));
    toolBar.addSeparator();
    JLabel branchLabel = new JLabel("Branch");
    branchLabel.setBorder(new EmptyBorder(0, 2, 0, 5));
    JLabel authorLabel = new JLabel("Author");
    authorLabel.setBorder(new EmptyBorder(0, 8, 0, 5));
    toolBar.add(branchLabel);
    branchSelectionBox.setPreferredSize(new Dimension(200, 26));
    toolBar.add(branchSelectionBox);
    toolBar.add(authorLabel);
    authorField.setPreferredSize(new Dimension(400, 26));
    toolBar.add(authorField);
    commitFilterDisposable = Observable.combineLatest(commitFilterObs, statusObservable, (pFilter, pStatus) -> pFilter).subscribe(pRefreshContentCallBack::accept);
  }

  private void _selectCommit(ICommit pCommit, int startIndex)
  {
    if (pCommit != null && startIndex < commitTable.getModel().getRowCount())
    {
      boolean foundCommit = false;
      String searchString = pCommit.getId();
      int commitColumn = ((AbstractTableModel) commitTable.getModel()).findColumn(CommitHistoryTreeListTableModel.COMMIT_ID_COL_NAME);
      for (int index = startIndex; index < commitTable.getModel().getRowCount(); index++)
      {
        if (StringUtils.containsIgnoreCase(commitTable.getModel().getValueAt(index, commitColumn).toString(), searchString))
        {
          commitTable.scrollRectToVisible(commitTable.getCellRect(index, 1, true));
          commitTable.setRowSelectionInterval(index, index);
          foundCommit = true;
          break;
        }
      }
      if (!foundCommit)
      {
        int newStartIndex = commitTable.getModel().getRowCount();
        loadMoreCallback.run();
        _selectCommit(pCommit, newStartIndex);
      }
    }
  }

  private void _setUpCommitTable()
  {
    commitTable.setDefaultRenderer(CommitHistoryTreeListItem.class, new CommitHistoryTreeListItemRenderer());
    commitTable.setRowHeight(21);
    commitListPopupMenu.add(actionProvider.getDiffCommitToHeadAction(repository, selectedCommitObservable, Observable.just(Optional.empty())));
    commitListPopupMenu.add(actionProvider.getResetAction(repository, selectedCommitObservable));
    commitListPopupMenu.add(actionProvider.getAddTagAction(repository, selectedCommitObservable));
    commitListPopupMenu.add(menuProvider.getDeleteTagsMenu("Delete Tag", repository, selectedCommitHistoryItems));
    commitListPopupMenu.add(actionProvider.getCherryPickAction(repository, selectedCommitObservable));
    commitListPopupMenu.add(actionProvider.getNewBranchAction(repository, selectedCommitObservable));
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

  /**
   * Observable that sends the current Text of a TextField each time the text changes
   */
  private static class _JTextFieldObservable extends AbstractListenerObservable<DocumentListener, JTextField, String>
  {

    _JTextFieldObservable(@NotNull JTextField pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected DocumentListener registerListener(@NotNull JTextField pJTextField, @NotNull IFireable<String> pIFireable)
    {
      DocumentListener listener = new DocumentListener()
      {
        @Override
        public void insertUpdate(DocumentEvent e)
        {
          pIFireable.fireValueChanged(pJTextField.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
          pIFireable.fireValueChanged(pJTextField.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
          pIFireable.fireValueChanged(pJTextField.getText());
        }
      };
      pJTextField.getDocument().addDocumentListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull JTextField pJTextField, @NotNull DocumentListener pDocumentListener)
    {
      pJTextField.getDocument().removeDocumentListener(pDocumentListener);
    }
  }

  /**
   * Observable that fires the currently selected Branch (or optional.empty) if the selection of the ComboBox with Branches changes
   */
  private static class _ComboBoxObservable extends AbstractListenerObservable<ItemListener, JComboBox<IBranch>, Optional<IBranch>>
  {

    _ComboBoxObservable(@NotNull JComboBox<IBranch> pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected ItemListener registerListener(@NotNull JComboBox<IBranch> pIBranchJComboBox, @NotNull IFireable<Optional<IBranch>> pIFireable)
    {
      ItemListener listener = e -> {
        if (e.getStateChange() == ItemEvent.SELECTED)
          pIFireable.fireValueChanged(Optional.of((IBranch) e.getItem()));
      };
      pIBranchJComboBox.addItemListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull JComboBox<IBranch> pIBranchJComboBox, @NotNull ItemListener pItemListener)
    {
      pIBranchJComboBox.removeItemListener(pItemListener);
    }
  }

}
