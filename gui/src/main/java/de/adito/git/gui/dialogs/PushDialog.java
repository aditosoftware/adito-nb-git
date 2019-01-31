package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.CommitListTableModel;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class PushDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private final JTable commitListTable = new JTable();
  private final JScrollPane commitListTableScrollP = new JScrollPane(commitListTable);
  private final CommitDetailsPanel commitDetailsPanel;
  private final JLabel headerLabel = new JLabel("Commits to be pushed:");

  @Inject
  public PushDialog(@Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<ICommit> pCommitList)
  {
    ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(commitListTable.getSelectionModel());
    commitListTable.setSelectionModel(observableListSelectionModel);
    commitListTable.setModel(new CommitListTableModel(pCommitList));
    commitListTable.getColumnModel().removeColumn(commitListTable.getColumnModel()
                                                      .getColumn(((AbstractTableModel) commitListTable.getModel())
                                                                     .findColumn(CommitListTableModel.COMMIT_OBJ_COL_NAME)));
    Observable<Optional<List<ICommit>>> selectedCommitObservable = observableListSelectionModel.selectedRows().map(pSelectedRows -> {
      List<ICommit> selectedCommits = new ArrayList<>();
      for (int selectedRow : pSelectedRows)
      {
        selectedCommits.add((ICommit) (commitListTable.getModel().getValueAt(selectedRow, 0)));
      }
      return Optional.of(selectedCommits);
    });
    commitDetailsPanel = new CommitDetailsPanel(pRepository, selectedCommitObservable);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    add(headerLabel, BorderLayout.NORTH);
    add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, commitListTableScrollP, commitDetailsPanel.getPanel()), BorderLayout.CENTER);
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public Object getInformation()
  {
    return null;
  }

  @Override
  public void discard()
  {
    commitDetailsPanel.discard();
  }
}
