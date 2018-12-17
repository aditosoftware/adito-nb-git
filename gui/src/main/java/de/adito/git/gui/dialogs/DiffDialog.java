package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.ColorPicker;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.panels.DiffPanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.DiffTableModel;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Window that displays the list of changes found during a diff
 *
 * @author m.kaspera 05.10.2018
 */
class DiffDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private static final String DISCARD_ICON_PATH = Constants.DISCARD_CHANGE_ICON;
  private static final String ACCEPT_ICON_PATH = Constants.ACCEPT_CHANGE_YOURS_ICON;
  private final JTable fileListTable = new JTable();
  private final ObservableListSelectionModel observableListSelectionModel;
  private final IEditorKitProvider editorKitProvider;
  private final IIconLoader iconLoader;
  private DiffPanel diffPanel;
  private final JTextPane notificationArea = new JTextPane();
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
    setMinimumSize(new Dimension(800, 600));
    setPreferredSize(new Dimension(1600, 900));

    // Table on which to select which IFileDiff is displayed in the DiffPanel
    fileListTable.setModel(new DiffTableModel(diffs));
    fileListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // display the first entry as default
    if (diffs.size() > 0)
      fileListTable.getSelectionModel().setSelectionInterval(0, 0);
    // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
    Observable<Optional<IFileDiff>> fileDiffObservable = observableListSelectionModel.selectedRows().map(pSelectedRows -> {
      if (pSelectedRows != null && pSelectedRows.length == 1)
        return Optional.of(diffs.get(pSelectedRows[0]));
      else return Optional.empty();
    });

    diffPanel = new DiffPanel(fileDiffObservable, iconLoader.getIcon(ACCEPT_ICON_PATH), iconLoader.getIcon(DISCARD_ICON_PATH));


    // add table and DiffPanel to the Panel
    notificationArea.setEnabled(false);
    notificationArea.setForeground(ColorPicker.INFO_TEXT);
    add(notificationArea, BorderLayout.NORTH);
    JSplitPane diffToListSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, diffPanel, new JScrollPane(fileListTable));
    diffToListSplitPane.setResizeWeight(1);
    add(diffToListSplitPane, BorderLayout.CENTER);
  }

  @Override
  public void discard()
  {
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
