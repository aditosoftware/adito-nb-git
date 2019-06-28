package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IDiscardable;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
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
  private final Subject<List<IMergeDiff>> mergeConflictDiffs;
  private final JTable mergeConflictTable = new JTable();
  private final JButton manualMergeButton = new JButton("Manual Merge");
  private final JButton acceptYoursButton = new JButton("Accept Yours");
  private final JButton acceptTheirsButton = new JButton("Accept Theirs");
  private final Observable<Optional<List<IMergeDiff>>> selectedMergeDiffObservable;
  private final MergeDiffStatusModel mergeDiffStatusModel;
  private final Disposable disposable;
  private final Disposable selectionDisposable;

  @Inject
  MergeConflictDialog(IDialogProvider pDialogProvider, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<IMergeDiff> pMergeConflictDiffs, @Assisted boolean pOnlyConflicting)
  {
    dialogProvider = pDialogProvider;
    isValidDescriptor = pIsValidDescriptor;
    repository = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG));
    ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(mergeConflictTable.getSelectionModel());
    mergeConflictTable.setSelectionModel(observableListSelectionModel);
    mergeConflictDiffs = BehaviorSubject.createDefault(pMergeConflictDiffs);
    Observable<Optional<IFileStatus>> obs = repository.getStatus();
    Observable<List<IMergeDiff>> mergeDiffListObservable = Observable
        .combineLatest(obs, mergeConflictDiffs, (pStatus, pMergeDiffs) -> pMergeDiffs.stream()
            .filter(pMergeDiff -> !pOnlyConflicting || pStatus
                .map(IFileStatus::getConflicting)
                .orElse(Collections.emptySet())
                .contains(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath()))
            .collect(Collectors.toList()));
    selectedMergeDiffObservable = Observable
        .combineLatest(observableListSelectionModel.selectedRows(), mergeDiffListObservable, (pSelectedRows, pMergeDiffList) -> {
          // if either the list or selection is null, more than one element is selected or the list has 0 elements
          if (pSelectedRows == null || pMergeDiffList == null || pMergeDiffList.isEmpty())
          {
            return Optional.empty();
            // if no element is selected (and at least one element is in the list, this follows from the if above)
          }
          List<IMergeDiff> selectedMergeDiffs = new ArrayList<>();
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
    setLayout(new BorderLayout(5, 0));
    setMinimumSize(new Dimension(1200, 1));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
    manualMergeButton.addActionListener(e -> _doManualResolve(selectedMergeDiffObservable));
    acceptYoursButton.addActionListener(e -> _acceptDefaultVersion(selectedMergeDiffObservable, IMergeDiff.CONFLICT_SIDE.YOURS));
    acceptTheirsButton.addActionListener(e -> _acceptDefaultVersion(selectedMergeDiffObservable, IMergeDiff.CONFLICT_SIDE.THEIRS));
    manualMergeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) manualMergeButton.getMaximumSize().getHeight()));
    acceptYoursButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) acceptYoursButton.getMaximumSize().getHeight()));
    acceptTheirsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) acceptTheirsButton.getMaximumSize().getHeight()));
    buttonPanel.add(manualMergeButton);
    buttonPanel.add(Box.createVerticalStrut(10));
    buttonPanel.add(acceptYoursButton);
    buttonPanel.add(Box.createVerticalStrut(5));
    buttonPanel.add(acceptTheirsButton);
    add(buttonPanel, BorderLayout.EAST);
    mergeConflictTable.setModel(mergeDiffStatusModel);
    JScrollPane mergeConflictTableScrollPane = new JScrollPane(mergeConflictTable);
    add(mergeConflictTableScrollPane, BorderLayout.CENTER);
  }

  /**
   * @param pSelectedMergeDiffObservable Observable optional list (should be of size 1) of IMergeDiffs whose conflicts should be manually resolved
   */
  private void _doManualResolve(Observable<Optional<List<IMergeDiff>>> pSelectedMergeDiffObservable)
  {
    Optional<List<IMergeDiff>> mergeDiffOptional = pSelectedMergeDiffObservable.blockingFirst();
    mergeDiffOptional.ifPresent(iMergeDiffs -> {
      if (iMergeDiffs.size() == 1 && dialogProvider.showMergeConflictResolutionDialog(iMergeDiffs.get(0)).isPressedOk())
        _acceptManualVersion(iMergeDiffs.get(0));
    });
  }

  /**
   * @param pSelectedMergeDiff Observable optional of the list of selected IMergeDiffs
   * @param pConflictSide      Side of the IMergeDiffs that should be accepted
   */
  private void _acceptDefaultVersion(Observable<Optional<List<IMergeDiff>>> pSelectedMergeDiff, IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    Optional<List<IMergeDiff>> mergeDiffOptional = pSelectedMergeDiff.blockingFirst();
    if (mergeDiffOptional.isPresent())
    {
      for (IMergeDiff selectedMergeDiff : mergeDiffOptional.get())
      {
        String path = selectedMergeDiff.getDiff(pConflictSide).getAbsoluteFilePath();
        if (path != null)
        {
          File selectedFile = new File(path);
          StringBuilder fileContents = new StringBuilder();
          for (IFileChangeChunk changeChunk : selectedMergeDiff.getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst().getNewValue())
          {
            // BLines is always the "new" version of the file, in comparison to the fork point
            fileContents.append(changeChunk.getLines(EChangeSide.NEW));
          }
          _writeToFile(fileContents.toString(), selectedMergeDiff.getDiff(pConflictSide).getEncoding(EChangeSide.NEW), selectedMergeDiff, selectedFile);
        }
      }
    }
  }

  /**
   * @param pMergeDiff IMergeDiff whose conflicts were resolved
   */
  private void _acceptManualVersion(IMergeDiff pMergeDiff)
  {
    String path = pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getAbsoluteFilePath();
    if (path != null)
    {
      File selectedFile = new File(repository.getTopLevelDirectory(), pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath());
      StringBuilder fileContents = new StringBuilder();
      for (IFileChangeChunk changeChunk : pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges()
          .getChangeChunks().blockingFirst().getNewValue())
      {
        // use "OLD" side here since the fork-point is the final result in the manual version
        fileContents.append(changeChunk.getLines(EChangeSide.OLD));
      }
      _writeToFile(fileContents.toString(), pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getEncoding(EChangeSide.OLD), pMergeDiff, selectedFile);
    }
  }

  /**
   * @param pFileContents      Contents that should be written to the file
   * @param pCharset           Charset used to write pFileContents to disk (gets transferred from String to byte array)
   * @param pSelectedMergeDiff IMergeDiff that will get resolved by writing the contents to the file
   * @param pSelectedFile      file which should be overridden with pFileContents
   */
  private void _writeToFile(String pFileContents, Charset pCharset, IMergeDiff pSelectedMergeDiff, File pSelectedFile)
  {
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pSelectedFile, false), pCharset))
    {
      writer.write(pFileContents);
      List<IMergeDiff> mergeDiffs = mergeConflictDiffs.blockingFirst();
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

  @Override
  public void discard()
  {
    disposable.dispose();
    selectionDisposable.dispose();
    mergeDiffStatusModel.discard();
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
