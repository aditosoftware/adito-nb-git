package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.dialogs.panels.CommitDetailsPanel;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.CommitListTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
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
  private final Disposable disposable;
  private String selectedCommitId = "";

  @Inject
  public StashedCommitSelectionDialog(CommitDetailsPanel.IPanelFactory pPanelFactory, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<ICommit> pStashedCommits)
  {
    isValidDescriptor = pIsValidDescriptor;
    stashedCommits = pStashedCommits;
    ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(commitListTable.getSelectionModel());
    Observable<Optional<List<ICommit>>> selectedCommitObservable = observableListSelectionModel.selectedRows().map(pSelectedRows -> {
      if (pSelectedRows.length != 1)
        return Optional.empty();
      else
        return Optional.of(Collections.singletonList(pStashedCommits.get(pSelectedRows[0])));
    });
    disposable = selectedCommitObservable.subscribe(pOptSelectedCommits -> {
      isValidDescriptor.setValid(pOptSelectedCommits.map(pCommits -> !pCommits.isEmpty()).orElse(false));
      selectedCommitId = pOptSelectedCommits.map(pCommits -> pCommits.get(0).getId()).orElse("");
    });
    commitDetailsPanel = pPanelFactory.createCommitDetailsPanel(pRepository, selectedCommitObservable);
    commitListTable.setSelectionModel(observableListSelectionModel);
    commitListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _initGui();
  }

  private void _initGui()
  {
    commitListTable.setModel(new CommitListTableModel(stashedCommits));
    add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(commitListTable), commitDetailsPanel.getPanel()));
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
