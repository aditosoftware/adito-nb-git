package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.IRepository;
import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IFileChangeChunk;
import de.adito.git.api.data.IFileStatus;
import de.adito.git.api.data.IMergeDiff;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.tableModels.MergeDiffStatusModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 25.10.2018
 */
class MergeConflictDialog extends JPanel {

    private final IDialogProvider dialogFactory;
    private Observable<Optional<IRepository>> repository;
    private final Subject<List<IMergeDiff>> mergeConflictDiffs;
    private final Observable<List<IMergeDiff>> mergeDiffListObservable;
    private final JTable mergeConflictTable = new JTable();
    private final Observable<Optional<IMergeDiff>> selectedMergeDiffObservable;

    @Inject
    MergeConflictDialog(IDialogProvider pDialogFactory, @Assisted Observable<Optional<IRepository>> pRepository, @Assisted List<IMergeDiff> pMergeConflictDiffs) {
        dialogFactory = pDialogFactory;
        repository = pRepository;
        ObservableListSelectionModel observableListSelectionModel = new ObservableListSelectionModel(mergeConflictTable.getSelectionModel());
        mergeConflictTable.setSelectionModel(observableListSelectionModel);
        mergeConflictDiffs = BehaviorSubject.createDefault(pMergeConflictDiffs);
        Observable<IFileStatus> obs = pRepository.flatMap(pRepo -> pRepo.orElseThrow(() -> new RuntimeException("no valid repository found")).getStatus());
        mergeDiffListObservable = Observable.combineLatest(obs, mergeConflictDiffs, (pStatus, pMergeDiffs) -> pMergeDiffs.stream()
                .filter(pMergeDiff -> pStatus.getConflicting().contains(pMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW)))
                .collect(Collectors.toList()));
        selectedMergeDiffObservable = Observable.combineLatest(observableListSelectionModel.selectedRows(), mergeDiffListObservable, (pSelectedRows, pMergeDiffList) -> {
            // if either the list or selection is null, more than one element is selected or the list has 0 elements
            if (pSelectedRows == null || pMergeDiffList == null || pSelectedRows.length != 1 || pMergeDiffList.size() == 0) {
                return Optional.empty();
                // if no element is selected (and at least one element is in the list, this follows from the if above)
            }
            return Optional.of(pMergeDiffList.get(pSelectedRows[0]));
        });
        _initGui();
    }

    private void _initGui() {
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
        mergeConflictTable.setModel(new MergeDiffStatusModel(mergeDiffListObservable));
        add(mergeConflictTable, BorderLayout.WEST);
    }

    private void _doManualResolve(Observable<Optional<IMergeDiff>> pSelectedMergeDiffObservable) {
        Optional<IMergeDiff> mergeDiffOptional = pSelectedMergeDiffObservable.blockingFirst();
        mergeDiffOptional.ifPresent(iMergeDiff -> {
            if (dialogFactory.showMergeConflictResolutionDialog(iMergeDiff).isPressedOk())
                _acceptManualVersion(iMergeDiff);
        });
    }

    private void _acceptDefaultVersion(Observable<Optional<IMergeDiff>> pSelectedMergeDiff, IMergeDiff.CONFLICT_SIDE conflictSide) {
        Optional<IMergeDiff> mergeDiffOptional = pSelectedMergeDiff.blockingFirst();
        if (mergeDiffOptional.isPresent()) {
            IMergeDiff selectedMergeDiff = mergeDiffOptional.get();
            File selectedFile = new File(repository
                    .blockingFirst()
                    .orElseThrow(() -> new RuntimeException("no valid repository found"))
                    .getTopLevelDirectory(), selectedMergeDiff.getDiff(conflictSide).getFilePath(EChangeSide.NEW));
            StringBuilder fileContents = new StringBuilder();
            for (IFileChangeChunk changeChunk : selectedMergeDiff.getDiff(conflictSide).getFileChanges().getChangeChunks().blockingFirst()) {
                // BLines is always the "new" version of the file, in comparison to the fork point
                fileContents.append(changeChunk.getBLines());
            }
            _writeToFile(fileContents.toString(), selectedMergeDiff, selectedFile);
        }
    }

    private void _acceptManualVersion(IMergeDiff iMergeDiff) {
        File selectedFile = new File(repository
                .blockingFirst()
                .orElseThrow(() -> new RuntimeException("no valid repository found"))
                .getTopLevelDirectory(), iMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFilePath(EChangeSide.NEW));
        StringBuilder fileContents = new StringBuilder();
        for (IFileChangeChunk changeChunk : iMergeDiff.getDiff(IMergeDiff.CONFLICT_SIDE.YOURS).getFileChanges().getChangeChunks().blockingFirst()) {
            // BLines is always the "new" version of the file, in comparison to the fork point
            fileContents.append(changeChunk.getALines());
        }
        _writeToFile(fileContents.toString(), iMergeDiff, selectedFile);
    }

    private void _writeToFile(String pFileContents, IMergeDiff selectedMergeDiff, File pSelectedFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pSelectedFile, false))) {
            writer.write(pFileContents);
            List<IMergeDiff> mergeDiffs = mergeConflictDiffs.blockingFirst();
            mergeDiffs.remove(selectedMergeDiff);
            mergeConflictDiffs.onNext(mergeDiffs);
            repository
                    .blockingFirst()
                    .orElseThrow(() -> new RuntimeException("no valid repository found"))
                    .add(Collections.singletonList(pSelectedFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
