package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.gui.dialogs.results.IMergeConflictResolutionDialogResult;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.swing.MergeDiffTableCellRenderer;
import de.adito.git.gui.tablemodels.MergeDiffStatusModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 25.10.2018
 */
class MergeConflictDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private static final String NO_REPO_ERROR_MSG = "no valid repository found";

  private final IDialogProvider dialogProvider;
  private IDialogDisplayer.IDescriptor isValidDescriptor;
  private IRepository repository;
  private final Subject<List<IMergeData>> mergeConflictDiffs;
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private final JTable mergeConflictTable = new JTable();
  private final JButton manualMergeButton = new JButton("Manual Merge");
  private final JButton acceptYoursButton = new JButton("Accept Yours");
  private final JButton acceptTheirsButton = new JButton("Accept Theirs");
  private final Observable<Optional<List<IMergeData>>> selectedMergeDiffObservable;
  private final MergeDiffStatusModel mergeDiffStatusModel;
  private final Disposable disposable;
  private final Disposable selectionDisposable;
  private final ObservableListSelectionModel observableListSelectionModel;

  @Inject
  MergeConflictDialog(IDialogProvider pDialogProvider, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<IMergeData> pMergeConflictDiffs, @Assisted boolean pOnlyConflicting)
  {
    dialogProvider = pDialogProvider;
    isValidDescriptor = pIsValidDescriptor;
    repository = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG));
    observableListSelectionModel = new ObservableListSelectionModel(mergeConflictTable.getSelectionModel());
    mergeConflictTable.setSelectionModel(observableListSelectionModel);
    mergeConflictDiffs = BehaviorSubject.createDefault(pMergeConflictDiffs);
    Observable<Optional<IFileStatus>> obs = repository.getStatus();
    Observable<List<IMergeData>> mergeDiffListObservable = Observable
        .combineLatest(obs, mergeConflictDiffs, (pStatus, pMergeDiffs) -> pMergeDiffs.stream()
            .filter(pMergeDiff -> !pOnlyConflicting || pStatus
                .map(IFileStatus::getConflicting)
                .orElse(Collections.emptySet())
                .stream().anyMatch(pFilePath -> IFileDiff.isSameFile(pFilePath, pMergeDiff.getDiff(EConflictSide.YOURS))))
            .collect(Collectors.toList()));
    selectedMergeDiffObservable = Observable
        .combineLatest(observableListSelectionModel.selectedRows(), mergeDiffListObservable, (pSelectedRows, pMergeDiffList) -> {
          // if either the list or selection is null, more than one element is selected or the list has 0 elements
          if (pSelectedRows == null || pMergeDiffList == null || pMergeDiffList.isEmpty())
          {
            return Optional.empty();
            // if no element is selected (and at least one element is in the list, this follows from the if above)
          }
          List<IMergeData> selectedMergeDiffs = new ArrayList<>();
          for (Integer pSelectedRow : pSelectedRows)
          {
            if (pSelectedRow < pMergeDiffList.size())
              selectedMergeDiffs.add(pMergeDiffList.get(pSelectedRow));
          }
          return Optional.of(selectedMergeDiffs);
        });
    selectionDisposable = selectedMergeDiffObservable.subscribe(pSelectedMergeDiffs -> {
      manualMergeButton.setEnabled(pSelectedMergeDiffs.map(pList -> pList.size() == 1).orElse(false));
      acceptYoursButton.setEnabled(pSelectedMergeDiffs.map(pList -> !pList.isEmpty()).orElse(false));
      acceptTheirsButton.setEnabled(pSelectedMergeDiffs.map(pList -> !pList.isEmpty()).orElse(false));
    });
    disposable = mergeDiffListObservable.subscribe(pList -> isValidDescriptor.setValid(pList.isEmpty()));
    mergeDiffStatusModel = new MergeDiffStatusModel(mergeDiffListObservable);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout(5, 10));
    setPreferredSize(new Dimension(800, 600));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
    manualMergeButton.addActionListener(e -> _doManualResolve(selectedMergeDiffObservable));
    acceptYoursButton.addActionListener(e -> _acceptDefaultVersion(selectedMergeDiffObservable, EConflictSide.YOURS));
    acceptTheirsButton.addActionListener(e -> _acceptDefaultVersion(selectedMergeDiffObservable, EConflictSide.THEIRS));
    manualMergeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) manualMergeButton.getMaximumSize().getHeight()));
    acceptYoursButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) acceptYoursButton.getMaximumSize().getHeight()));
    acceptTheirsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) acceptTheirsButton.getMaximumSize().getHeight()));
    buttonPanel.setPreferredSize(new Dimension(120, 60));
    buttonPanel.add(manualMergeButton);
    buttonPanel.add(Box.createVerticalStrut(10));
    buttonPanel.add(acceptYoursButton);
    buttonPanel.add(Box.createVerticalStrut(5));
    buttonPanel.add(acceptTheirsButton);
    add(buttonPanel, BorderLayout.EAST);
    mergeConflictTable.setModel(mergeDiffStatusModel);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn("Filename")).setPreferredWidth(230);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn("Filepath")).setPreferredWidth(230);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn("Your Changes")).setPreferredWidth(120);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn("Their Changes")).setPreferredWidth(120);
    mergeConflictTable.setDefaultRenderer(Object.class, new MergeDiffTableCellRenderer());
    JScrollPane mergeConflictTableScrollPane = new JScrollPane(mergeConflictTable);
    add(mergeConflictTableScrollPane, BorderLayout.CENTER);
  }

  /**
   * @param pSelectedMergeDiffObservable Observable optional list (should be of size 1) of IMergeDatas whose conflicts should be manually resolved
   */
  private void _doManualResolve(Observable<Optional<List<IMergeData>>> pSelectedMergeDiffObservable)
  {
    Optional<List<IMergeData>> mergeDiffOptional = pSelectedMergeDiffObservable.blockingFirst();
    mergeDiffOptional.ifPresent(pMergeDatas -> {
      if (pMergeDatas.size() == 1)
      {
        IMergeConflictResolutionDialogResult<?, ?> result = dialogProvider.showMergeConflictResolutionDialog(pMergeDatas.get(0));
        if (result.isAcceptChanges())
          _acceptManualVersion(pMergeDatas.get(0));
        else if (result.getSelectedButton().equals(IDialogDisplayer.EButtons.ACCEPT_YOURS))
          _acceptDefaultVersion(selectedMergeDiffObservable, EConflictSide.YOURS);
        else if (result.getSelectedButton().equals(IDialogDisplayer.EButtons.ACCEPT_THEIRS))
          _acceptDefaultVersion(selectedMergeDiffObservable, EConflictSide.THEIRS);
        else
          pMergeDatas.get(0).reset();
      }
    });
  }

  /**
   * @param pSelectedMergeDiff Observable optional of the list of selected IMergeDatas
   * @param pConflictSide      Side of the IMergeDatas that should be accepted
   */
  private void _acceptDefaultVersion(Observable<Optional<List<IMergeData>>> pSelectedMergeDiff, EConflictSide pConflictSide)
  {
    Optional<List<IMergeData>> mergeDiffOptional = pSelectedMergeDiff.blockingFirst();
    if (mergeDiffOptional.isPresent())
    {
      for (IMergeData selectedMergeDiff : mergeDiffOptional.get())
      {
        String path = selectedMergeDiff.getDiff(pConflictSide).getFileHeader().getAbsoluteFilePath();
        if (path != null)
        {
          try
          {
            File selectedFile = new File(path);
            if (selectedMergeDiff.getDiff(pConflictSide).getFileHeader().getChangeType() == EChangeType.DELETE)
            {
              repository.remove(List.of(selectedFile));
            }
            else
            {
              _saveVersion(pConflictSide, selectedMergeDiff, selectedFile);
            }
          }
          catch (AditoGitException pE)
          {
            throw new RuntimeException(pE);
          }
        }
      }
    }
  }

  /**
   * @param pConflictSide     side of the conflict that was accepted by the user
   * @param selectedMergeDiff the mergeDiff that should be resolved
   * @param pSelectedFile     File that this mergeDiff is for
   */
  private void _saveVersion(EConflictSide pConflictSide, IMergeData selectedMergeDiff, File pSelectedFile)
  {
    String fileContents = selectedMergeDiff.getDiff(pConflictSide).getText(EChangeSide.OLD);
    logger.log(Level.INFO, () -> String.format("Git: encoding used for writing file %s to disk: %s", pSelectedFile.getAbsolutePath(),
                                               selectedMergeDiff.getDiff(pConflictSide).getEncoding(EChangeSide.NEW)));
    _writeToFile(fileContents, selectedMergeDiff.getDiff(pConflictSide).getEncoding(EChangeSide.NEW), selectedMergeDiff, pSelectedFile);
  }

  /**
   * @param pMergeDiff IMergeData whose conflicts were resolved
   */
  private void _acceptManualVersion(IMergeData pMergeDiff)
  {
    String path = pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getAbsoluteFilePath();
    if (path != null)
    {
      File selectedFile = new File(repository.getTopLevelDirectory(), pMergeDiff.getDiff(EConflictSide.YOURS).getFileHeader().getFilePath());
      String fileContents = pMergeDiff.getDiff(EConflictSide.YOURS).getText(EChangeSide.NEW);
      logger.log(Level.INFO, () -> String.format("Git: encoding used for writing file %s to disk: %s", path,
                                                 pMergeDiff.getDiff(EConflictSide.YOURS).getEncoding(EChangeSide.NEW)));
      _writeToFile(_adjustLineEndings(fileContents, pMergeDiff), pMergeDiff.getDiff(EConflictSide.YOURS).getEncoding(EChangeSide.OLD), pMergeDiff, selectedFile);
    }
  }

  /**
   * @param pFileContents      Contents that should be written to the file
   * @param pCharset           Charset used to write pFileContents to disk (gets transferred from String to byte array)
   * @param pSelectedMergeDiff IMergeData that will get resolved by writing the contents to the file
   * @param pSelectedFile      file which should be overridden with pFileContents
   */
  private void _writeToFile(String pFileContents, Charset pCharset, IMergeData pSelectedMergeDiff, File pSelectedFile)
  {
    if (!pSelectedFile.exists())
      pSelectedFile.getParentFile().mkdirs();
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pSelectedFile, false), pCharset))
    {
      writer.write(pFileContents);
      List<IMergeData> mergeDiffs = mergeConflictDiffs.blockingFirst();
      mergeDiffs.remove(pSelectedMergeDiff);
      mergeConflictDiffs.onNext(mergeDiffs);
    }
    catch (Exception pE)
    {
      throw new RuntimeException(pE);
    }
    try
    {
      repository.add(Collections.singletonList(pSelectedFile));
    }
    catch (Exception pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * replaces all lineEndings with those determined by the MergeData
   *
   * @param pFileContent content for which to change the newlines
   * @param pMergeData   IMergeData containing the FileContentInfos used to determine the used lineEndings
   * @return String with changed newlines
   */
  private static String _adjustLineEndings(String pFileContent, IMergeData pMergeData)
  {
    ELineEnding lineEnding = _getLineEnding(pMergeData);
    if (lineEnding == ELineEnding.UNIX)
    {
      return pFileContent.replaceAll("\r\n", ELineEnding.UNIX.getLineEnding()).replaceAll("\r", ELineEnding.UNIX.getLineEnding());
    }
    else if (lineEnding == ELineEnding.WINDOWS)
    {
      return pFileContent.replaceAll("\r(?!\n)", ELineEnding.WINDOWS.getLineEnding()).replaceAll("(?<!\r)\n", ELineEnding.WINDOWS.getLineEnding());
    }
    else return pFileContent.replaceAll("\r\n", ELineEnding.MAC.getLineEnding()).replaceAll("\n", ELineEnding.MAC.getLineEnding());
  }

  /**
   * Determines the lineEnding to use by checking the two NEW versions of the ConflictSides, if those have the same lineEnding then that lineEnding is used.
   * Otherwise the lineEnding used by the sytem is returned
   *
   * @param pMergeData IMergeData containing the FileContentInfos used to determine the used lineEndings
   * @return LineEnding
   */
  private static ELineEnding _getLineEnding(IMergeData pMergeData)
  {
    if (pMergeData.getDiff(EConflictSide.THEIRS).getFileContentInfo(EChangeSide.NEW).getLineEnding().get()
        == pMergeData.getDiff(EConflictSide.YOURS).getFileContentInfo(EChangeSide.NEW).getLineEnding().get())
    {
      return pMergeData.getDiff(EConflictSide.THEIRS).getFileContentInfo(EChangeSide.NEW).getLineEnding().get();
    }
    else return ELineEnding.getLineEnding(System.lineSeparator());
  }

  @Override
  public void discard()
  {
    disposable.dispose();
    selectionDisposable.dispose();
    mergeDiffStatusModel.discard();
    observableListSelectionModel.discard();
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
