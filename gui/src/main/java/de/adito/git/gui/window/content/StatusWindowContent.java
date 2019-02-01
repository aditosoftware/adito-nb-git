package de.adito.git.gui.window.content;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.FileStatusCellRenderer;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class to display the results of the status command to git (i.e. lists all changes made to the
 * local filesystem in comparison to HEAD)
 *
 * @author m.kaspera 27.09.2018
 */
class StatusWindowContent extends JPanel implements IDiscardable
{

  private final Observable<Optional<IRepository>> repository;
  private final IActionProvider actionProvider;
  private final Observable<Optional<List<IFileChangeType>>> selectionObservable;
  private final JTable statusTable;
  private final Action openFileAction;
  private Disposable disposable;
  private JPopupMenu popupMenu;

  @Inject
  StatusWindowContent(IActionProvider pActionProvider, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    repository = pRepository;
    actionProvider = pActionProvider;
    Observable<Optional<IFileStatus>> status = repository
        .flatMap(pRepo -> pRepo.map(IRepository::getStatus).orElse(Observable.just(Optional.empty())));
    statusTable = new JTable(new StatusTableModel(status));
    statusTable.addMouseListener(new _DoubleClickListener());
    ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(statusTable.getSelectionModel());
    statusTable.setSelectionModel(observableListSelectionModel);
    selectionObservable = Observable.combineLatest(observableListSelectionModel.selectedRows(), status, (pSelected, pStatus) -> {
      if (pSelected == null)
        return Optional.of(Collections.emptyList());
      List<IFileChangeType> uncommittedListCached = pStatus.map(IFileStatus::getUncommitted).orElse(Collections.emptyList());
      return Optional.of(Stream.of(pSelected)
                             .map(uncommittedListCached::get)
                             .collect(Collectors.toList()));
    });
    openFileAction = actionProvider.getOpenFileAction(selectionObservable);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    // Do not show the row with the EChangeTypes, they are represented by the color of the text of the files
    statusTable.getColumnModel().removeColumn(statusTable.getColumn(StatusTableModel.columnNames[2]));
    statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    // CellRenderer so the files are colored according to their EChangeType
    for (int index = 0; index < statusTable.getColumnModel().getColumnCount(); index++)
    {
      statusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
    }

    disposable = repository.subscribe(pRepository -> {
      popupMenu = new JPopupMenu();
      popupMenu.add(actionProvider.getOpenFileAction(selectionObservable));
      popupMenu.addSeparator();
      popupMenu.add(actionProvider.getCommitAction(repository, selectionObservable));
      popupMenu.add(actionProvider.getIgnoreAction(repository, selectionObservable));
      popupMenu.add(actionProvider.getExcludeAction(repository, selectionObservable));
      popupMenu.addSeparator();
      popupMenu.add(actionProvider.getDiffToHeadAction(repository, selectionObservable));
      popupMenu.addSeparator();
      popupMenu.add(actionProvider.getRevertWorkDirAction(repository, selectionObservable));
      popupMenu.addSeparator();
      popupMenu.add(actionProvider.getResolveConflictsAction(repository, selectionObservable));
    });

    statusTable.addMouseListener(new PopupMouseListener(popupMenu));
    JScrollPane tableScrollPane = new JScrollPane(statusTable);
    add(tableScrollPane, BorderLayout.CENTER);
  }

  @Override
  public void discard()
  {
    ((StatusTableModel) statusTable.getModel()).discard();
    disposable.dispose();
  }

  private class _DoubleClickListener extends MouseAdapter
  {
    @Override
    public void mousePressed(MouseEvent pEvent)
    {
      if (pEvent.getClickCount() == 2)
      {
        openFileAction.actionPerformed(null);
      }
    }
  }

}
