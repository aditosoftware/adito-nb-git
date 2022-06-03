package de.adito.git.gui.dialogs.panels;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.*;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.swing.MutableIconActionButton;
import de.adito.git.gui.tree.StatusTree;
import de.adito.git.gui.tree.models.*;
import de.adito.git.gui.tree.nodes.*;
import de.adito.git.impl.data.*;
import de.adito.util.reactive.AbstractListenerObservable;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author m.kaspera 13.12.2018
 */
public class CommitDetailsPanel extends ObservableTreePanel implements IDiscardable
{

  private static final double DETAIL_SPLIT_PANE_RATIO = 0.5;
  private static final String DETAILS_FORMAT_STRING = "%7.7s %s <%s> on %s";
  private static final String STANDARD_ACTION_STRING = "STANDARD_ACTION";
  private final JSplitPane detailPanelPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
  private final IActionProvider actionProvider;
  private final IPrefStore prefStore;
  private final IIconLoader iconLoader;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final JCheckBox showAllCheckbox = new JCheckBox("Show all changed files");
  private final ICommitFilter commitFilter;
  private final _SelectedCommitsPanel commits;
  private final StatusTree statusTree;
  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final ObservableTreeUpdater<IDiffInfo> treeUpdater;

  @Inject
  public CommitDetailsPanel(IActionProvider pActionProvider, IQuickSearchProvider pQuickSearchProvider,
                            IFileSystemUtil pFileSystemUtil, IPrefStore pPrefStore, IIconLoader pIconLoader,
                            @Assisted Observable<Optional<IRepository>> pRepository,
                            @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                            @Assisted ICommitFilter pCommitFilter)
  {
    super();
    actionProvider = pActionProvider;
    prefStore = pPrefStore;
    iconLoader = pIconLoader;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
    commitFilter = pCommitFilter;
    disposables.add(new ObservableCacheDisposable(observableCache));
    showAllCheckbox.setEnabled(!pCommitFilter.getFiles().isEmpty());
    showAllCheckbox.setSelected(pCommitFilter.getFiles().isEmpty());
    commits = new _SelectedCommitsPanel(selectedCommitObservable);
    File projectDirectory = repository.blockingFirst().map(IRepository::getTopLevelDirectory)
        .orElseThrow(() -> new RuntimeException("could not determine project root directory"));
    boolean useFlatTree = Constants.TREE_VIEW_FLAT.equals(pPrefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY));
    BaseObservingTreeModel<IDiffInfo> diffTreeModel = useFlatTree ? new FlatDiffTreeModel(projectDirectory) : new DiffTreeModel(projectDirectory);
    statusTree = new StatusTree(pQuickSearchProvider, pFileSystemUtil, diffTreeModel, useFlatTree, projectDirectory, treeViewPanel, treeScrollpane);
    Runnable[] doAfterJobs = {this::showTree};
    treeUpdater = new ObservableTreeUpdater<>(_observeChangedFiles(), diffTreeModel, pFileSystemUtil, doAfterJobs, this::showLoading);
    treeViewPanel.add(_getTreeToolbar(projectDirectory), BorderLayout.NORTH, 0);

