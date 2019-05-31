package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.jidesoft.swing.CheckBoxTree;
import de.adito.git.api.*;
import de.adito.git.api.data.IFileChangeType;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.gui.IEditorKitProvider;
import de.adito.git.gui.actions.IActionProvider;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.quicksearch.QuickSearchTreeCallbackImpl;
import de.adito.git.gui.quicksearch.SearchableCheckboxTree;
import de.adito.git.gui.swing.LinedDecorator;
import de.adito.git.gui.tree.models.StatusTreeModel;
import de.adito.git.gui.tree.nodes.FileChangeTypeNode;
import de.adito.git.gui.tree.nodes.FileChangeTypeNodeInfo;
import de.adito.git.gui.tree.renderer.FileChangeTypeTreeCellRenderer;
import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;

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
import java.beans.PropertyChangeListener;
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
  private final SearchableCheckboxTree checkBoxTree;
  private final Observable<List<File>> selectedFiles;
  private final Disposable disposable;

  @Inject
  public CommitDialog(IFileSystemUtil pFileSystemUtil, IQuickSearchProvider pQuickSearchProvider, IActionProvider pActionProvider,
                      @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor, @Assisted Observable<Optional<IRepository>> pRepository,
                      @Assisted Observable<Optional<List<IFileChangeType>>> pFilesToCommit, @Assisted String pMessageTemplate, IEditorKitProvider pEditorKitProvider)
  {
    actionProvider = pActionProvider;
    // disable OK button at the start since the commit message is empty then
    pIsValidDescriptor.setValid(pMessageTemplate != null && !pMessageTemplate.isEmpty());
    Observable<Optional<IFileStatus>> statusObservable = pRepository
        .switchMap(pRepo -> pRepo
            .orElseThrow(() -> new RuntimeException("no valid repository found"))
            .getStatus());
    Observable<List<IFileChangeType>> filesToCommitObservable = Observable
        .combineLatest(statusObservable, pFilesToCommit, (pStatusObservable, pSelectedFiles) -> new ArrayList<>(pStatusObservable
                                                                                                                    .map(IFileStatus::getUncommitted)
                                                                                                                    .orElse(Collections.emptyList())));
    checkBoxTree = new SearchableCheckboxTree();
    Optional<IRepository> optRepo = pRepository.blockingFirst();
    if (optRepo.isPresent())
    {
      File dir = optRepo.get().getTopLevelDirectory();
      checkBoxTree.init(tableSearchView, new StatusTreeModel(filesToCommitObservable, dir));
      checkBoxTree.setCellRenderer(new FileChangeTypeTreeCellRenderer(pFileSystemUtil, dir));
      JScrollPane scrollPane = new JScrollPane(checkBoxTree);
      tableSearchView.add(scrollPane, BorderLayout.CENTER);
      pQuickSearchProvider.attach(tableSearchView, BorderLayout.SOUTH, new QuickSearchTreeCallbackImpl(checkBoxTree));
      SwingUtilities.invokeLater(() -> {
        messagePane.setEditorKit(pEditorKitProvider.getEditorKitForContentType("text/plain"));
        messagePane.setText(pMessageTemplate);
      });
      Observable<Boolean> nonEmptyTextObservable = Observable.create(new _DocumentObservable(messagePane))
          .switchMap(pDocument -> Observable.create(new _NonEmptyTextObservable(pDocument)))
          .startWith(messagePane.getDocument().getLength() > 0);
      selectedFiles = Observable.create(new _CBTreeObservable(checkBoxTree)).startWith(List.<File>of()).share().subscribeWith(BehaviorSubject.create());
      disposable = Observable.combineLatest(selectedFiles, nonEmptyTextObservable, (pFiles, pValid) -> !pFiles.isEmpty() && pValid)
          .subscribe(pIsValidDescriptor::setValid);
    }
    else
    {
      // in case the repository was not present: Everything is empty, but no exception/crash
      selectedFiles = Observable.just(List.of());
      disposable = selectedFiles.subscribe();
    }
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
    _initGui();
  }

  /**
   * initialise GUI elements
   */
  private void _initGui()
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

    JPanel contentWithToolbar = new JPanel(new BorderLayout());
    contentWithToolbar.add(content, BorderLayout.CENTER);

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

    _addDetailsCategory(details, "Git", amendCheckBox);

    return details;
  }

  private void _addDetailsCategory(JPanel pDetailsPanel, String pTitle, JComponent... pComponents)
  {
    pDetailsPanel.add(new LinedDecorator(pTitle, 32));

    JPanel content = new JPanel();
    content.setBorder(new EmptyBorder(0, 16, 0, 0));
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    for (JComponent component : pComponents)
      content.add(component);
    pDetailsPanel.add(content);
  }

  @Override
  public String getMessage()
  {
    return messagePane.getText();
  }

  @Override
  public CommitDialogResult getInformation()
  {
    return new CommitDialogResult(_getFilesToCommit(), amendCheckBox.isSelected());
  }

  private Supplier<List<File>> _getFilesToCommit()
  {
    return selectedFiles::blockingFirst;
  }

  @Override
  public void discard()
  {
    disposable.dispose();
  }

  /**
   * Observes a JEditorPane and fires the new Document in case the document changes
   */
  private static class _DocumentObservable extends AbstractListenerObservable<PropertyChangeListener, JEditorPane, Document>
  {

    _DocumentObservable(@NotNull JEditorPane pListenableValue)
    {
      super(pListenableValue);
    }

    @NotNull
    @Override
    protected PropertyChangeListener registerListener(@NotNull JEditorPane pJEditorPane, @NotNull IFireable<Document> pIFireable)
    {
      PropertyChangeListener listener = evt -> {
        if ("document".equals(evt.getPropertyName()))
          pIFireable.fireValueChanged(pJEditorPane.getDocument());
      };
      pJEditorPane.addPropertyChangeListener(listener);
      return listener;
    }

    @Override
    protected void removeListener(@NotNull JEditorPane pJEditorPane, @NotNull PropertyChangeListener pPropertyChangeListener)
    {
      pJEditorPane.removePropertyChangeListener(pPropertyChangeListener);
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


