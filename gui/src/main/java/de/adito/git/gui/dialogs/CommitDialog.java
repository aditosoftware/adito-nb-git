package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.gui.*;
import de.adito.git.gui.dialogs.results.CommitDialogResult;
import de.adito.git.gui.quickSearch.*;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.swing.LinedDecorator;
import de.adito.git.gui.tableModels.StatusTableModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
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
  private static final int SELECTION_COL_MAX_WIDTH = 25;
  private static final Dimension MESSAGE_PANE_MIN_SIZE = new Dimension(100, 100);
  private final SearchableTable fileStatusTable;
  private final JPanel tableSearchView = new JPanel(new BorderLayout());
  private final _SelectedCommitTableModel commitTableModel;
  private final JEditorPane messagePane = new JEditorPane();
  private final JCheckBox amendCheckBox = new JCheckBox("amend commit");
  private IDialogDisplayer.IDescriptor isValidDescriptor;
  private final Observable<Optional<IRepository>> repository;

  @Inject
  public CommitDialog(IQuickSearchProvider pQuickSearchProvider, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted Observable<Optional<List<IFileChangeType>>> pFilesToCommit,
                      @Assisted String pMessageTemplate, IEditorKitProvider pEditorKitProvider)
  {
    isValidDescriptor = pIsValidDescriptor;
    repository = pRepository;
    messagePane.setEditorKit(pEditorKitProvider.getEditorKitForContentType("text/plain"));
    // disable OK button at the start since the commit message is empty then
    isValidDescriptor.setValid(pMessageTemplate != null && !pMessageTemplate.isEmpty());
    Observable<Optional<IFileStatus>> statusObservable = pRepository
        .switchMap(pRepo -> pRepo
            .orElseThrow(() -> new RuntimeException("no valid repository found"))
            .getStatus());
    Observable<List<SelectedFileChangeType>> filesToCommitObservable = Observable
        .combineLatest(statusObservable, pFilesToCommit, (pStatusObservable, pSelectedFiles) -> pStatusObservable
            .map(IFileStatus::getUncommitted)
            .orElse(Collections.emptyList())
            .stream()
            .map(pUncommitted -> new SelectedFileChangeType(pSelectedFiles.orElse(Collections.emptyList()).contains(pUncommitted), pUncommitted))
            .collect(Collectors.toList()));
    commitTableModel = new _SelectedCommitTableModel(filesToCommitObservable);
    fileStatusTable = new SearchableTable(commitTableModel, tableSearchView);
    List<Integer> searchableColumns = new ArrayList<>();
    searchableColumns.add(commitTableModel.findColumn(_SelectedCommitTableModel.FILE_NAME_COLUMN_NAME));
    searchableColumns.add(commitTableModel.findColumn(_SelectedCommitTableModel.FILE_PATH_COLUMN_NAME));
    pQuickSearchProvider.attach(tableSearchView, BorderLayout.SOUTH, new QuickSearchCallbackImpl(fileStatusTable, searchableColumns));
    JScrollPane scroller = new JScrollPane(fileStatusTable);
    tableSearchView.add(scroller, BorderLayout.CENTER);
    messagePane.setText(pMessageTemplate);
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
    fileStatusTable.setSelectionModel(new ObservableListSelectionModel(fileStatusTable.getSelectionModel()));
    fileStatusTable.getColumnModel().getColumn(commitTableModel.findColumn(_SelectedCommitTableModel.IS_SELECTED_COLUMN_NAME))
        .setMaxWidth(SELECTION_COL_MAX_WIDTH);
    fileStatusTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    // Hide the status column from view, but leave the data (retrieved via table.getModel.getValueAt)
    fileStatusTable.getColumnModel().removeColumn(fileStatusTable.getColumn(_SelectedCommitTableModel.CHANGE_TYPE_COLUMN_NAME));
    // Set Renderer for cells so they are colored according to their EChangeType
    for (int index = 1; index < fileStatusTable.getColumnModel().getColumnCount(); index++)
    {
      _setColumnSize(index);
      fileStatusTable.getColumnModel().getColumn(index).setCellRenderer(new FileStatusCellRenderer());
    }
    fileStatusTable.setBorder(BorderFactory.createEmptyBorder());

    // EditorPane for the Commit message
    messagePane.setMinimumSize(MESSAGE_PANE_MIN_SIZE);
    messagePane.getDocument().addDocumentListener(new _EmptyDocumentListener()); // Listener for enabling/disabling the OK button

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
    toolBar.add(new _SelectAllAction());
    toolBar.add(new _DeselectAllAction());

    JPanel contentWithToolbar = new JPanel(new BorderLayout());
    contentWithToolbar.add(toolBar, BorderLayout.NORTH);
    contentWithToolbar.add(content, BorderLayout.CENTER);

    setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    setLayout(new BorderLayout());
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

  private void _setColumnSize(int pColumnNum)
  {
    FontMetrics fontMetrics = fileStatusTable.getFontMetrics(fileStatusTable.getFont());
    int currentWidth;
    int maxWidth = 0;
    for (int index = 0; index < fileStatusTable.getModel().getRowCount(); index++)
    {
      currentWidth = fontMetrics.stringWidth(fileStatusTable.getModel().getValueAt(index, pColumnNum).toString());
      if (currentWidth > maxWidth)
        maxWidth = currentWidth;
    }
    fileStatusTable.getColumnModel().getColumn(pColumnNum).setMinWidth(maxWidth);
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

  private Supplier<List<IFileChangeType>> _getFilesToCommit()
  {
    return () -> commitTableModel.fileList
        .stream()
        .filter(pSelectedFileChangeType -> pSelectedFileChangeType.isSelected)
        .map(SelectedFileChangeType::getChangeType)
        .collect(Collectors.toList());
  }

  @Override
  public void discard()
  {
    commitTableModel.discard();
  }

  private static class SelectedFileChangeType
  {

    private final IFileChangeType changeType;
    private boolean isSelected;

    SelectedFileChangeType(boolean pIsSelected, IFileChangeType pChangeType)
    {
      isSelected = pIsSelected;
      changeType = pChangeType;
    }

    IFileChangeType getChangeType()
    {
      return changeType;
    }

    boolean isSelected()
    {
      return isSelected;
    }

    void setSelected(boolean pIsSelected)
    {
      isSelected = pIsSelected;
    }
  }

  /**
   * TableModel for the Table, has the list of files to commit. Similar to {@link StatusTableModel}
   */
  private class _SelectedCommitTableModel extends AbstractTableModel implements IDiscardable
  {

    static final String IS_SELECTED_COLUMN_NAME = "commit file";
    static final String FILE_NAME_COLUMN_NAME = StatusTableModel.FILE_NAME_COLUMN_NAME;
    static final String FILE_PATH_COLUMN_NAME = StatusTableModel.FILE_PATH_COLUMN_NAME;
    static final String CHANGE_TYPE_COLUMN_NAME = StatusTableModel.CHANGE_TYPE_COLUMN_NAME;
    final String[] columnNames = {IS_SELECTED_COLUMN_NAME, FILE_NAME_COLUMN_NAME, FILE_PATH_COLUMN_NAME, CHANGE_TYPE_COLUMN_NAME};

    private Disposable disposable;
    private List<SelectedFileChangeType> fileList = List.of();

    _SelectedCommitTableModel(Observable<List<SelectedFileChangeType>> pSelectedFileChangeTypes)
    {
      disposable = pSelectedFileChangeTypes.subscribe(pFilesToCommit -> fileList = pFilesToCommit);
    }

    @Override
    public int findColumn(String pColumnName)
    {
      for (int index = 0; index < pColumnName.length(); index++)
      {
        if (columnNames[index].equals(pColumnName))
        {
          return index;
        }
      }
      return -1;
    }

    @Override
    public String getColumnName(int pColumn)
    {
      return pColumn == 0 ? "" : columnNames[pColumn];
    }

    @Override
    public int getRowCount()
    {
      return fileList.size();
    }

    @Override
    public Class<?> getColumnClass(int pColumnIndex)
    {
      if (pColumnIndex == findColumn(IS_SELECTED_COLUMN_NAME))
        return Boolean.class;
      else if (pColumnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
        return String.class;
      else
        return String.class;
    }

    @Override
    public int getColumnCount()
    {
      return columnNames.length;
    }

    @Override
    public void setValueAt(Object pAValue, int pRowIndex, int pColumnIndex)
    {
      if (pColumnIndex == findColumn(IS_SELECTED_COLUMN_NAME) && pAValue instanceof Boolean)
      {
        fileList.get(pRowIndex).setSelected((Boolean) pAValue);
        fireTableDataChanged();
      }
      else
        super.setValueAt(pAValue, pRowIndex, pColumnIndex);
    }

    @Override
    public Object getValueAt(int pRowIndex, int pColumnIndex)
    {
      Object returnValue = null;
      if (pColumnIndex == findColumn(IS_SELECTED_COLUMN_NAME))
      {
        returnValue = fileList.get(pRowIndex).isSelected;
      }
      else if (pColumnIndex == findColumn(FILE_NAME_COLUMN_NAME))
      {
        returnValue = fileList.get(pRowIndex).getChangeType().getFile().getName();
      }
      else if (pColumnIndex == findColumn(FILE_PATH_COLUMN_NAME))
      {
        final URI fileUri = fileList.get(pRowIndex).getChangeType().getFile().toURI();
        returnValue = repository.blockingFirst().map(pRepository -> pRepository.getTopLevelDirectory().toURI().relativize(fileUri)).orElse(fileUri);
      }
      else if (pColumnIndex == findColumn(CHANGE_TYPE_COLUMN_NAME))
      {
        returnValue = fileList.get(pRowIndex).getChangeType().getChangeType();
      }
      return returnValue;
    }

    @Override
    public boolean isCellEditable(int pRowIndex, int pColumnIndex)
    {
      return pColumnIndex == findColumn(IS_SELECTED_COLUMN_NAME);
    }

    @Override
    public void discard()
    {
      disposable.dispose();
    }

    /**
     * Sets the selection state of all files in this tableModel to the passed value and fires a tableDataChanged event afterwards
     *
     * @param pSelectionState whether all files are selected or not
     */
    void setSelectionStateForAll(boolean pSelectionState)
    {
      for (SelectedFileChangeType selectedFileChangeType : fileList)
      {
        selectedFileChangeType.setSelected(pSelectionState);
      }
      fireTableDataChanged();
    }

  }

  /**
   * Action that selects all files in the list of files to commit
   */
  private class _SelectAllAction extends AbstractAction
  {

    _SelectAllAction()
    {
      super("Select all");
    }

    @Override
    public void actionPerformed(ActionEvent pEvent)
    {
      ((_SelectedCommitTableModel) fileStatusTable.getModel()).setSelectionStateForAll(true);
    }
  }

  /**
   * Action that deselects all files in the list of files to commit
   */
  private class _DeselectAllAction extends AbstractAction
  {

    _DeselectAllAction()
    {
      super("Deselect all");
    }

    @Override
    public void actionPerformed(ActionEvent pEvent)
    {
      ((_SelectedCommitTableModel) fileStatusTable.getModel()).setSelectionStateForAll(false);
    }
  }

  /**
   * Listen to document changes, disable the OK button if there is no text written by the user
   */
  private class _EmptyDocumentListener implements DocumentListener
  {

    @Override
    public void insertUpdate(DocumentEvent pEvent)
    {
      isValidDescriptor.setValid(pEvent.getDocument().getLength() != 0);
    }

    @Override
    public void removeUpdate(DocumentEvent pEvent)
    {
      isValidDescriptor.setValid(pEvent.getDocument().getLength() != 0);
    }

    @Override
    public void changedUpdate(DocumentEvent pEvent)
    {
      isValidDescriptor.setValid(pEvent.getDocument().getLength() != 0);
    }
  }

}