    _initStatusTreeActions((ObservableTreeSelectionModel) statusTree.getTree().getSelectionModel(), _observeChangedFiles());
    _initDetailPanel();
  }

  @NotNull
  public JComponent getPanel()
  {
    return detailPanelPane;
  }

  @NotNull
  private List<IDiffInfo> _getChangedFiles(@NotNull File pProjectDirectory, @NotNull List<ICommit> pSelectedCommits, @NotNull IRepository currentRepo,
                                           @NotNull Boolean pShowAll) throws AditoGitException
  {
    Set<IFileChangeType> changedFilesSet = new HashSet<>();
    for (ICommit selectedCommit : pSelectedCommits.subList(0, pSelectedCommits.size() - 1))
    {
      changedFilesSet.addAll(currentRepo.getCommittedFiles(selectedCommit.getId())
                                 .stream()
                                 .flatMap(pDiffInfo -> pDiffInfo.getChangedFiles().stream().map(pChangeType -> new FileChangeTypeImpl(
                                     new File(pProjectDirectory, pChangeType.getFile(EChangeSide.NEW).getPath()),
                                     new File(pProjectDirectory, pChangeType.getFile(EChangeSide.OLD).getPath()),
                                     pChangeType.getChangeType())))
                                 .filter(pFileChangeType -> commitFilter.getFiles().isEmpty() || pShowAll
                                     || commitFilter.getFiles().stream().anyMatch(pFile -> _isChildOf(pFile, pFileChangeType.getFile())))
                                 .collect(Collectors.toList()));
    }
    return currentRepo.getCommittedFiles(pSelectedCommits.get(pSelectedCommits.size() - 1).getId())
        .stream()
        .map(pDiffInfo -> {
          Set<IFileChangeType> changedFiles = new HashSet<>(changedFilesSet);
          changedFiles.addAll(pDiffInfo.getChangedFiles().stream().map(pChangeType -> new FileChangeTypeImpl(
              new File(pProjectDirectory, pChangeType.getFile(EChangeSide.NEW).getPath()),
              new File(pProjectDirectory, pChangeType.getFile(EChangeSide.OLD).getPath()),
              pChangeType.getChangeType()))
                                  .filter(pFileChangeType -> commitFilter.getFiles().isEmpty() || pShowAll
                                      || commitFilter.getFiles().stream().anyMatch(pFile -> _isChildOf(pFile, pFileChangeType.getFile())))
                                  .collect(Collectors.toList()));
          return new DiffInfoImpl(pSelectedCommits.get(0), pDiffInfo.getParentCommit(), new ArrayList<>(changedFiles));
        }).collect(Collectors.toList());

  }

  /**
   * init all actions/menus of the statusTree in this method
   *
   * @param pObservableTreeSelectionModel Model of the observable tree
   * @param pChangedFilesObs              Observable with the changed files of the selected commits
   */
  private void _initStatusTreeActions(@NotNull ObservableTreeSelectionModel pObservableTreeSelectionModel, @NotNull Observable<List<IDiffInfo>> pChangedFilesObs)
  {
    Observable<Optional<String>> selectedFile = Observable
        .combineLatest(pObservableTreeSelectionModel.getSelectedPaths(), pChangedFilesObs, (pSelected, pStatus) -> {
          if (pSelected == null)
            return Optional.empty();
          return Arrays.stream(pSelected)
              .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo())
              .filter(Objects::nonNull)
              .map(FileChangeTypeNodeInfo::getNodeFile)
              .map(File::getAbsolutePath)
              .findFirst();
        });
    Observable<Optional<ICommit>> parentCommitObs = Observable
        .combineLatest(pObservableTreeSelectionModel.getSelectedPaths(), pChangedFilesObs, (pSelected, pStatus) -> {
          if (pSelected == null)
            return Optional.empty();
          return Arrays.stream(pSelected)
              .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getAssignedCommit()).findFirst();
        });

    JPopupMenu popupMenu = new JPopupMenu();
    Action diffCommitsAction = actionProvider.getDiffCommitsAction(repository, selectedCommitObservable, parentCommitObs, selectedFile);
    popupMenu.add(actionProvider.getOpenFileStringAction(selectedFile));
    popupMenu.addSeparator();
    popupMenu.add(diffCommitsAction);
    statusTree.getTree().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), STANDARD_ACTION_STRING);
    statusTree.getTree().getActionMap().put(STANDARD_ACTION_STRING, diffCommitsAction);
    PopupMouseListener popupMouseListener = new PopupMouseListener(() -> popupMenu);
    popupMouseListener.setDoubleClickAction(diffCommitsAction);
    statusTree.getTree().addMouseListener(popupMouseListener);
  }

  private JToolBar _getTreeToolbar(File pProjectDirectory)
  {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getExpandTreeAction(statusTree.getTree()));
    toolBar.add(actionProvider.getCollapseTreeAction(statusTree.getTree()));
    toolBar.add(new MutableIconActionButton(actionProvider.getSwitchDiffTreeViewAction(statusTree.getTree(), treeUpdater, pProjectDirectory,
                                                                                       this.getClass().getName()),
                                            () -> Constants.TREE_VIEW_FLAT.equals(prefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY)),
                                            iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_HIERARCHICAL),
                                            iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT))
                    .getButton());
    toolBar.add(showAllCheckbox);
    return toolBar;
  }

  /**
   * DetailPanel shows the changed files and the short message, author, commit date and full message of the
   * currently selected commit to the right of the branching window of the commitHistory.
   * If more than one commit is selected, the first selected commit in the list is chosen
   *
   * ------------------------------
   * | -------------------------- |
   * | |  Table with scrollPane | |
   * | -------------------------- |
   * |         SplitPane          |
   * | -------------------------- |
   * | |  TextArea with detail  | |
   * | |  message in scrollPane | |
   * | -------------------------- |
   * ------------------------------
   */
  private void _initDetailPanel()
  {
    detailPanelPane.setLeftComponent(treeViewPanel);
    detailPanelPane.setRightComponent(new JScrollPane(commits, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    detailPanelPane.setResizeWeight(DETAIL_SPLIT_PANE_RATIO);
  }

  /**
   * @param pDirectory File
   * @param pFile      File
   * @return true if pFile is child of or equal to pDirectory
   */
  private boolean _isChildOf(@NotNull File pDirectory, @NotNull File pFile)
  {
    Path directoryPath = pDirectory.toPath();
    Path parent = pFile.toPath();
    while (parent != null)
    {
      if (parent.equals(directoryPath))
        return true;
      parent = parent.getParent();
    }
    return false;
  }

  @NotNull
  private Observable<List<IDiffInfo>> _observeChangedFiles()
  {
    return observableCache.calculateParallel("changedFiles", () -> Observable
        .combineLatest(selectedCommitObservable, repository, _observeShowAllCB(), (pSelectedCommitsOpt, currentRepo, pShowAll) -> {
          if (pSelectedCommitsOpt.isPresent() && !pSelectedCommitsOpt.get().isEmpty() && currentRepo.isPresent())
          {
            return _getChangedFiles(currentRepo.get().getTopLevelDirectory(), pSelectedCommitsOpt.get(), currentRepo.get(), pShowAll);
          }
          else
          {
            return Collections.<IDiffInfo>emptyList();
          }
        })
        .startWithItem(List.of()));
  }

  @NotNull
  private Observable<Boolean> _observeShowAllCB()
  {
    return observableCache.calculateParallel("showAllCB", () -> Observable.create(new _CheckboxObservable(showAllCheckbox))
        .startWithItem(commitFilter.getFiles().isEmpty()));
  }

  @Override
  public void discard()
  {
    disposables.clear();
    commits.discard();
    statusTree.discard();
    treeUpdater.discard();
    treeUpdater.discard();
  }

  @Override
  protected JTree getTree()
  {
    return statusTree.getTree();
  }

  public interface ICommitDetailsPanelFactory
  {

    CommitDetailsPanel createCommitDetailsPanel(@NotNull Observable<Optional<IRepository>> pRepository,
                                                @NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable,
                                                @NotNull ICommitFilter pCommitFilter);

  }

  /**
   * Panel for all currently selected commits
   */
  private static class _SelectedCommitsPanel extends JPanel implements Scrollable, IDiscardable
  {
    private final Disposable disposable;

    _SelectedCommitsPanel(@NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      disposable = pSelectedCommitObservable
          .map(pCommitsOpt -> pCommitsOpt.orElse(List.of()))
          .distinctUntilChanged()
          .map(pCommitList -> pCommitList.stream()
              .map(_SelectedCommitsPanel::_createSingleDetailsComponent)
              .collect(Collectors.toList()))
          .subscribe(pCommitComponents -> SwingUtilities.invokeLater(() -> {
            // Rebuild UI
            removeAll();
            for (int i = 0; i < pCommitComponents.size(); i++)
            {
              if (i > 0)
              {
                add(Box.createVerticalStrut(10));
                add(new JSeparator(SwingConstants.HORIZONTAL));
                add(Box.createVerticalStrut(10));
              }
              add(pCommitComponents.get(i));
            }

            // Update
            revalidate();
            repaint();
          }));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
      return null;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle pVisibleRect, int pOrientation, int pDirection)
    {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle pVisibleRect, int pOrientation, int pDirection)
    {
      return 16;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
      return false;
    }

    @Override
    public void discard()
    {
      if (!disposable.isDisposed())
        disposable.dispose();
    }

    @NotNull
    private static JComponent _createSingleDetailsComponent(@NotNull ICommit pCommit)
    {
      String shortMessage = pCommit.getShortMessage();
      String message = pCommit.getMessage();

      JPanel panel = new JPanel(new BorderLayout(0, 8));
      JPanel messageComp = new JPanel();
      messageComp.setLayout(new BoxLayout(messageComp, BoxLayout.Y_AXIS));

      // Comp: ShortMessage
      JTextArea shortMessageComp = _createDetailsTextArea();
      shortMessageComp.setText(shortMessage);
      shortMessageComp.setFont(shortMessageComp.getFont().deriveFont(Font.BOLD));
      messageComp.add(shortMessageComp);

      if (!Objects.equals(shortMessage, message))
      {
        if (message.startsWith(shortMessage))
          message = message.substring(shortMessage.length()).trim();

        if (!message.isEmpty())
        {
          messageComp.add(Box.createVerticalStrut(10));

          // Comp: LongMessage
          JTextArea longMessageComp = _createDetailsTextArea();
          longMessageComp.setText(message);
          messageComp.add(longMessageComp);
        }
      }

      panel.add(messageComp, BorderLayout.NORTH);

      // Comp: Details
      JTextArea details = _createDetailsTextArea();
      details.setText(String.format(DETAILS_FORMAT_STRING, pCommit.getId(), pCommit.getAuthor(), pCommit.getEmail(),
                                    DateTimeRenderer.asString(pCommit.getTime())));
      details.setForeground(UIManager.getColor("Label.disabledForeground"));
      panel.add(details, BorderLayout.CENTER);
      return panel;
    }

    @NotNull
    private static JTextArea _createDetailsTextArea()
    {
      JTextArea shortMessageComp = new JTextArea();
      shortMessageComp.setBackground(new JLabel().getBackground());
      shortMessageComp.setLineWrap(true);
      shortMessageComp.setWrapStyleWord(true);
      shortMessageComp.setEditable(false);
      return shortMessageComp;
    }
  }

  private static class _CheckboxObservable extends AbstractListenerObservable<ItemListener, JCheckBox, Boolean>
  {

    _CheckboxObservable(@NotNull JCheckBox pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected ItemListener registerListener(@NotNull JCheckBox pJCheckBox, @NotNull IFireable<Boolean> pIFireable)
    {
      ItemListener listener = e -> pIFireable.fireValueChanged(pJCheckBox.isSelected());
      pJCheckBox.addItemListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull JCheckBox pJCheckBox, @NotNull ItemListener pItemListener)
    {
      pJCheckBox.removeItemListener(pItemListener);
    }
  }
}
