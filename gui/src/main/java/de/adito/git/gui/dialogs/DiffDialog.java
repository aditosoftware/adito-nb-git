package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.ColorPicker;
import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tablemodels.DiffTableModel;
import de.adito.git.gui.tablemodels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
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
  private final boolean acceptChange;
  private final boolean showFileTable;
  private final JTextPane notificationArea = new JTextPane();
  private DiffPanel diffPanel;
  private Disposable disposable;
  private List<IFileDiff> diffs;

  @Inject
  public DiffDialog(IIconLoader pIconLoader, IEditorKitProvider pEditorKitProvider, @Assisted List<IFileDiff> pDiffs,
                    @Assisted @javax.annotation.Nullable String pSelectedFile,
                    @Assisted("acceptChange") boolean pAcceptChange, @Assisted("showFileTable") boolean pShowFileTable)
  {
    iconLoader = pIconLoader;
    acceptChange = pAcceptChange;
    showFileTable = pShowFileTable;
    observableListSelectionModel = new ObservableListSelectionModel(fileListTable.getSelectionModel());
    fileListTable.setSelectionModel(observableListSelectionModel);
    editorKitProvider = pEditorKitProvider;
    diffs = pDiffs;
    _initGui(pSelectedFile);
  }

  /**
   * sets up the GUI
   */
  private void _initGui(@Nullable String pSelectedFile)
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
      _setSelectedFile(pSelectedFile);
    // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
    Observable<Optional<IFileDiff>> fileDiffObservable = observableListSelectionModel.selectedRows().map(pSelectedRows -> {
      if (pSelectedRows != null && pSelectedRows.length == 1)
        return Optional.of(diffs.get(pSelectedRows[0]));
      else return Optional.empty();
    });
    Observable<EditorKit> editorKitObservable = fileDiffObservable
        .map(pFileDiff -> editorKitProvider.getEditorKit(pFileDiff.map(IFileDiff::getFilePath).orElse("text/plain")));

    diffPanel = new DiffPanel(fileDiffObservable, acceptChange ? iconLoader.getIcon(ACCEPT_ICON_PATH) : null, editorKitObservable);

    // notificationArea for information such as identical files (except whitespaces)
    notificationArea.setEnabled(false);
    notificationArea.setForeground(ColorPicker.INFO_TEXT);
    disposable = fileDiffObservable.subscribe(pFileDiff -> {
      if (pFileDiff.isPresent())
      {
        _setNotificationArea(pFileDiff.get());
      }
      else
      {
        notificationArea.setText("");
      }
    });
    if (diffs.size() > 1 && showFileTable)
    {
      // add table and DiffPanel to the SplitPane
      JSplitPane diffToListSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, diffPanel, fileListTableScrollPane);
      diffToListSplitPane.setResizeWeight(1);
      add(diffToListSplitPane, BorderLayout.CENTER);
    }
    else
    {
      diffPanel.setBorder(null);
      add(diffPanel, BorderLayout.CENTER);
    }
  }

  private void _setSelectedFile(@Nullable String pSelectedFile)
  {
    fileListTable.getSelectionModel().setSelectionInterval(0, 0);
    if (pSelectedFile != null)
    {
      for (int index = 0; index < diffs.size(); index++)
      {
        if (new File(diffs.get(index).getFilePath()).equals(new File(pSelectedFile)))
          fileListTable.getSelectionModel().setSelectionInterval(index, index);
      }
    }
  }

  /**
   * sets the text in the notificationArea according to the current status of the IFileDiff
   *
   * @param pFileDiff current IFileDiff
   */
  private void _setNotificationArea(IFileDiff pFileDiff)
  {
    List<IFileChangeChunk> currentChangeChunks = pFileDiff.getFileChanges().getChangeChunks().blockingFirst().getNewValue();
    if ((currentChangeChunks.size() == 1 && currentChangeChunks.get(0).getChangeType() == EChangeType.SAME)
        || currentChangeChunks.stream().allMatch(pChunk -> pChunk.getChangeType() == EChangeType.SAME))
    {
      add(notificationArea, BorderLayout.NORTH);
      notificationArea.setText("Files do not differ in actual content, trailing whitespaces may be different");
      revalidate();
    }
    else
    {
      notificationArea.setText("");
    }
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
