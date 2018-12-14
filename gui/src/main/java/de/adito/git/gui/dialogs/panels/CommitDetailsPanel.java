package de.adito.git.gui.dialogs.panels;

import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.gui.*;
import de.adito.git.gui.tableModels.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.util.*;

public class CommitDetailsPanel implements IDiscardable
{

  private static final double DETAIL_SPLIT_PANE_RATIO = 0.5;
  private static final String DETAILS_FORMAT_STRING = "%-10s\t%s%s";
  private final JSplitPane detailPanelPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
  private final JTextPane messageTextArea = new JTextPane(new DefaultStyledDocument());
  private final JTable changedFilesTable = new JTable();
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final ChangedFilesTableModel changedFilesTableModel;
  private Disposable disposable;

  public CommitDetailsPanel(Observable<Optional<IRepository>> pRepository, Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    selectedCommitObservable = pSelectedCommitObservable;
    changedFilesTableModel = new ChangedFilesTableModel(selectedCommitObservable, pRepository);
    _setUpChangedFilesTable();
    _initDetailPanel();
  }

  public JComponent getPanel()
  {
    return detailPanelPane;
  }

  private void _setUpChangedFilesTable()
  {
    changedFilesTable.setModel(changedFilesTableModel);
    changedFilesTable.getColumnModel().removeColumn(changedFilesTable.getColumn(StatusTableModel.CHANGE_TYPE_COLUMN_NAME));
    changedFilesTable.getColumnModel()
        .getColumn(changedFilesTableModel.findColumn(ChangedFilesTableModel.FILE_NAME_COLUMN_NAME))
        .setCellRenderer(new FileStatusCellRenderer());
    changedFilesTable.getColumnModel()
        .getColumn(changedFilesTableModel.findColumn(ChangedFilesTableModel.FILE_PATH_COLUMN_NAME))
        .setCellRenderer(new FileStatusCellRenderer());
  }

  /**
   * DetailPanel shows the changed files and the short message, author, commit date and full message of the
   * currently selected commit to the right of the branching window of the commitHistory.
   * If more than one commit is selected, the first selected commit in the list is chosen
   */
  private void _initDetailPanel()
  {
        /*
        ------------------------------
        | -------------------------- |
        | |  Table with scrollPane | |
        | -------------------------- |
        |         SplitPane          |
        | -------------------------- |
        | |  TextArea with detail  | |
        | |  message in scrollPane | |
        | -------------------------- |
        ------------------------------
         */
    disposable = selectedCommitObservable.subscribe(commits -> commits.ifPresent(iCommits -> messageTextArea.setText(_getDescriptionText(iCommits))));
    JScrollPane messageTextScrollPane = new JScrollPane(messageTextArea);
    JScrollPane changedFilesScrollPane = new JScrollPane(changedFilesTable);
    detailPanelPane.setLeftComponent(changedFilesScrollPane);
    detailPanelPane.setRightComponent(messageTextScrollPane);
    detailPanelPane.setResizeWeight(DETAIL_SPLIT_PANE_RATIO);
  }

  private String _getDescriptionText(List<ICommit> pCommits)
  {
    if (pCommits.isEmpty())
      return "";
    return String.format(DETAILS_FORMAT_STRING, "ID:", pCommits.get(0).getId(), "\n")
        + String.format(DETAILS_FORMAT_STRING, "Author:", pCommits.get(0).getAuthor(), "\n")
        + String.format(DETAILS_FORMAT_STRING, "Date:", DateTimeRenderer.asString(pCommits.get(0).getTime()), "\n\n")
        + "Full message:\n"
        + pCommits.get(0).getMessage();
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }
}
