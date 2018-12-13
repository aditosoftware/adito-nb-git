package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.*;
import de.adito.git.gui.IDiscardable;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.MergeDiffStatusModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
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
  private Observable<Optional<IRepository>> repository;
  private final Subject<List<IMergeDiff>> mergeConflictDiffs;
  private final JTable mergeConflictTable = new JTable();
  private final Observable<Optional<IMergeDiff>> selectedMergeDiffObservable;
  private final MergeDiffStatusModel mergeDiffStatusModel;
  private final Disposable disposable;

  @Inject
  MergeConflictDialog(IDialogProvider pDialogProvider, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor,
                      @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<IMergeDiff> pMergeConflictDiffs)
  {
    dialogProvider = pDialogProvider;
    isValidDescriptor = pIsValidDescriptor;
    repository = pRepository;
    ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(mergeConflictTable.getSelectionModel());
    mergeConflictTable.setSelectionModel(observableListSelectionModel);
    mergeConflictDiffs = BehaviorSubject.createDefault(pMergeConflictDiffs);
    Observable<Optional<IFileStatus>> obs = pRepository
        .flatMap(pRepo -> pRepo.orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG)).getStatus());
    Observable<List<IMergeDiff>> mergeDiffListObservable = Observable.combineLatest(obs,
                                                                                    mergeConflictDiffs, (pStatus, pMergeDiffs) -> pMergeDiffs.stream()
            .filter(pMergeDiff -> pStatus.map(IFileStatus::getConflicting).orElse(Collections.emptySet())
                .contains(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW)))
            .collect(Collectors.toList()));
    selectedMergeDiffObservable = Observable
        .combineLatest(observableListSelectionModel.selectedRows(), mergeDiffListObservable, (pSelectedRows, pMergeDiffList) -> {
          // if either the list or selection is null, more than one element is selected or the list has 0 elements
          if (pSelectedRows == null || pMergeDiffList == null || pSelectedRows.length != 1 || pMergeDiffList.isEmpty())
          {
            return Optional.empty();
            // if no element is selected (and at least one element is in the list, this follows from the if above)
          }
          return Optional.of(pMergeDiffList.get(pSelectedRows[0]));
        });
    disposable = mergeDiffListObservable.subscribe(pList -> isValidDescriptor.setValid(pList.isEmpty()));
    mergeDiffStatusModel = new MergeDiffStatusModel(mergeDiffListObservable);
    _initGui();
  }

  private void _initGui()
  {
    setLayout(new BorderLayout());
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
    JButton manualMergeButton = new JButton("Manual Merge");
    JButton acceptYoursButton = new JButton("Accept YOURS");
    JButton acceptTheirsButton = new JButton("Accept THEIRS");
    manualMergeButton.addActionListener(e -> _doManualResolve(selectedMergeDiffObservable));
    acceptYoursButton.addActionListener(e -> _acceptDefaultVersion(selectedMergeDiffObservable, IMergeDiff.CONFLICT_SIDE.YOURS));
    acceptTheirsButton.addActionListener(e -> _acceptDefaultVersion(selectedMergeDiffObservable, IMergeDiff.CONFLICT_SIDE.THEIRS));
    buttonPanel.add(manualMergeButton);
    buttonPanel.add(acceptYoursButton);
    buttonPanel.add(acceptTheirsButton);
    add(buttonPanel, BorderLayout.EAST);
    mergeConflictTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mergeConflictTable.setModel(mergeDiffStatusModel);
    add(mergeConflictTable, BorderLayout.WEST);
  }

  private void _doManualResolve(Observable<Optional<IMergeDiff>> pSelectedMergeDiffObservable)
  {
    Optional<IMergeDiff> mergeDiffOptional = pSelectedMergeDiffObservable.blockingFirst();
    mergeDiffOptional.ifPresent(iMergeDiff -> {
      if (dialogProvider.showMergeConflictResolutionDialog(iMergeDiff).isPressedOk())
        _acceptManualVersion(iMergeDiff);
    });
  }

  private void _acceptDefaultVersion(Observable<Optional<IMergeDiff>> pSelectedMergeDiff, IMergeDiff.CONFLICT_SIDE pConflictSide)
  {
    Optional<IMergeDiff> mergeDiffOptional = pSelectedMergeDiff.blockingFirst();
    if (mergeDiffOptional.isPresent())
    {
      IMergeDiff selectedMergeDiff = mergeDiffOptional.get();
      File selectedFile = new File(repository
                                       .blockingFirst()
                                       .orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG))
                                       .getTopLevelDirectory(), selectedMergeDiff.getDiff(pConflictSide).getFilePath(EChangeSide.NEW));
      StringBuilder fileContents = new StringBuilder();
      for (IFileChangeChunk changeChunk : selectedMergeDiff.getDiff(pConflictSide).getFileChanges().getChangeChunks().blockingFirst())
      {
        // BLines is always the "new" version of the file, in comparison to the fork point
        fileContents.append(changeChunk.getBLines());
      }
      _writeToFile(fileContents.toString(), selectedMergeDiff, selectedFile);
    }
  }

  private void _acceptManualVersion(IMergeDiff pIMergeDiff)
  {
    File selectedFile = new File(repository
                                     .blockingFirst()
                                     .orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG))
                                     .getTopLevelDirectory(), pIMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW));
    StringBuilder fileContents = new StringBuilder();
    for (IFileChangeChunk changeChunk : pIMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks().blockingFirst())
    {
      // BLines is always the "new" version of the file, in comparison to the fork point
      fileContents.append(changeChunk.getALines());
    }
    _writeToFile(fileContents.toString(), pIMergeDiff, selectedFile);
  }

  private void _writeToFile(String pFileContents, IMergeDiff pSelectedMergeDiff, File pSelectedFile)
  {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(pSelectedFile, false)))
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
      repository
          .blockingFirst()
          .orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG))
          .add(Collections.singletonList(pSelectedFile));
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
