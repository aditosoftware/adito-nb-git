package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.models.ObservingTreeModel;
import de.adito.git.gui.tree.models.StatusTreeModel;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.EditorKit;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
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
  private final SearchableTree fileTree;
  private final ObservableTreeSelectionModel observableTreeSelectionModel;
  private final IEditorKitProvider editorKitProvider;
  private final IIconLoader iconLoader;
  private final IFileSystemUtil fileSystemUtil;
  private final IActionProvider actionProvider;
  private final File projectDirectory;
  private final boolean acceptChange;
  private final boolean showFileTable;
  private final JTextPane notificationArea = new JTextPane();
  private final JPanel searchPanel = new JPanel(new BorderLayout());
  private DiffPanel diffPanel;
  private Disposable disposable;
  private List<IFileDiff> diffs;

  @Inject
  public DiffDialog(IIconLoader pIconLoader, IEditorKitProvider pEditorKitProvider, IQuickSearchProvider pQuickSearchProvider, IFileSystemUtil pFileSystemUtil,
                    IActionProvider pActionProvider, @Assisted File pProjectDirectory, @Assisted List<IFileDiff> pDiffs,
                    @Assisted @javax.annotation.Nullable String pSelectedFile,
                    @Assisted("acceptChange") boolean pAcceptChange, @Assisted("showFileTable") boolean pShowFileTable)
  {
    iconLoader = pIconLoader;
    fileSystemUtil = pFileSystemUtil;
    actionProvider = pActionProvider;
    projectDirectory = pProjectDirectory;
    acceptChange = pAcceptChange;
    showFileTable = pShowFileTable;
    diffs = pDiffs;
    List<IFileChangeType> pList = new ArrayList<>(diffs);
    fileTree = new SearchableTree();
    StatusTreeModel statusTreeModel = new StatusTreeModel(Observable.just(pList), pProjectDirectory);
    fileTree.setModel(statusTreeModel);
    statusTreeModel.registerDataModelUpdatedListener(new ObservingTreeModel.IDataModelUpdateListener()
    {
      @Override
      public void modelUpdated()
      {
        // display the first entry as default
        if (!diffs.isEmpty())
          actionProvider.getExpandTreeAction(fileTree).actionPerformed(null);
        _setSelectedFile(pSelectedFile);
        statusTreeModel.removeDataModelUpdateListener(this);
      }
    });
    observableTreeSelectionModel = new ObservableTreeSelectionModel(fileTree.getSelectionModel());
    fileTree.setSelectionModel(observableTreeSelectionModel);
    pQuickSearchProvider.attach(searchPanel, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(fileTree));
    editorKitProvider = pEditorKitProvider;
    _initGui(pIconLoader);
  }

  /**
   * sets up the GUI
   */
  private void _initGui(IIconLoader pIconLoader)
  {
    setLayout(new BorderLayout());
    setMinimumSize(PANEL_MIN_SIZE);
    setPreferredSize(PANEL_PREF_SIZE);

    // Tree on which to select which IFileDiff is displayed in the DiffPanel
    fileTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(fileSystemUtil, projectDirectory));
    fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    JScrollPane fileTreeScrollPane = new JScrollPane(fileTree);
    fileTreeScrollPane.setMinimumSize(TABLE_MIN_SIZE);
    fileTreeScrollPane.setPreferredSize(TABLE_PREF_SIZE);
    // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
    Observable<Optional<IFileDiff>> fileDiffObservable = observableTreeSelectionModel.getSelectedPaths().map(pSelectedPaths -> {
      if (pSelectedPaths != null && pSelectedPaths.length == 1 && !((FileChangeTypeNode) pSelectedPaths[0].getLastPathComponent()).getInfo().getMembers().isEmpty())
        return Optional.of(((IFileDiff) ((FileChangeTypeNode) pSelectedPaths[0].getLastPathComponent()).getInfo().getMembers().get(0)));
      else return Optional.empty();
    });
    Observable<EditorKit> editorKitObservable = fileDiffObservable
        .map(pFileDiff -> pFileDiff
            .map(IFileDiff::getAbsoluteFilePath)
            .map(editorKitProvider::getEditorKit)
            .orElseGet(() -> editorKitProvider.getEditorKitForContentType("text/plain")));

    diffPanel = new DiffPanel(pIconLoader, fileDiffObservable, acceptChange ? iconLoader.getIcon(ACCEPT_ICON_PATH) : null, editorKitObservable);

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
      JToolBar toolBar = new JToolBar();
      toolBar.setFloatable(false);
      toolBar.add(actionProvider.getExpandTreeAction(fileTree));
      toolBar.add(actionProvider.getCollapseTreeAction(fileTree));
      searchPanel.add(toolBar, BorderLayout.NORTH);
      searchPanel.add(fileTreeScrollPane, BorderLayout.CENTER);
      // add table and DiffPanel to the SplitPane
      JSplitPane diffToListSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, diffPanel, searchPanel);
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
    FileChangeTypeNode node = (FileChangeTypeNode) fileTree.getModel().getRoot();
    if (node != null)
    {
      TreePath treePath = new TreePath(node);
      while (pSelectedFile != null && !node.isLeaf())
      {
        for (int index = 0; index < node.getChildCount(); index++)
        {
          if (((FileChangeTypeNode) node.getChildAt(index)).getInfo().getMembers()
              .stream()
              .anyMatch(pChangeType -> pSelectedFile.equals(pChangeType.getFile().toString())))
          {
            node = (FileChangeTypeNode) node.getChildAt(index);
            treePath = treePath.pathByAddingChild(node);
            break;
          }
        }
      }
      fileTree.getSelectionModel().setSelectionPath(treePath);
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
