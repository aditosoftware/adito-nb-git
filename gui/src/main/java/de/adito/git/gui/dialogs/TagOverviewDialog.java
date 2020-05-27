package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IQuickSearchProvider;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.ITag;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.concurrency.ComputationCycleExecutor;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.TagTreeBackgroundUpdater;
import de.adito.git.gui.tree.TreeUtil;
import de.adito.git.gui.tree.renderer.TagTreeCellRenderer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dialog that shows an overview of all tags in a repository
 *
 * @author m.kaspera, 06.06.2019
 */
class TagOverviewDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private final Map<Path, ITag> pathToTagMapping = new HashMap<>();
  private final SearchableTree tree;
  private final Consumer<ICommit> selectedCommitCallback;
  private final Observable<Optional<IRepository>> repository;
  private final Disposable tagDisposable;
  private final ComputationCycleExecutor executor = new ComputationCycleExecutor();
  private static final boolean expandActionDone = false;
  private ObservableTreeSelectionModel selectionModel;
  private _SelectCommitAction selectCommitAction;

  @Inject
  TagOverviewDialog(IActionProvider pActionProvider, IIconLoader pIconLoader, IQuickSearchProvider pQuickSearchProvider,
                    @Assisted Consumer<ICommit> pSelectedCommitCallback, @Assisted Observable<Optional<IRepository>> pRepository)
  {
    selectedCommitCallback = pSelectedCommitCallback;
    repository = pRepository;
    setLayout(new BorderLayout());
    tree = new SearchableTree();
    DefaultTreeModel treeModel = _initTree(pIconLoader, pQuickSearchProvider);
    Observable<List<ITag>> tagList = pRepository.blockingFirst().map(IRepository::getTags).orElse(Observable.just(List.of()));
    tagDisposable = tagList.subscribe(pTagList -> {
      pathToTagMapping.clear();
      pTagList.forEach(pTag -> pathToTagMapping.put(Paths.get(pTag.getName()), pTag));
      TagTreeBackgroundUpdater updater;
      // expand the tree initially
      if (!expandActionDone)
        updater = new TagTreeBackgroundUpdater(treeModel, pTagList.stream().map(pTag -> Paths.get(pTag.getName())).collect(Collectors.toList()),
                                               pActionProvider.getExpandTreeAction(tree));
      else
        updater = new TagTreeBackgroundUpdater(treeModel, pTagList.stream().map(pTag -> Paths.get(pTag.getName())).collect(Collectors.toList()));
      executor.invokeComputation(updater);
    });
    Observable<Optional<ITag>> selectedTagObservable = selectionModel.getSelectedPaths().map(pSelectedPaths -> {
      if (pSelectedPaths != null && pSelectedPaths.length == 1)
        return Optional.of(pathToTagMapping.get(TreeUtil.pathFromTreePath(pSelectedPaths[0])));
      else return Optional.empty();
    });
    PopupMouseListener popupMouseListener = _getPopupMouseListener(pActionProvider, pRepository, selectionModel, selectedTagObservable);
    tree.addMouseListener(popupMouseListener);
  }

  /**
   * @param pIconLoader          IconLoader for the TreeCellRenderer to load icons
   * @param pQuickSearchProvider QuickSearchProvider to attach to a tree/table for QuickSearch support
   * @return TreeModel for the Tree used in this dialog
   */
  @NotNull
  private DefaultTreeModel _initTree(@NotNull IIconLoader pIconLoader, @NotNull IQuickSearchProvider pQuickSearchProvider)
  {
    DefaultTreeModel treeModel = new DefaultTreeModel(null);
    tree.init(this, treeModel);
    selectionModel = new ObservableTreeSelectionModel(tree.getSelectionModel());
    tree.setSelectionModel(selectionModel);
    tree.setCellRenderer(new TagTreeCellRenderer(pIconLoader));
    add(new JScrollPane(tree), BorderLayout.CENTER);
    pQuickSearchProvider.attach(this, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(tree));
    return treeModel;
  }

  /**
   * @param pActionProvider        injected IActionProvider
   * @param pRepository            IRepository of the repository for the project the user has selected
   * @param pSelectionModel        the SelectionModel of the Tree
   * @param pSelectedTagObservable Observable with the currently selected Tag
   * @return PopupMouseListener that shows a popup for the selected item
   */
  @NotNull
  private PopupMouseListener _getPopupMouseListener(@NotNull IActionProvider pActionProvider, @NotNull Observable<Optional<IRepository>> pRepository,
                                                    @NotNull ObservableTreeSelectionModel pSelectionModel, @NotNull Observable<Optional<ITag>> pSelectedTagObservable)
  {
    JPopupMenu popupMenu = new JPopupMenu();
    selectCommitAction = new _SelectCommitAction(pSelectionModel);
    popupMenu.add(selectCommitAction);
    popupMenu.add(pActionProvider.getDeleteTagAction(pRepository, pSelectedTagObservable));
    PopupMouseListener popupMouseListener = new PopupMouseListener(popupMenu);
    popupMouseListener.setDoubleClickAction(selectCommitAction);
    return popupMouseListener;
  }

  @Override
  public @Nullable String getMessage()
  {
    return null;
  }

  @Nullable
  @Override
  public Object getInformation()
  {
    return null;
  }

  @Override
  public void discard()
  {
    selectCommitAction.discard();
    tagDisposable.dispose();
    executor.shutdown();
    selectionModel.discard();
    tree.discard();
  }

  /**
   * Action that selects the commit that belongs to the currently selected tag via the selectedCommitCallback
   */
  private class _SelectCommitAction extends AbstractAction implements IDiscardable
  {

    private final ObservableTreeSelectionModel observableSelectionModel;
    private final Disposable disposable;

    _SelectCommitAction(ObservableTreeSelectionModel pObservableSelectionModel)
    {
      super("Select associated commit");
      observableSelectionModel = pObservableSelectionModel;
      disposable = observableSelectionModel.getSelectedPaths()
          .subscribe(pTreePaths -> setEnabled(pTreePaths != null && pTreePaths.length == 1 && ((DefaultMutableTreeNode) pTreePaths[0].getLastPathComponent()).isLeaf()));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      selectedCommitCallback.accept(repository.blockingFirst().map(pRepo -> {
        try
        {
          TreePath[] selectedPaths = observableSelectionModel.getSelectionPaths();
          if (selectedPaths != null && selectedPaths.length == 1)
            return pRepo.getCommit(pathToTagMapping.get(TreeUtil.pathFromTreePath(selectedPaths[0])).getId());
          return null;
        }
        catch (AditoGitException pE)
        {
          return null;
        }
      }).orElse(null));
    }

    @Override
    public void discard()
    {
      disposable.dispose();
    }
  }
}
