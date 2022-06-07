package de.adito.git.gui.dialogs;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.api.*;
import de.adito.git.api.data.*;
import de.adito.git.api.data.diff.*;
import de.adito.git.api.exception.AditoGitException;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.gui.PopupMouseListener;
import de.adito.git.gui.dialogs.results.IMergeConflictResolutionDialogResult;
import de.adito.git.gui.quicksearch.*;
import de.adito.git.gui.rxjava.ObservableListSelectionModel;
import de.adito.git.gui.sequences.MergeConflictSequence;
import de.adito.git.gui.swing.*;
import de.adito.git.gui.tablemodels.MergeDiffStatusModel;
import de.adito.git.impl.Util;
import de.adito.util.reactive.cache.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.kaspera 25.10.2018
 */
class MergeConflictDialog extends AditoBaseDialog<Object> implements IDiscardable
{

  private static final String NO_REPO_ERROR_MSG = Util.getResource(MergeConflictDialog.class, "noValidRepoMsg");
  private static final String PREF_STORE_SIZE_KEY = Util.getResource(MergeConflictResolutionDialog.class, "mergePanelSizeKey");

  private final IDialogProvider dialogProvider;
  private final IAsyncProgressFacade progressFacade;
  private final INotifyUtil notifyUtil;
  private final IDialogDisplayer.IDescriptor isValidDescriptor;
  private final boolean onlyConflicting;
  private final IRepository repository;
  private final Subject<List<IMergeData>> mergeConflictDiffs;
  private final SearchableTable mergeConflictTable = new SearchableTable(this);
  private final JButton manualMergeButton = new JButton("Manual Merge");
  private final JButton acceptYoursButton = new JButton("Accept Yours");
  private final JButton acceptTheirsButton = new JButton("Accept Theirs");
  private final JButton autoResolveButton = new JButton("Auto-Resolve");
  private final MergeDiffStatusModel mergeDiffStatusModel;
  private final ObservableCache observableCache = new ObservableCache();
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final ObservableListSelectionModel observableListSelectionModel;
  private final IPrefStore prefStore;

  @Inject
  MergeConflictDialog(IPrefStore pPrefStore, IDialogProvider pDialogProvider, IAsyncProgressFacade pProgressFacade, IQuickSearchProvider pQuickSearchProvider,
                      INotifyUtil pNotifyUtil, @Assisted IDialogDisplayer.IDescriptor pIsValidDescriptor, @Assisted Observable<Optional<IRepository>> pRepository,
                      @Assisted IMergeDetails pMergeDetails, @Assisted("onlyConflictingFlag") boolean pOnlyConflicting,
                      @Assisted("autoResolveFlag") boolean pShowAutoResolve)
  {
    prefStore = pPrefStore;
    dialogProvider = pDialogProvider;
    progressFacade = pProgressFacade;
    notifyUtil = pNotifyUtil;
    isValidDescriptor = pIsValidDescriptor;
    onlyConflicting = pOnlyConflicting;
    disposables.add(new ObservableCacheDisposable(observableCache));
    repository = pRepository.blockingFirst().orElseThrow(() -> new RuntimeException(NO_REPO_ERROR_MSG));
    observableListSelectionModel = new ObservableListSelectionModel(mergeConflictTable.getSelectionModel());
    mergeConflictTable.setSelectionModel(observableListSelectionModel);
    mergeConflictDiffs = BehaviorSubject.createDefault(pMergeDetails.getMergeConflicts());
    disposables.add(_observeSelectedMergeDiff().subscribe(pSelectedMergeDiffs -> {
      manualMergeButton.setEnabled(pSelectedMergeDiffs.map(pList -> pList.size() == 1).orElse(false));
      acceptYoursButton.setEnabled(pSelectedMergeDiffs.map(pList -> !pList.isEmpty()).orElse(false));
      acceptTheirsButton.setEnabled(pSelectedMergeDiffs.map(pList -> !pList.isEmpty()).orElse(false));
    }));
    disposables.add(_observeMergeDiffList().subscribe(pList -> isValidDescriptor.setValid(pList.isEmpty())));
    mergeDiffStatusModel = new MergeDiffStatusModel(_observeMergeDiffList(), pMergeDetails);
    _initGui(pMergeDetails, pShowAutoResolve, pQuickSearchProvider);
  }

