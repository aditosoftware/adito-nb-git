package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.panels.basediffpanel.DiffPanel;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.swing.MutableIconActionButton;
import de.adito.git.gui.tree.StatusTree;
import de.adito.git.gui.tree.TreeUtil;
import de.adito.git.gui.tree.models.*;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
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
  private final StatusTree fileTree;
  private final IEditorKitProvider editorKitProvider;
  private final IIconLoader iconLoader;
  private final IPrefStore prefStore;
  private final IActionProvider actionProvider;
  private final boolean acceptChange;
  private final boolean showFileTree;
  private final JTextPane notificationArea = new JTextPane();
  private final JPanel searchPanel = new JPanel(new BorderLayout());
  private final ObservableTreeUpdater<IFileChangeType> treeUpdater;
  private DiffPanel diffPanel;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final List<IFileDiff> diffs;

  @Inject
  public DiffDialog(IIconLoader pIconLoader, IEditorKitProvider pEditorKitProvider, IQuickSearchProvider pQuickSearchProvider, IFileSystemUtil pFileSystemUtil,
                    IPrefStore pPrefStore, IActionProvider pActionProvider, @Assisted File pProjectDirectory, @Assisted List<IFileDiff> pDiffs,
                    @Assisted("selectedFile") @javax.annotation.Nullable String pSelectedFile, @Assisted("leftHeader") @javax.annotation.Nullable String pLeftHeader,
                    @Assisted("rightHeader") @javax.annotation.Nullable String pRightHeader,
                    @Assisted("acceptChange") boolean pAcceptChange, @Assisted("showFileTree") boolean pShowFileTree)
  {
    iconLoader = pIconLoader;
    prefStore = pPrefStore;
    actionProvider = pActionProvider;
    acceptChange = pAcceptChange;
    showFileTree = pShowFileTree;
    diffs = pDiffs;
    List<IFileChangeType> pList = new ArrayList<>(diffs);
    Observable<List<IFileChangeType>> changedFiles = Observable.just(pList);
    boolean useFlatTree = Constants.TREE_VIEW_FLAT.equals(pPrefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY));
    BaseObservingTreeModel<IFileChangeType> statusTreeModel =
        useFlatTree ? new FlatStatusTreeModel(pProjectDirectory) : new StatusTreeModel(pProjectDirectory);
    fileTree = new StatusTree(pQuickSearchProvider, pFileSystemUtil, statusTreeModel, useFlatTree, pProjectDirectory, searchPanel, null);
    Runnable[] doAfterJobs = new Runnable[2];
    doAfterJobs[0] = () -> TreeUtil.expandTreeInterruptible(fileTree.getTree());
    doAfterJobs[1] = () -> _setSelectedFile(pSelectedFile);
    treeUpdater = new ObservableTreeUpdater<>(changedFiles, statusTreeModel, pFileSystemUtil, doAfterJobs);
    editorKitProvider = pEditorKitProvider;
    _initGui(pIconLoader, pProjectDirectory, pLeftHeader, pRightHeader);
  }

  /**
   * sets up the GUI
   */
  private void _initGui(@NotNull IIconLoader pIconLoader, @NotNull File pProjectDirectory, @Nullable String pLeftHeader, @Nullable String pRightHeader)
  {
    setLayout(new BorderLayout());
    setMinimumSize(PANEL_MIN_SIZE);
    setPreferredSize(PANEL_PREF_SIZE);

    // Tree on which to select which IFileDiff is displayed in the DiffPanel
    fileTree.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    JScrollPane fileTreeScrollPane = new JScrollPane(fileTree.getTree());
    // pSelectedRows[0] because with SINGLE_SELECTION only one row can be selected
    Observable<Optional<IFileDiff>> fileDiffObservable = fileTree.getSelectionObservable()
        .map(pSelectedPaths -> pSelectedPaths.map(pChangeTypes -> pChangeTypes.isEmpty() ? null : (IFileDiff) pChangeTypes.get(0)))
        .replay(1)
        .autoConnect(0, disposables::add);
    Observable<Optional<EditorKit>> editorKitObservable = fileDiffObservable
        .map(pFileDiff -> Optional.of(pFileDiff
                                          .map(pFDiff -> pFDiff.getFileHeader().getAbsoluteFilePath())
                                          .map(editorKitProvider::getEditorKit)
                                          .orElseGet(() -> editorKitProvider.getEditorKitForContentType("text/plain"))));

    diffPanel = new DiffPanel(pIconLoader, fileDiffObservable, acceptChange ? iconLoader.getIcon(ACCEPT_ICON_PATH) : null, editorKitObservable,
                              pLeftHeader, pRightHeader);

    // notificationArea for information such as identical files (except whitespaces)
    notificationArea.setEditable(false);
    notificationArea.setForeground(ColorPicker.INFO_TEXT);
    disposables.add(fileDiffObservable.subscribe(pFileDiff -> {
      if (pFileDiff.isPresent())
      {
        _setNotificationArea(pFileDiff.get());
      }
      else
      {
        notificationArea.setText("");
      }
    }));
    if (diffs.size() > 1 && showFileTree)
    {
      JToolBar toolBar = new JToolBar();
      toolBar.setFloatable(false);
      toolBar.add(actionProvider.getExpandTreeAction(fileTree.getTree()));
      toolBar.add(actionProvider.getCollapseTreeAction(fileTree.getTree()));
      toolBar.add(new MutableIconActionButton(actionProvider.getSwitchTreeViewAction(fileTree.getTree(), pProjectDirectory, this.getClass().getName(), treeUpdater),
                                              () -> Constants.TREE_VIEW_FLAT.equals(this.getClass().getName() + prefStore.get(Constants.TREE_VIEW_TYPE_KEY)),
                                              iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_HIERARCHICAL),
                                              iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT))
                      .getButton());
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
    FileChangeTypeNode node = (FileChangeTypeNode) fileTree.getTree().getModel().getRoot();
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
      fileTree.getTree().getSelectionModel().setSelectionPath(treePath);
    }
  }

  /**
   * sets the text in the notificationArea according to the current status of the IFileDiff
   *
   * @param pFileDiff current IFileDiff
   */
  private void _setNotificationArea(IFileDiff pFileDiff)
  {
    List<IChangeDelta> currentChangeDeltas = pFileDiff.getChangeDeltas();
    if (currentChangeDeltas.stream().noneMatch(pChangeDelta -> pChangeDelta.getChangeType() != EChangeType.SAME))
    {
      add(notificationArea, BorderLayout.NORTH);
      notificationArea.setText("Files do not differ in actual content, depending on the selected whitespace treatment some whitespaces may be different");
    }
    else
    {
      notificationArea.setText("");
      remove(notificationArea);
    }
    revalidate();
    repaint();
  }

  @Override
  public void discard()
  {
    disposables.dispose();
    diffPanel.discard();
    fileTree.discard();
    treeUpdater.discard();
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
