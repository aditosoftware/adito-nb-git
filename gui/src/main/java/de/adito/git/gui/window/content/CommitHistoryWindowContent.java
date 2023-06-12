package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.menu.IMenuProvider;
import de.adito.git.gui.quicksearch.*;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.swing.SimpleBranchNameListCellRenderer;
import de.adito.git.gui.tablemodels.CommitHistoryTreeListTableModel;
import de.adito.git.impl.data.CommitFilterImpl;
import de.adito.util.reactive.AbstractListenerObservable;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
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
  private final QuickSearchCallbackImpl quickSearchCallback;
  private final List<IDiscardable> popupDiscardables = new ArrayList<>();
  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final ObservableListSelectionModel observableCommitListSelectionModel;
  private final JPopupMenu commitListPopupMenu = new JPopupMenu();

  // Variables for filtering the shown entries
  private final JTextField authorField = new JTextField();
  private final JComboBox<IBranch> branchSelectionBox = new JComboBox<>();
  private final List<File> chosenFiles = new ArrayList<>();
  private final Disposable branchObservable;

  /**
   * @param pActionProvider         IActionProvider from which actions can be retrieved
   * @param pRepository             Observable with the Repository of the current project
   * @param pTableModel             TableModel used for the table with all commits. Should already be filled with information about the commits
   * @param pLoadMoreCallback       Runnable that puts addition entries into the tableModel, if any more are available
   * @param pRefreshContentCallBack Runnable that resets the current entries in the tableModel and fills them with the latest values
   */
  @Inject
  CommitHistoryWindowContent(IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider, IMenuProvider pMenuProvider,
                             CommitDetailsPanel.ICommitDetailsPanelFactory pPanelFactory, IIconLoader pIconLoader,
                             @Assisted Observable<Optional<IRepository>> pRepository, @Assisted TableModel pTableModel,
                             @Assisted Runnable pLoadMoreCallback, @Assisted Consumer<ICommitFilter> pRefreshContentCallBack, @Assisted ICommitFilter pStartFilter)
  {
    actionProvider = pActionProvider;
    menuProvider = pMenuProvider;
    repository = pRepository;
    loadMoreCallback = pLoadMoreCallback;
    disposables.add(new ObservableCacheDisposable(observableCache));
    if (pStartFilter.getAuthor() != null) authorField.setText(pStartFilter.getAuthor());
    if (!pStartFilter.getFiles().isEmpty()) chosenFiles.addAll(pStartFilter.getFiles());
    branchSelectionBox.setRenderer(new SimpleBranchNameListCellRenderer());
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
    observableCommitListSelectionModel = new ObservableListSelectionModel(commitTable.getSelectionModel());
    commitTable.setSelectionModel(observableCommitListSelectionModel);
    branchObservable = repository
        .switchMap(pOptRepo -> pOptRepo
            .map(IRepository::getBranches)
            .orElse(Observable.just(Optional.of(List.of()))))
        .subscribe(pOptBranches -> pOptBranches
            .ifPresent(pIBranches -> _resetSelectableBranches(pStartFilter, pIBranches)));
    commitDetailsPanel = pPanelFactory.createCommitDetailsPanel(pRepository, _observeSelectedCommits(), pStartFilter);
    _initGUI(pLoadMoreCallback, pRefreshContentCallBack, pIconLoader);
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
    branchSelectionBox.addItem(IBranch.HEAD);
    pBranches.forEach(branchSelectionBox::addItem);
    branchSelectionBox.setSelectedItem(tmpSelectedItem == null ? IBranch.ALL_BRANCHES : tmpSelectedItem);
  }

  @Override
  public void discard()
  {
    branchObservable.dispose();
    commitDetailsPanel.discard();
    popupDiscardables.forEach(IDiscardable::discard);
    observableCommitListSelectionModel.discard();
    disposables.clear();
  }

  private void _initGUI(Runnable pLoadMoreCallback, Consumer<ICommitFilter> pRefreshContentCallBack, IIconLoader pIconLoader)
  {
    setLayout(new BorderLayout());
    _setUpCommitTable(pIconLoader);
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
    toolBar.add(actionProvider.getRefreshContentAction(() -> pRefreshContentCallBack.accept(_observeCommitFilter().blockingFirst())));
    toolBar.addSeparator();
    toolBar.add(actionProvider.getCherryPickAction(repository, _observeSelectedCommits()));
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
    disposables.add(Observable.combineLatest(_observeCommitFilter(), _observeStatus(), (pFilter, pStatus) -> pFilter).subscribe(pRefreshContentCallBack::accept));
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

  private void _setUpCommitTable(IIconLoader pIconLoader)
  {
    commitTable.setDefaultRenderer(CommitHistoryTreeListItem.class, new CommitHistoryTreeListItemRenderer(pIconLoader));
    commitTable.setRowHeight(21);
    _buildPopupMenu();

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

  private void _buildPopupMenu()
  {
    Action diffCommitToHeadAction = actionProvider.getDiffCommitToHeadAction(repository, _observeSelectedCommits(), Observable.just(Optional.empty()));
    Action checkoutCommitAction = actionProvider.getCheckoutCommitAction(repository, _observeSelectedCommits());
    Action resetAction = actionProvider.getResetAction(repository, _observeSelectedCommits());
    Action addTagAction = actionProvider.getAddTagAction(repository, _observeSelectedCommits());
    JMenu deleteTagsMenu = menuProvider.getDeleteTagsMenu("Delete Tag", repository, _observeSelectedCommitHistoryItems());
    Action cherryPickAction = actionProvider.getCherryPickAction(repository, _observeSelectedCommits());
    Action newBranchAction = actionProvider.getNewBranchAction(repository, _observeSelectedCommits());
    Action revertCommitsAction = actionProvider.getRevertCommitsAction(repository, _observeSelectedCommits());
    popupDiscardables.add((IDiscardable) diffCommitToHeadAction);
    popupDiscardables.add((IDiscardable) resetAction);
    popupDiscardables.add((IDiscardable) addTagAction);
    popupDiscardables.add((IDiscardable) deleteTagsMenu);
    popupDiscardables.add((IDiscardable) cherryPickAction);
    popupDiscardables.add((IDiscardable) newBranchAction);
    popupDiscardables.add((IDiscardable) checkoutCommitAction);
    popupDiscardables.add((IDiscardable) revertCommitsAction);
    commitListPopupMenu.add(diffCommitToHeadAction);
    commitListPopupMenu.addSeparator();
    commitListPopupMenu.add(addTagAction);
    commitListPopupMenu.add(deleteTagsMenu);
    commitListPopupMenu.addSeparator();
    commitListPopupMenu.add(checkoutCommitAction);
    commitListPopupMenu.add(cherryPickAction);
    commitListPopupMenu.add(newBranchAction);
    commitListPopupMenu.addSeparator();
    commitListPopupMenu.add(resetAction);
    commitListPopupMenu.add(revertCommitsAction);
    commitTable.addMouseListener(new PopupMouseListener(() -> commitListPopupMenu));
  }

  @NonNull
  private Observable<Optional<IFileStatus>> _observeStatus()
  {
    return observableCache.calculateParallel("status", () -> repository
        .switchMap(pOptRepo -> pOptRepo.map(IRepository::getStatus).orElse(Observable.just(Optional.empty())))
        .debounce(500, TimeUnit.MILLISECONDS));
  }

  @NonNull
  private Observable<ICommitFilter> _observeCommitFilter()
  {
    return observableCache.calculateParallel("commitFilter", () -> Observable.combineLatest(
        Observable.create(new _ComboBoxObservable(branchSelectionBox))
            .startWithItem(branchSelectionBox.getSelectedItem() == null ? Optional.empty() : Optional.of((IBranch) branchSelectionBox.getSelectedItem())),
        Observable.create(new _JTextFieldObservable(authorField))
            .startWithItem("")
            .debounce(500, TimeUnit.MILLISECONDS),
        (pBranch, pAuthor) -> new CommitFilterImpl()
            .setAuthor(pAuthor.isEmpty() ? null : pAuthor)
            .setBranch(pBranch.orElse(null))
            .setFileList(chosenFiles)));
  }

  @NonNull
  private Observable<Optional<List<ICommit>>> _observeSelectedCommits()
  {
    return observableCache.calculateParallel("selectedCommits", () -> _observeSelectedCommitHistoryItems()
        .map(pOptSelectedItems -> pOptSelectedItems
            .map(pSelectedItems -> pSelectedItems
                .stream()
                .map(CommitHistoryTreeListItem::getCommit).collect(Collectors.toList()))));
  }

  @NonNull
  private Observable<Optional<List<CommitHistoryTreeListItem>>> _observeSelectedCommitHistoryItems()
  {
    return observableCache.calculateParallel("selectedCommitHistoryItems", () -> observableCommitListSelectionModel.selectedRows()
        .map(selectedRows -> {
          List<CommitHistoryTreeListItem> selectedCommits = new ArrayList<>();
          for (int selectedRow : selectedRows)
          {
            selectedCommits.add(((CommitHistoryTreeListItem) commitTable.getValueAt(selectedRow, 0)));
          }
          return Optional.of(selectedCommits);
        }));
  }

  /**
   * JTable whose tooltip depends on the component that the mouse is hovering over (so if one cell consists of more than one component, the tooltip
   * of the component that the mouse is over is shown)
   */
  private static class _SearchableCommitTable extends SearchableTable
  {

    _SearchableCommitTable(@Nullable TableModel pTableModel, @NonNull JPanel pView)
    {
      super(pTableModel, pView);
    }

    @Override
    public String getToolTipText(@NonNull MouseEvent pEvent)
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

    _JTextFieldObservable(@NonNull JTextField pListenableValue)
    {
      super(pListenableValue);
    }

    @NonNull
    @Override
    protected DocumentListener registerListener(@NonNull JTextField pJTextField, @NonNull IFireable<String> pIFireable)
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
    protected void removeListener(@NonNull JTextField pJTextField, @NonNull DocumentListener pDocumentListener)
    {
      pJTextField.getDocument().removeDocumentListener(pDocumentListener);
    }
  }

  /**
   * Observable that fires the currently selected Branch (or optional.empty) if the selection of the ComboBox with Branches changes
   */
  private static class _ComboBoxObservable extends AbstractListenerObservable<ItemListener, JComboBox<IBranch>, Optional<IBranch>>
  {

    _ComboBoxObservable(@NonNull JComboBox<IBranch> pListenableValue)
    {
      super(pListenableValue);
    }

    @NonNull
    @Override
    protected ItemListener registerListener(@NonNull JComboBox<IBranch> pIBranchJComboBox, @NonNull IFireable<Optional<IBranch>> pIFireable)
    {
      ItemListener listener = e -> {
        if (e.getStateChange() == ItemEvent.SELECTED)
          pIFireable.fireValueChanged(Optional.of((IBranch) e.getItem()));
      };
      pIBranchJComboBox.addItemListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NonNull JComboBox<IBranch> pIBranchJComboBox, @NonNull ItemListener pItemListener)
    {
      pIBranchJComboBox.removeItemListener(pItemListener);
    }
  }

}
