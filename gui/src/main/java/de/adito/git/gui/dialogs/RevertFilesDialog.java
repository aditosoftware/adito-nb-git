package de.adito.git.gui.dialogs;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.diff.IFileChangeType;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.swing.MutableIconActionButton;
import de.adito.git.gui.tree.TreeUtil;
import de.adito.git.gui.tree.models.*;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera, 15.04.2019
 */
class RevertFilesDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private ObservableTreeUpdater<IFileChangeType> treeUpdater;
  private ObservableTreeSelectionModel observableTreeSelectionModel;
  private MouseListener popupMouseListener;
  private JTree fileTree;
  protected JLabel descriptionLabel;

  @Inject
  public RevertFilesDialog(IActionProvider pActionProvider, IIconLoader pIconLoader, IPrefStore pPrefStore, IFileSystemUtil pFileSystemUtil,
                           @Assisted Observable<Optional<IRepository>> pRepositoryObs, @Assisted List<IFileChangeType> pFilesToRevert, @Assisted File pProjectDir)
  {
    if (pFilesToRevert.size() > 1)
    {
      setPreferredSize(new Dimension(675, 500));
      setLayout(new BorderLayout(0, 10));

      // Label at the top
      descriptionLabel = new JLabel("Revert the listed files back to the state of HEAD?");
      descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
      add(descriptionLabel, BorderLayout.NORTH);

      // Tree and scrollPane for tree
      Observable<List<IFileChangeType>> changedFiles = Observable.just(pFilesToRevert);
      boolean useFlatTree = Constants.TREE_VIEW_FLAT.equals(pPrefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY));
      BaseObservingTreeModel<IFileChangeType> treeModel = useFlatTree ? new FlatStatusTreeModel(pProjectDir) : new StatusTreeModel(pProjectDir);
      fileTree = new JTree(treeModel);
      Runnable[] doAfterJobs = new Runnable[1];
      doAfterJobs[0] = () -> TreeUtil.expandTreeInterruptible(fileTree);
      treeUpdater = new ObservableTreeUpdater<>(changedFiles, treeModel, pFileSystemUtil, doAfterJobs);
      fileTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil, pProjectDir));
      JScrollPane scrollPane = new JScrollPane(fileTree);
      add(scrollPane, BorderLayout.CENTER);

      Observable<Optional<List<IFileChangeType>>> selectionObservable = _getSelectionObservable();
      Action diffToHeadAction = pActionProvider.getDiffToHeadAction(pRepositoryObs, selectionObservable, false);
      // Toolbar
      JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
      toolBar.setFloatable(false);
      toolBar.add(pActionProvider.getExpandTreeAction(fileTree));
      toolBar.add(pActionProvider.getCollapseTreeAction(fileTree));
      toolBar.add(diffToHeadAction);
      toolBar.add(new MutableIconActionButton(pActionProvider.getSwitchTreeViewAction(fileTree, pProjectDir, this.getClass().getName(), treeUpdater),
                                              () -> Constants.TREE_VIEW_FLAT.equals(pPrefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY)),
                                              pIconLoader.getIcon(Constants.SWITCH_TREE_VIEW_HIERARCHICAL),
                                              pIconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT))
                      .getButton());
      add(toolBar, BorderLayout.WEST);
      JPopupMenu popupMenu = new JPopupMenu();
      popupMenu.add(diffToHeadAction);
      popupMouseListener = new PopupMouseListener(popupMenu);
      fileTree.addMouseListener(popupMouseListener);
    }
    else if (pFilesToRevert.size() == 1)
    {
      descriptionLabel = new JLabel("Revert file " + pFilesToRevert.get(0).getFile().getAbsolutePath() + " back to the state of HEAD?");
      add(descriptionLabel);
    }
  }

  /**
   * @return Observable of the currently selected IFileChangeTypes
   */
  private Observable<Optional<List<IFileChangeType>>> _getSelectionObservable()
  {
    observableTreeSelectionModel = new ObservableTreeSelectionModel(fileTree.getSelectionModel());
    fileTree.setSelectionModel(observableTreeSelectionModel);
    return observableTreeSelectionModel.getSelectedPaths().map(pSelected -> {
      if (pSelected == null)
        return Optional.of(Collections.emptyList());
      return Optional.of(Arrays.stream(pSelected)
                             .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo().getMembers())
                             .flatMap(Collection::stream)
                             .collect(Collectors.toList()));
    });
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
    if (treeUpdater != null)
      treeUpdater.discard();
    if (observableTreeSelectionModel != null)
      observableTreeSelectionModel.discard();
    if (fileTree != null)
      fileTree.removeMouseListener(popupMouseListener);
  }
}
