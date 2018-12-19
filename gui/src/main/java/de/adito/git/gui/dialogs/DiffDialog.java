package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.ColorPicker;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.panels.DiffPanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.DiffTableModel;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;

/**
 * Window that displays the list of changes found during a diff
 *
 * @author m.kaspera 05.10.2018
 */
class DiffDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private static final String ACCEPT_ICON_PATH = Constants.ACCEPT_CHANGE_YOURS_ICON;
  private static final Dimension PANEL_MIN_SIZE = new Dimension(800, 600);
  private static final Dimension PANEL_PREF_SIZE = new Dimension(1600, 900);
  private static final Dimension TABLE_MIN_SIZE = new Dimension(350, 600);
  private static final Dimension TABLE_PREF_SIZE = new Dimension(150, 900);
  private final JTable fileListTable = new JTable();
  private final ObservableListSelectionModel observableListSelectionModel;
  private final IEditorKitProvider editorKitProvider;
  private final IIconLoader iconLoader;
  private final JTextPane notificationArea = new JTextPane();
  private DiffPanel diffPanel;
  private Disposable disposable;
  private List<IFileDiff> diffs;

  @Inject
  public DiffDialog(IIconLoader pIconLoader, IEditorKitProvider pEditorKitProvider, @Assisted List<IFileDiff> pDiffs)
  {
    iconLoader = pIconLoader;
    observableListSelectionModel = new ObservableListSelectionModel(fileListTable.getSelectionModel());
    fileListTable.setSelectionModel(observableListSelectionModel);
    editorKitProvider = pEditorKitProvider;
    this.diffs = pDiffs;
    _initGui();
  }

  /**
   * sets up the GUI
   */
  private void _initGui()
  {
    setLayout(new BorderLayout());
    setMinimumSize(PANEL_MIN_SIZE);
    setPreferredSize(PANEL_PREF_SIZE);

    // Table on which to select which IFileDiff is displayed in the DiffPanel
    fileListTable.setModel(new DiffTableModel(diffs));
    fileListTable.setDefaultRenderer(String.class, new FileStatusCellRenderer());
    fileListTable.getColumnModel().removeColumn(fileListTable.getColumn(StatusTableModel.CHANGE_TYPE_COLUMN_NAME));
    fileListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane fileListTableScrollPane = new JScrollPane(fileListTable);
    fileListTableScrollPane.setMinimumSize(TABLE_MIN_SIZE);
    fileListTableScrollPane.setPreferredSize(TABLE_PREF_SIZE);
    // display the first entry as default
    if (!diffs.isEmpty())
      fileListTable.getSelectionModel().setSelectionInterval(0, 0);
    // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
    Observable<Optional<IFileDiff>> fileDiffObservable = observableListSelectionModel.selectedRows().map(pSelectedRows -> {
      if (pSelectedRows != null && pSelectedRows.length == 1)
        return Optional.of(diffs.get(pSelectedRows[0]));
      else return Optional.empty();
    });

    diffPanel = new DiffPanel(fileDiffObservable, iconLoader.getIcon(ACCEPT_ICON_PATH));

    // notificationArea for information such as identical files (except whitespaces)
    notificationArea.setEnabled(false);
    notificationArea.setForeground(ColorPicker.INFO_TEXT);
    add(notificationArea, BorderLayout.NORTH);
    disposable = fileDiffObservable.subscribe(pFileDiff -> {
      if (pFileDiff.isPresent())
      {
        List<IFileChangeChunk> currentChangeChunks = pFileDiff.get().getFileChanges().getChangeChunks().blockingFirst();
        if (currentChangeChunks.size() == 1 && currentChangeChunks.get(0).getChangeType() == EChangeType.SAME)
          notificationArea.setText("Files do not differ in actual content, trailing whitespaces may be different");
        else
        {
          notificationArea.setText("");
        }
      }
      else
      {
        notificationArea.setText("");
      }
    });
    // add table and DiffPanel to the SplitPane
    JSplitPane diffToListSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, diffPanel, new JScrollPane(fileListTable));
    diffToListSplitPane.setResizeWeight(1);
    add(diffToListSplitPane, BorderLayout.CENTER);
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    diffPanel.discard();
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
}