  private void _initGui(@NotNull IMergeDetails pMergeDetails, boolean pShowAutoResolve, IQuickSearchProvider pQuickSearchProvider)
  {
    setLayout(new BorderLayout(5, 10));
    setPreferredSize(ComponentResizeListener._getPreferredSize(prefStore, PREF_STORE_SIZE_KEY, new Dimension(800, 600)));
    addComponentListener(new ComponentResizeListener(prefStore, PREF_STORE_SIZE_KEY));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
    manualMergeButton.addActionListener(e -> _doManualResolve(_observeSelectedMergeDiff(), pMergeDetails.getYoursOrigin(), pMergeDetails.getTheirsOrigin()));
    acceptYoursButton.addActionListener(e -> _acceptDefaultVersion(EConflictSide.YOURS));
    acceptTheirsButton.addActionListener(e -> _acceptDefaultVersion(EConflictSide.THEIRS));
    manualMergeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) manualMergeButton.getMaximumSize().getHeight()));
    acceptYoursButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) acceptYoursButton.getMaximumSize().getHeight()));
    acceptTheirsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) acceptTheirsButton.getMaximumSize().getHeight()));
    autoResolveButton.addActionListener(e -> {
      MergeConflictSequence.performAutoResolve(mergeConflictDiffs.blockingFirst(List.of()), repository, progressFacade, notifyUtil);
      autoResolveButton.setEnabled(false);
    });
    autoResolveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) autoResolveButton.getMaximumSize().getHeight()));
    buttonPanel.setPreferredSize(new Dimension(120, 60));
    buttonPanel.add(manualMergeButton);
    buttonPanel.add(Box.createVerticalStrut(10));
    buttonPanel.add(acceptYoursButton);
    buttonPanel.add(Box.createVerticalStrut(5));
    buttonPanel.add(acceptTheirsButton);
    if (pShowAutoResolve)
    {
      buttonPanel.add(Box.createVerticalStrut(10));
      buttonPanel.add(new JSeparator());
      buttonPanel.add(Box.createVerticalStrut(10));
      buttonPanel.add(autoResolveButton);
    }
    buttonPanel.add(Box.createVerticalStrut(Integer.MAX_VALUE));
    add(buttonPanel, BorderLayout.EAST);
    mergeConflictTable.setModel(mergeDiffStatusModel);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn("Filename")).setPreferredWidth(230);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn("Filepath")).setPreferredWidth(230);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn(pMergeDetails.getYoursOrigin())).setPreferredWidth(120);
    mergeConflictTable.getColumnModel().getColumn(mergeDiffStatusModel.findColumn(pMergeDetails.getTheirsOrigin())).setPreferredWidth(120);
    mergeConflictTable.setDefaultRenderer(Object.class, new MergeDiffTableCellRenderer());
    PopupMouseListener popupMouseListener = new PopupMouseListener(JPopupMenu::new);
    popupMouseListener.setDoubleClickAction(new ManualResolveAction(pMergeDetails));
    mergeConflictTable.addMouseListener(popupMouseListener);
    JScrollPane mergeConflictTableScrollPane = new JScrollPane(mergeConflictTable);
    add(mergeConflictTableScrollPane, BorderLayout.CENTER);
    pQuickSearchProvider.attach(this, BorderLayout.SOUTH, new QuickSearchCallbackImpl(mergeConflictTable, List.of(0)));
  }

  @NotNull
  private Observable<Optional<List<IMergeData>>> _observeSelectedMergeDiff()
  {
    return observableCache.calculateParallel("selectedMergeDiff", () -> Observable
        .combineLatest(observableListSelectionModel.selectedRows(), _observeMergeDiffList(), (pSelectedRows, pMergeDiffList) -> {
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
        }));
  }

  @NotNull
  private Observable<List<IMergeData>> _observeMergeDiffList()
  {
    return observableCache.calculateParallel("mergeDiffList", () -> Observable
        .combineLatest(repository.getStatus(), mergeConflictDiffs, (pStatus, pMergeDiffs) -> pMergeDiffs.stream()
            .filter(pMergeDiff -> !onlyConflicting || pStatus
                .map(IFileStatus::getConflicting)
                .orElse(Collections.emptySet())
                .stream().anyMatch(pFilePath -> IFileDiff.isSameFile(pFilePath, pMergeDiff.getDiff(EConflictSide.YOURS))))
            .collect(Collectors.toList())));
  }

  /**
   * @param pSelectedMergeDiffObservable Observable optional list (should be of size 1) of IMergeDatas whose conflicts should be manually resolved
   * @param pYoursOrigin                 name of the branch/commit that is the origin of the yours version
   * @param pTheirsOrigin                name of the branch/commit that is the origin of the theirs version
   */
  private void _doManualResolve(Observable<Optional<List<IMergeData>>> pSelectedMergeDiffObservable, @NotNull String pYoursOrigin, @NotNull String pTheirsOrigin)
  {
    Optional<List<IMergeData>> mergeDiffOptional = pSelectedMergeDiffObservable.blockingFirst();
    mergeDiffOptional.ifPresent(pMergeDatas -> {
      if (pMergeDatas.size() == 1)
      {
        IMergeConflictResolutionDialogResult<?, ?> result = dialogProvider.showMergeConflictResolutionDialog(pMergeDatas.get(0), pYoursOrigin, pTheirsOrigin);
        if (result.isAcceptChanges())
        {
          _acceptManualVersionMarkResolved(pMergeDatas);
        }
        else if (result.getSelectedButton().equals(EButtons.ACCEPT_YOURS))
        {
          _acceptDefaultVersion(EConflictSide.YOURS);
          pMergeDatas.forEach(this::_removeFromMergeConflicts);
        }
        else if (result.getSelectedButton().equals(EButtons.ACCEPT_THEIRS))
        {
          _acceptDefaultVersion(EConflictSide.THEIRS);
          pMergeDatas.forEach(this::_removeFromMergeConflicts);
        }
        else if (result.getSelectedButton() == EButtons.ACCEPT_REMAINING)
        {
          MergeConflictResolutionDialog.acceptNonConflictingDeltas(pMergeDatas.get(0), EConflictSide.THEIRS);
          MergeConflictResolutionDialog.acceptNonConflictingDeltas(pMergeDatas.get(0), EConflictSide.YOURS);
          _acceptManualVersionMarkResolved(pMergeDatas);
        }
        else if (result.getSelectedButton() == EButtons.ACCEPT_AS_IS)
        {
          _acceptManualVersionMarkResolved(pMergeDatas);
        }
        else if (result.getSelectedButton() == EButtons.CANCEL)
          pMergeDatas.forEach(IMergeData::reset);
      }
    });
  }

  private void _acceptManualVersionMarkResolved(List<IMergeData> pMergeDatas)
  {
    File resolvedFile = MergeConflictSequence.acceptManualVersion(pMergeDatas.get(0), repository);
    try
    {
      if (resolvedFile != null)
        repository.add(Collections.singletonList(resolvedFile));
      pMergeDatas.forEach(this::_removeFromMergeConflicts);
    }
    catch (AditoGitException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  private void _acceptDefaultVersion(EConflictSide pConflictSide)
  {
    Optional<List<IMergeData>> mergeDiffOptional = _observeSelectedMergeDiff().blockingFirst(Optional.empty());
    mergeDiffOptional.ifPresent(pIMergeData -> {
      MergeConflictSequence.acceptDefaultVersion(pIMergeData, pConflictSide, repository);
      mergeDiffOptional.get().forEach(this::_removeFromMergeConflicts);
    });
  }

  private void _removeFromMergeConflicts(IMergeData pMergeData)
  {
    List<IMergeData> mergeDiffs = mergeConflictDiffs.blockingFirst();
    mergeDiffs.remove(pMergeData);
    mergeConflictDiffs.onNext(mergeDiffs);
  }

  @Override
  public void discard()
  {
    disposables.dispose();
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

  private class ManualResolveAction extends AbstractAction
  {
    @NotNull
    private final IMergeDetails mergeDetails;

    public ManualResolveAction(@NotNull IMergeDetails pMergeDetails)
    {
      mergeDetails = pMergeDetails;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      _doManualResolve(_observeSelectedMergeDiff(), mergeDetails.getYoursOrigin(), mergeDetails.getTheirsOrigin());
    }
  }
}
