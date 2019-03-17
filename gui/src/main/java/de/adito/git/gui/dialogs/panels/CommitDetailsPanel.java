package de.adito.git.gui.dialogs.panels;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.ICommit;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.gui.DateTimeRenderer;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.tree.models.StatusTreeModel;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import de.adito.git.impl.data.FileChangeTypeImpl;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 13.12.2018
 */
public class CommitDetailsPanel implements IDiscardable
{

  private static final double DETAIL_SPLIT_PANE_RATIO = 0.5;
  private static final String DETAILS_FORMAT_STRING = "%7.7s %s <%s> on %s";
  private final JSplitPane detailPanelPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
  private final JPanel tableViewPanel = new JPanel(new BorderLayout());
  private final IActionProvider actionProvider;
  private final Observable<Optional<IRepository>> repository;
  private final Observable<Optional<List<ICommit>>> selectedCommitObservable;
  private final _SelectedCommitsPanel commits;
  private final SearchableTree statusTree;

  @Inject
  public CommitDetailsPanel(IActionProvider pActionProvider, IQuickSearchProvider pQuickSearchProvider,
                            IFileSystemUtil pFileSystemUtil,
                            @Assisted Observable<Optional<IRepository>> pRepository,
                            @Assisted Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
  {
    actionProvider = pActionProvider;
    repository = pRepository;
    selectedCommitObservable = pSelectedCommitObservable;
    commits = new _SelectedCommitsPanel(selectedCommitObservable);
    statusTree = new SearchableTree();
    _setUpChangedFilesTreePanel(pQuickSearchProvider, pFileSystemUtil);
    _initDetailPanel();
  }

  public JComponent getPanel()
  {
    return detailPanelPane;
  }

  private void _setUpChangedFilesTreePanel(IQuickSearchProvider pQuickSearchProvider, IFileSystemUtil pFileSystemUtil)
  {
    File projectDirectory = repository.blockingFirst().map(IRepository::getTopLevelDirectory)
        .orElseThrow(() -> new RuntimeException("could not determine project root directory"));
    Observable<List<IFileChangeType>> changedFilesObs = Observable
        .combineLatest(selectedCommitObservable, repository, (pSelectedCommitsOpt, currentRepo) -> {
          Set<IFileChangeType> changedFilesSet = new HashSet<>();
          if (pSelectedCommitsOpt.isPresent() && !pSelectedCommitsOpt.get().isEmpty() && currentRepo.isPresent())
          {
            for (ICommit selectedCommit : pSelectedCommitsOpt.get())
            {
              changedFilesSet.addAll(currentRepo.get().getCommittedFiles(selectedCommit.getId())
                                         .stream()
                                         .map(pChangeType -> new FileChangeTypeImpl(
                                             new File(projectDirectory, pChangeType.getFile().getPath()), pChangeType.getChangeType()))
                                         .collect(Collectors.toList()));
            }
            return new ArrayList<>(changedFilesSet);
          }
          else
          {
            return Collections.emptyList();
          }
        });
    StatusTreeModel statusTreeModel = new StatusTreeModel(changedFilesObs, projectDirectory);
    statusTree.init(tableViewPanel, statusTreeModel);
    statusTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil));
    pQuickSearchProvider.attach(tableViewPanel, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(statusTree));
    tableViewPanel.add(new JScrollPane(statusTree), BorderLayout.CENTER);
    tableViewPanel.add(_getTreeToolbar(), BorderLayout.NORTH);
    ObservableTreeSelectionModel observableTreeSelectionModel = new ObservableTreeSelectionModel(statusTree.getSelectionModel());
    statusTree.setSelectionModel(observableTreeSelectionModel);
    Observable<Optional<String>> selectedFile = Observable
        .combineLatest(observableTreeSelectionModel.getSelectedPaths(), changedFilesObs, (pSelected, pStatus) -> {
          if (pSelected == null)
            return Optional.empty();
          return Arrays.stream(pSelected)
              .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo().getMembers())
              .flatMap(Collection::stream)
              .map(pChangeType -> pChangeType.getFile().getAbsolutePath())
              .findFirst();
        });
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionProvider.getOpenFileStringAction(selectedFile));
    popupMenu.addSeparator();
    popupMenu.add(actionProvider.getDiffCommitToHeadAction(repository, selectedCommitObservable, selectedFile));
    popupMenu.add(actionProvider.getDiffCommitsAction(repository, selectedCommitObservable, selectedFile));
    PopupMouseListener popupMouseListener = new PopupMouseListener(popupMenu);
    popupMouseListener.setDoubleClickAction(actionProvider.getDiffCommitsAction(repository, selectedCommitObservable, selectedFile));
    statusTree.addMouseListener(popupMouseListener);
  }

  private JToolBar _getTreeToolbar()
  {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getExpandTreeAction(statusTree));
    toolBar.add(actionProvider.getCollapseTreeAction(statusTree));
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
    detailPanelPane.setLeftComponent(tableViewPanel);
    detailPanelPane.setRightComponent(new JScrollPane(commits, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    detailPanelPane.setResizeWeight(DETAIL_SPLIT_PANE_RATIO);
  }

  @Override
  public void discard()
  {
    commits.discard();
  }

  public interface IPanelFactory
  {

    CommitDetailsPanel createCommitDetailsPanel(Observable<Optional<IRepository>> pRepository,
                                                Observable<Optional<List<ICommit>>> pSelectedCommitObservable);

  }

  /**
   * Panel for all currently selected commits
   */
  private static class _SelectedCommitsPanel extends JPanel implements Scrollable, IDiscardable
  {
    private final Disposable disposable;

    public _SelectedCommitsPanel(@NotNull Observable<Optional<List<ICommit>>> pSelectedCommitObservable)
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
}
