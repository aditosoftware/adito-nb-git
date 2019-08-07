package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.icon.IIconLoader;
import de.adito.git.gui.Constants;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tablemodels.CommitListTableModel;
import de.adito.git.impl.data.CommitFilterImpl;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Dialog for showing the user all stashed commits so he can pick one (single selection only)
 *
 * @author m.kaspera, 14.12.2018
 */
class StashedCommitSelectionDialog extends AditoBaseDialog<String> implements IDiscardable
{

  private final IDialogDisplayer.IDescriptor isValidDescriptor;
  private final List<ICommit> stashedCommits;
  private final JTable commitListTable = new JTable();
  private final CommitDetailsPanel commitDetailsPanel;
  private final JButton deleteButton;
  private final Action deleteStashCommitAction;
  private final Disposable disposable;
  private final BehaviorSubject<Optional<String>> selectedCommit = BehaviorSubject.createDefault(Optional.empty());
  private String selectedCommitId = "";

  @Inject
  public StashedCommitSelectionDialog(CommitDetailsPanel.IPanelFactory pPanelFactory, IActionProvider pActionProvider, IIconLoader pIconLoader,
                                      @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<ICommit> pStashedCommits)
  {
    isValidDescriptor = pIsValidDescriptor;
    stashedCommits = pStashedCommits;
    deleteButton = new JButton(pIconLoader.getIcon(Constants.DELETE_ICON));
    ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(commitListTable.getSelectionModel());
    Observable<Optional<List<ICommit>>> selectedCommitObservable = observableListSelectionModel.selectedRows().map(pSelectedRows -> {
      if (pSelectedRows.length != 1 || pSelectedRows[0] >= stashedCommits.size())
        return Optional.empty();
      else
        return Optional.of(Collections.singletonList(pStashedCommits.get(pSelectedRows[0])));
    });
    disposable = selectedCommitObservable.subscribe(pOptSelectedCommits -> {
      isValidDescriptor.setValid(pOptSelectedCommits.map(pCommits -> !pCommits.isEmpty()).orElse(false));
      deleteButton.setEnabled(pOptSelectedCommits.map(pCommits -> !pCommits.isEmpty()).orElse(false));
      selectedCommitId = pOptSelectedCommits.map(pCommits -> pCommits.get(0).getId()).orElse("");
    });
    deleteStashCommitAction = pActionProvider.getDeleteStashedCommitAction(pRepository, selectedCommit);
    commitDetailsPanel = pPanelFactory.createCommitDetailsPanel(pRepository, selectedCommitObservable, new CommitFilterImpl());
    commitListTable.setSelectionModel(observableListSelectionModel);
    commitListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    commitListTable.setModel(new CommitListTableModel(stashedCommits));
    commitListTable.getColumnModel().removeColumn(commitListTable.getColumnModel()
                                                      .getColumn(((AbstractTableModel) commitListTable.getModel())
                                                                     .findColumn(CommitListTableModel.COMMIT_OBJ_COL_NAME)));
    JScrollPane commitListScrollPane = new JScrollPane(commitListTable);
    JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
    toolBar.setFloatable(false);
    toolBar.add(deleteButton);
    add(toolBar, BorderLayout.WEST);
    deleteButton.addActionListener(e -> {
      // since mapping the selectedCommitObservable could lead to deleting a wrong stash (remove from list -> selection changes ->
      // -> observable is asked for value -> value is too recent) the selectedCommit subject basically caches the stashId for the stash to delete
      selectedCommit.onNext(Optional.of(selectedCommitId));
      deleteStashCommitAction.actionPerformed(null);
      stashedCommits.remove(commitListTable.getSelectionModel().getMinSelectionIndex());
      ((AbstractTableModel) commitListTable.getModel()).fireTableDataChanged();
    });
    add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, commitListScrollPane, commitDetailsPanel.getPanel()), BorderLayout.CENTER);
  }


  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public String getInformation()
  {
    return selectedCommitId;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    commitDetailsPanel.discard();
  }
}
