package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.swing.CheckBoxTreeSelectionModel;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.icon.IIconLoader;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableCheckboxTree;
import de.adito.git.gui.rxjava.ObservableTreeSelectionModel;
import de.adito.git.gui.swing.LinedDecorator;
import de.adito.git.gui.swing.MutableIconActionButton;
import de.adito.git.gui.tree.TreeUtil;
import de.adito.git.gui.tree.models.*;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import de.adito.git.gui.tree.renderer.FileChangeTypeFlatTreeCellRenderer;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import de.adito.git.impl.observables.DocumentChangeObservable;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Commit window
 *
 * @author m.kaspera 04.10.2018
 */
class CommitDialog extends AditoBaseDialog<CommitDialogResult> implements IDiscardable
{

  private static final int PREFERRED_WIDTH = 750;
  private static final int PREFERRED_HEIGHT = 700;
  private static final Dimension MESSAGE_PANE_MIN_SIZE = new Dimension(100, 100);
  private final JPanel tableSearchView = new JPanel(new BorderLayout());
  private final JEditorPane messagePane = new JEditorPane();
  private final JCheckBox amendCheckBox = new JCheckBox("amend commit");
  private final IActionProvider actionProvider;
  private final IIconLoader iconLoader;
  private final IPrefStore prefStore;
  private final Observable<Optional<IRepository>> repository;
  private final SearchableCheckboxTree checkBoxTree;
  private final Observable<List<File>> selectedFiles;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final JLabel loadingLabel = new JLabel("Loading...");
  private _DetailsWithDescriptionPanel committerNamePanel;
  private _DetailsWithDescriptionPanel committerMailPanel;
  private ObservableTreeUpdater<IFileChangeType> treeUpdater;
  private ObservableTreeSelectionModel observableTreeSelectionModel;

  @Inject
  public CommitDialog(IFileSystemUtil pFileSystemUtil, IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider,
                      IIconLoader pIconLoader, IPrefStore pPrefStore, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                      @Assisted String pMessageTemplate, IEditorKitProvider pEditorKitProvider)
  {
    actionProvider = pActionProvider;
    iconLoader = pIconLoader;
    prefStore = pPrefStore;
    repository = pRepository;
    // disable OK button at the start since the commit message is empty then
    pIsValidDescriptor.setValid(pMessageTemplate != null && !pMessageTemplate.isEmpty());
    checkBoxTree = new SearchableCheckboxTree();
    Optional<IRepository> optRepo = pRepository.blockingFirst();
    Observable<Optional<IFileStatus>> statusObservable = optRepo.map(IRepository::getStatus).orElse(Observable.just(Optional.empty()));
    Observable<List<IFileChangeType>> filesToCommitObservable = Observable
        .combineLatest(statusObservable, pFilesToCommit, (pStatusObs, pSelectedFiles) -> new ArrayList<>(pStatusObs
                                                                                                             .map(IFileStatus::getUncommitted)
                                                                                                             .orElse(Collections.emptyList())));
    File dir = optRepo.map(IRepository::getTopLevelDirectory).orElse(null);
    if (optRepo.isPresent() && dir != null)
    {
      _initCheckBoxTree(pFileSystemUtil, pQuickSearchProvider, pFilesToCommit, filesToCommitObservable, dir);
      SwingUtilities.invokeLater(() -> {
        messagePane.setEditorKit(pEditorKitProvider.getEditorKitForContentType("text/plain"));
        messagePane.setText(pMessageTemplate);
      });
      Observable<Boolean> nonEmptyTextObservable = Observable.create(new DocumentChangeObservable(messagePane))
          .switchMap(pDocument -> Observable.create(new _NonEmptyTextObservable(pDocument)))
          .startWith(messagePane.getDocument().getLength() > 0);
      selectedFiles = Observable.create(new _CBTreeObservable(checkBoxTree))
          .startWith(List.<File>of())
          .replay(1)
          .autoConnect(0, disposables::add);
      disposables.add(Observable.combineLatest(selectedFiles, nonEmptyTextObservable, (pFiles, pValid) -> !pFiles.isEmpty() && pValid)
                          .subscribe(pIsValidDescriptor::setValid));
      _initGui(dir, optRepo.get().getConfig());
    }
    else
    {
      // in case the repository was not present: Everything is empty, but no exception/crash
      selectedFiles = Observable.just(List.of());
      disposables.add(selectedFiles.subscribe());
    }
    _addAmendCheckbox(pRepository);
  }

