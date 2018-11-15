package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileDiff;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.dialogs.panels.DiffPanel;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.DiffTableModel;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Window that displays the list of changes found during a diff
 *
 * @author m.kaspera 05.10.2018
 */
class DiffDialog extends JPanel implements IDiscardable {

    private final JTable fileListTable = new JTable();
    private final ObservableListSelectionModel observableListSelectionModel;
    private final DiffPanel oldVersionPanel = new DiffPanel(BorderLayout.EAST, EChangeSide.OLD, null, true);
    private final DiffPanel newVersionPanel = new DiffPanel(BorderLayout.WEST, EChangeSide.NEW, null, true);
    private Disposable disposable;
    private final IEditorKitProvider editorKitProvider;
    private List<IFileDiff> diffs;

    @Inject
    public DiffDialog(IEditorKitProvider pEditorKitProvider, @Assisted List<IFileDiff> pDiffs) {
        observableListSelectionModel = new ObservableListSelectionModel(fileListTable.getSelectionModel());
        fileListTable.setSelectionModel(observableListSelectionModel);
        editorKitProvider = pEditorKitProvider;
        this.diffs = pDiffs;
        _initGui();
    }

    /**
     * sets up the GUI
     */
    private void _initGui() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1600, 900));

        // Table on which to select which IFileDiff is displayed in the DiffPanel
        fileListTable.setModel(new DiffTableModel(diffs));
        fileListTable.setMinimumSize(new Dimension(200, 600));
        fileListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // display the first entry as default
        if (diffs.size() >= 1)
            _updateDiffPanel(diffs.get(0));
        // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
        disposable = observableListSelectionModel.selectedRows().subscribe(pSelectedRows -> {
            if (pSelectedRows != null && pSelectedRows.length == 1)
                _updateDiffPanel(diffs.get(pSelectedRows[0]));
        });

        // make left and right DiffPanel scroll at the same time/speed
        oldVersionPanel.coupleToScrollPane(newVersionPanel.getMainScrollPane());
        oldVersionPanel.getMainScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, oldVersionPanel, newVersionPanel);
        splitPane.setResizeWeight(0.5);

        // add table and DiffPanel to the Panel
        add(fileListTable, BorderLayout.EAST);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * @param fileDiff the IFileDiff that should be displayed in the Diff Panel
     */
    private void _updateDiffPanel(IFileDiff fileDiff) {
//        oldVersionPanel.getTextPane().setEditorKit(editorKitProvider.getEditorKit("text/javascript"));
//        newVersionPanel.getTextPane().setEditorKit(editorKitProvider.getEditorKit("text/javascript"));
        oldVersionPanel.setContent(fileDiff.getFileChanges().getChangeChunks());
        newVersionPanel.setContent(fileDiff.getFileChanges().getChangeChunks());
    }

    @Override
    public void discard() {
        disposable.dispose();
    }

}