  private void _addAmendCheckbox(@Assisted Observable<Optional<IRepository>> pRepository)
  {
    Observable<Optional<IRepositoryState>> repoState = pRepository.switchMap(pRepoOpt -> pRepoOpt.map(IRepository::getRepositoryState)
        .orElse(Observable.just(Optional.empty())));
    if (repoState.blockingFirst(Optional.empty()).map(pIRepositoryState -> !pIRepositoryState.canAmend()).orElse(true))
    {
      amendCheckBox.setEnabled(false);
      amendCheckBox.setToolTipText("<html>The current repository state does not allow amending a commit.<br>" +
                                       "This is because the merge/cherry pick/revert you are doing is still in progress.<br>" +
                                       "Finish that process by resolving all conflicts and doing a normal commit, or abort the process.</html>");
    }
    else
    {
      amendCheckBox.setToolTipText("");
    }
    amendCheckBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    amendCheckBox.addActionListener(e -> {
      if (amendCheckBox.getModel().isSelected())
      {
        messagePane.setText(pRepository.blockingFirst().map(pRepo -> {
          try
          {
            return pRepo.getCommit(null).getShortMessage();
          }
          catch (Exception e1)
          {
            return "an error occurred while retrieving the commit message of the last commit";
          }
        }).orElse("could not retrieve message of last commit"));
      }
    });
  }

  /**
   * initializes all necessary checboxtree attributes/abilites (QuickSearch, Popupmenu, preselected items...)
   *
   * @param pFileSystemUtil      FileSystemUtil to retrieve icons for files
   * @param pQuickSearchProvider QuickSearchProvider to provide QuickSearch support for the tree
   * @param pFilesToCommit       pre-selected files to commit by the user
   * @param pFilesToCommitObs    Observable of the changed files
   * @param pProjectDir          root folder of the project
   */
  private void _initCheckBoxTree(@NotNull IFileSystemUtil pFileSystemUtil, @NotNull IQuickSearchProvider pQuickSearchProvider,
                                 @NotNull Observable<Optional<List<IFileChangeType>>> pFilesToCommit, @NotNull Observable<List<IFileChangeType>> pFilesToCommitObs,
                                 @NotNull File pProjectDir)
  {
    boolean useFlatTreeModel = Constants.TREE_VIEW_FLAT.equals(prefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY));
    BaseObservingTreeModel<IFileChangeType> statusTreeModel = useFlatTreeModel ? new FlatStatusTreeModel(pProjectDir)
        : new StatusTreeModel(pProjectDir);
    checkBoxTree.init(tableSearchView, statusTreeModel);
    checkBoxTree.setCellRenderer(useFlatTreeModel ? new FileChangeTypeFlatTreeCellRenderer(pFileSystemUtil, pProjectDir)
                                     : new FileChangeTypeTreeCellRenderer(pFileSystemUtil, pProjectDir));
    List<File> preSelectedFiles = pFilesToCommit.blockingFirst()
        .map(pFileChangeTypes -> pFileChangeTypes.stream()
            .map(IFileChangeType::getFile)
            .collect(Collectors.toList()))
        .orElse(List.of());
    JScrollPane scrollPane = new JScrollPane(checkBoxTree);
    Runnable[] doAfterJobs = new Runnable[3];
    doAfterJobs[0] = () -> TreeUtil.expandTreeInterruptible(checkBoxTree);
    doAfterJobs[1] = () -> _markPreselectedAndExpand(statusTreeModel, preSelectedFiles);
    doAfterJobs[2] = () -> {
      tableSearchView.remove(loadingLabel);
      tableSearchView.add(scrollPane, BorderLayout.CENTER);
      revalidate();
      repaint();
    };
    treeUpdater = new ObservableTreeUpdater<>(pFilesToCommitObs, statusTreeModel, pFileSystemUtil, doAfterJobs);
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    tableSearchView.add(loadingLabel, BorderLayout.CENTER);
    pQuickSearchProvider.attach(tableSearchView, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(checkBoxTree));
    _attachPopupMenu(checkBoxTree);
  }

  /**
   * makes sure the preselected files are selected and expands the root node
   *
   * @param pStatusTreeModel  StatusTreeModel that should have the specified files selected and it's root node expanded
   * @param pPreSelectedFiles list of files that should be preselected in the tree
   */
  private void _markPreselectedAndExpand(BaseObservingTreeModel pStatusTreeModel, List<File> pPreSelectedFiles)
  {
    _setSelected(pPreSelectedFiles, null, (FileChangeTypeNode) checkBoxTree.getModel().getRoot(), checkBoxTree.getCheckBoxTreeSelectionModel());
    if (pStatusTreeModel.getRoot() != null)
      actionProvider.getExpandTreeAction(checkBoxTree).actionPerformed(null);
  }

  /**
   * Sets all the nodes that contain one of the selectedFiles in their nodeInfo selected (checkbox selected, not marked selected)
   *
   * @param pSelectedFiles              Files whose leaf nodes should have their checkbox checked
   * @param pCurrentPath                current treePath up to, but not including, the current Node. Null if current node is root
   * @param pCurrentNode                current Node
   * @param pCheckBoxTreeSelectionModel the checkbox selectionModel of the tree
   */
  private void _setSelected(@NotNull List<File> pSelectedFiles, @Nullable TreePath pCurrentPath, @NotNull FileChangeTypeNode pCurrentNode,
                            @NotNull CheckBoxTreeSelectionModel pCheckBoxTreeSelectionModel)
  {
    TreePath updatedPath = pCurrentPath == null ? new TreePath(pCurrentNode) : pCurrentPath.pathByAddingChild(pCurrentNode);
    FileChangeTypeNodeInfo nodeInfo = pCurrentNode.getInfo();
    if (pCurrentNode.isLeaf() && nodeInfo != null && pSelectedFiles.contains(nodeInfo.getNodeFile()))
    {
      pCheckBoxTreeSelectionModel.addSelectionPath(updatedPath);
    }
    Iterator<TreeNode> childIterator = pCurrentNode.children().asIterator();
    while (childIterator.hasNext())
    {
      _setSelected(pSelectedFiles, updatedPath, (FileChangeTypeNode) childIterator.next(), pCheckBoxTreeSelectionModel);
    }
  }

  /**
   * attach the popupMenu to the checkBoxTree
   * This also changes the selectionModel of the Tree to an observableSelectionModel
   *
   * @param pTree tree that should have the popupMenu attached
   */
  private void _attachPopupMenu(@NotNull JTree pTree)
  {
    observableTreeSelectionModel = new ObservableTreeSelectionModel(pTree.getSelectionModel());
    pTree.setSelectionModel(observableTreeSelectionModel);
    Observable<Optional<List<IFileChangeType>>> selectionObservable = observableTreeSelectionModel.getSelectedPaths().map(pSelected -> {
      if (pSelected == null)
        return Optional.of(Collections.emptyList());
      return Optional.of(Arrays.stream(pSelected)
                             .map(pTreePath -> ((FileChangeTypeNode) pTreePath.getLastPathComponent()).getInfo().getMembers())
                             .flatMap(Collection::stream)
                             .collect(Collectors.toList()));
    });
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionProvider.getDiffToHeadAction(repository, selectionObservable, true));
    popupMenu.add(actionProvider.getRevertWorkDirAction(repository, selectionObservable));
    pTree.addMouseListener(new PopupMouseListener(popupMenu));
  }

  /**
   * initialise GUI elements
   *
   * @param pDir    project directory
   * @param pConfig
   */
  private void _initGui(@NotNull File pDir, IConfig pConfig)
  {
    // EditorPane for the Commit message
    messagePane.setMinimumSize(MESSAGE_PANE_MIN_SIZE);

    JPanel messagePaneWithHeader = new JPanel(new BorderLayout());
    LinedDecorator cmDecorator = new LinedDecorator("Commit Message", 32);
    cmDecorator.setBorder(new EmptyBorder(0, 0, 7, 0));
    messagePaneWithHeader.add(cmDecorator, BorderLayout.NORTH);
    messagePaneWithHeader.add(messagePane, BorderLayout.CENTER);
    messagePaneWithHeader.setBorder(null);

    // mainContent center
    JSplitPane content = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tableSearchView, messagePaneWithHeader);
    content.setResizeWeight(0.9D);

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(actionProvider.getExpandTreeAction(checkBoxTree));
    toolBar.add(actionProvider.getCollapseTreeAction(checkBoxTree));
    toolBar.add(new MutableIconActionButton(actionProvider.getSwitchTreeViewAction(checkBoxTree, pDir, this.getClass().getName(), treeUpdater),
                                            () -> Constants.TREE_VIEW_FLAT.equals(prefStore.get(this.getClass().getName() + Constants.TREE_VIEW_TYPE_KEY)),
                                            iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_HIERARCHICAL),
                                            iconLoader.getIcon(Constants.SWITCH_TREE_VIEW_FLAT))
                    .getButton());

    JPanel contentWithToolbar = new JPanel(new BorderLayout());
    contentWithToolbar.add(content, BorderLayout.CENTER);
    committerNamePanel = new _DetailsWithDescriptionPanel("Author", pConfig.getUserName() == null ? "" : pConfig.getUserName());
    committerMailPanel = new _DetailsWithDescriptionPanel("Author email", pConfig.getUserEmail() == null ? "" : pConfig.getUserEmail());

    setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    setLayout(new BorderLayout());
    add(toolBar, BorderLayout.NORTH);
    add(contentWithToolbar, BorderLayout.CENTER);
    add(_createDetailsPanel(), BorderLayout.EAST);
  }

  private JPanel _createDetailsPanel()
  {
    JPanel details = new JPanel();
    details.setPreferredSize(new Dimension(200, 0));
    details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
    details.setBorder(new EmptyBorder(2, 12, 0, 0));

    JPanel userInfoPanel = new JPanel(new BorderLayout());
    userInfoPanel.add(committerNamePanel, BorderLayout.NORTH);
    userInfoPanel.add(committerMailPanel, BorderLayout.SOUTH);
    _addDetailsCategory(details, "Git", userInfoPanel, amendCheckBox);

    return details;
  }

  private void _addDetailsCategory(JPanel pDetailsPanel, String pTitle, JComponent... pComponents)
  {
    pDetailsPanel.add(new LinedDecorator(pTitle, 32));

    JPanel content = new JPanel();
    content.setBorder(new EmptyBorder(0, 16, 0, 0));
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    for (JComponent component : pComponents)
    {
      content.add(component);
      content.add(Box.createRigidArea(new Dimension(1, 5)));
    }
    JPanel wrapperPanel = new JPanel(new BorderLayout());
    wrapperPanel.add(content, BorderLayout.NORTH);
    wrapperPanel.add(new JPanel(), BorderLayout.CENTER);
    pDetailsPanel.add(wrapperPanel);
  }

  @Override
  public String getMessage()
  {
    return messagePane.getText();
  }

  @Override
  public CommitDialogResult getInformation()
  {
    return new CommitDialogResult(_getFilesToCommit(), amendCheckBox.isSelected(), committerNamePanel.getFieldContent(), committerMailPanel.getFieldContent());
  }

  private Supplier<List<File>> _getFilesToCommit()
  {
    return selectedFiles::blockingFirst;
  }

  @Override
  public void discard()
  {
    disposables.clear();
    observableTreeSelectionModel.discard();
    treeUpdater.discard();
  }

  private static class _DetailsWithDescriptionPanel extends JPanel
  {

    private final JTextField contentField;

    public _DetailsWithDescriptionPanel(@NotNull String pDescription, @NotNull String pFieldContent)
    {
      setLayout(new BorderLayout());
      JLabel descriptionLabel = new JLabel(pDescription);
      contentField = new JTextField(pFieldContent);
      add(descriptionLabel, BorderLayout.NORTH);
      descriptionLabel.setBorder(new EmptyBorder(0, 0, 3, 0));
      add(contentField, BorderLayout.SOUTH);
      setMaximumSize(new Dimension(Integer.MAX_VALUE, descriptionLabel.getHeight() + contentField.getHeight() + 10));
      setBorder(new EmptyBorder(5, 0, 5, 0));
    }

    public String getFieldContent()
    {
      return contentField.getText();
    }
  }

  /**
   * Observable that observes a Document and fires if the document's text is empty or not
   */
  private static class _NonEmptyTextObservable extends AbstractListenerObservable<DocumentListener, Document, Boolean>
  {
    _NonEmptyTextObservable(@NotNull Document pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected DocumentListener registerListener(@NotNull Document pDocument, @NotNull IFireable<Boolean> pIFireable)
    {
      DocumentListener documentListener = new DocumentListener()
      {
        @Override
        public void insertUpdate(DocumentEvent e)
        {
          pIFireable.fireValueChanged(e.getDocument().getLength() != 0);
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
          pIFireable.fireValueChanged(e.getDocument().getLength() != 0);
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
          pIFireable.fireValueChanged(e.getDocument().getLength() != 0);
        }
      };
      pDocument.addDocumentListener(documentListener);
      return documentListener;
    }

    @Override
    protected void removeListener(@NotNull Document pDocument, @NotNull DocumentListener pDocumentListener)
    {
      pDocument.removeDocumentListener(pDocumentListener);
    }
  }

  /**
   * Observable that observes the files of all selected child nodes of a checkboxtree
   */
  private static class _CBTreeObservable extends AbstractListenerObservable<TreeSelectionListener, CheckBoxTree, List<File>>
  {

    _CBTreeObservable(@NotNull CheckBoxTree pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected TreeSelectionListener registerListener(@NotNull CheckBoxTree pCheckBoxTree, @NotNull IFireable<List<File>> pIFireable)
    {
      TreeSelectionListener listener = e -> pIFireable.fireValueChanged(Arrays.stream(pCheckBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths())
                                                                            .map(TreePath::getLastPathComponent)
                                                                            .filter(pObj -> pObj instanceof FileChangeTypeNode)
                                                                            .map(FileChangeTypeNode.class::cast)
                                                                            .map(this::_getAllChildrensFiles)
                                                                            .flatMap(Collection::stream)
                                                                            .collect(Collectors.toList()));
      pCheckBoxTree.getCheckBoxTreeSelectionModel().addTreeSelectionListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull CheckBoxTree pCheckBoxTree, @NotNull TreeSelectionListener pTreeSelectionListener)
    {
      pCheckBoxTree.getCheckBoxTreeSelectionModel().removeTreeSelectionListener(pTreeSelectionListener);
    }

    /**
     * @param pNode starting node
     * @return list of files that are stored in the leaf nodes of the children (or children's children and so forth)
     */
    private List<File> _getAllChildrensFiles(FileChangeTypeNode pNode)
    {
      List<File> elements = new ArrayList<>();
      LinkedList<TreeNode> stack = new LinkedList<>();
      stack.add(pNode);
      while (!stack.isEmpty())
      {
        TreeNode current = stack.poll();
        if (current instanceof FileChangeTypeNode)
        {
          if (current.isLeaf())
          {
            FileChangeTypeNodeInfo nodeInfo = ((FileChangeTypeNode) current).getInfo();
            if (nodeInfo != null)
              elements.add(nodeInfo.getNodeFile());
          }
          stack.addAll(Collections.list(current.children()));
        }
      }
      return elements;
    }
  }

}


