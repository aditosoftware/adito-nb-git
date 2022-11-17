package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import de.adito.git.impl.Util;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author m.kaspera, 24.02.2020
 */
public class FileDiffImpl implements IFileDiff
{

  private final BehaviorSubject<Subject<IDeltaTextChangeEvent>> diffTextChangeObservable = BehaviorSubject.createDefault(ReplaySubject.create());
  private final IFileDiffHeader fileDiffHeader;
  private final EditList editList;
  private final IFileContentInfo originalFileContentInfo;
  private final IFileContentInfo newFileContentInfo;
  // in order to preserve the lazy nature of the fileContentInfo, the inital state of the textChangesEvents is only set once it is actually required
  private boolean initialObservableStateSet = false;
  private String oldVersion;
  private String newVersion;
  private List<IChangeDelta> changeDeltas;

  public FileDiffImpl(@NotNull IFileDiffHeader pFileDiffHeader, @NotNull EditList pEditList, @NotNull IFileContentInfo pOriginalFileContentInfo,
                      @NotNull IFileContentInfo pNewFileContentInfo)
  {
    fileDiffHeader = pFileDiffHeader;
    editList = pEditList;
    originalFileContentInfo = pOriginalFileContentInfo;
    newFileContentInfo = pNewFileContentInfo;
  }

  @Override
  public @NotNull IFileDiffHeader getFileHeader()
  {
    return fileDiffHeader;
  }

  @Override
  public IFileContentInfo getFileContentInfo(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFileContentInfo : originalFileContentInfo;
  }

  /**
   * Returns the EditList used to determine the indices for this FileDiff
   *
   * @return EditList used to determine the indices for this FileDiff
   */
  public EditList getEditList()
  {
    return editList;
  }

  @Override
  public Charset getEncoding(@NotNull EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFileContentInfo.getEncoding().get() : originalFileContentInfo.getEncoding().get();
  }

  @Override
  public ELineEnding getUsedLineEndings(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFileContentInfo.getLineEnding().get() : originalFileContentInfo.getLineEnding().get();
  }

  @Override
  public List<IChangeDelta> getChangeDeltas()
  {
    if (changeDeltas == null)
    {
      _initChangeDeltas();
    }
    return changeDeltas;
  }

  @Override
  public void reset()
  {
    _loadFileContent();
    diffTextChangeObservable.onNext(ReplaySubject.create());
    initialObservableStateSet = false;
    _checkInitialObservableState();
  }

  @Override
  public Observable<IDeltaTextChangeEvent> getDiffTextChangeObservable()
  {
    _checkInitialObservableState();
    return diffTextChangeObservable.switchMap(pIDeltaTextChangeEventSubject -> pIDeltaTextChangeEventSubject);
  }

  /**
   * load the initial file content strings for old and newVersion and initialize the deltas
   */
  private void _loadFileContent()
  {
    oldVersion = originalFileContentInfo.getFileContent().get();
    newVersion = newFileContentInfo.getFileContent().get();
    _initChangeDeltas();
  }

  /**
   * check if the observable was initialised by sending a textChangeEvent with the fileContents. If the observable was not initalised, send those events
   */
  private void _checkInitialObservableState()
  {
    if (!initialObservableStateSet)
    {
      _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, originalFileContentInfo.getFileContent().get(), this, EChangeSide.OLD, true));
      _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, newFileContentInfo.getFileContent().get(), this, EChangeSide.NEW, true));
      initialObservableStateSet = true;
    }
  }

  /**
   * adds the IDeltaTextChangeEvent as next element to the diffTextChangeObservable
   *
   * @param pDeltaTextChangeEvent IDeltaTextChangeEvent that should be fired
   */
  private void _fireTextChangeEvent(IDeltaTextChangeEvent pDeltaTextChangeEvent)
  {
    diffTextChangeObservable.getValue().onNext(pDeltaTextChangeEvent);
  }

  @Override
  public @NotNull List<IDeltaTextChangeEvent> acceptDelta(@NotNull IChangeDelta pChangeDelta, boolean pUseWordBasedResolve, boolean pCreateTextEvents, boolean pOverride)
  {
    if (pUseWordBasedResolve)
      return _applyDeltaParts(pChangeDelta, EChangeSide.NEW, pCreateTextEvents);
    else return _applyDelta(pChangeDelta, EChangeSide.NEW, pCreateTextEvents, pOverride);
  }

  @Override
  public @NotNull IDeltaTextChangeEvent appendDeltaText(@NotNull IChangeDelta pChangeDelta)
  {
    DeltaTextChangeEventImpl deltaTextChangeEvent;
    if (oldVersion == null || newVersion == null)
    {
      _loadFileContent();
    }
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      String prefix = oldVersion.substring(0, pChangeDelta.getEndTextIndex(EChangeSide.OLD));
      String infix = newVersion.substring(pChangeDelta.getStartTextIndex(EChangeSide.NEW), pChangeDelta.getEndTextIndex(EChangeSide.NEW));
      String postfix = oldVersion.substring(pChangeDelta.getEndTextIndex(EChangeSide.OLD));
      oldVersion = prefix + infix + postfix;
      deltaTextChangeEvent = new DeltaTextChangeEventImpl(pChangeDelta.getEndTextIndex(EChangeSide.OLD), 0, infix, this, EChangeSide.OLD);
      int lineDifference = pChangeDelta.getEndLine(EChangeSide.NEW) - pChangeDelta.getStartLine(EChangeSide.NEW);
      // exchange delta with updated delta, then propagate additional characters/lines to all deltas that occur later on in the file
      changeDeltas.set(deltaIndex, changeDeltas.get(deltaIndex).appendChange());
      _applyOffsetToFollowingDeltas(deltaIndex, infix.length(), lineDifference, EChangeSide.OLD);
    }
    else
    {
      deltaTextChangeEvent = new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.OLD);
    }
    _fireTextChangeEvent(deltaTextChangeEvent);
    return deltaTextChangeEvent;
  }

  @Override
  public @NotNull List<IDeltaTextChangeEvent> revertDelta(@NotNull IChangeDelta pChangeDelta, boolean pUseWordBasedResolve)
  {
    if (pUseWordBasedResolve)
      return _applyDeltaParts(pChangeDelta, EChangeSide.OLD, true);
    return _applyDelta(pChangeDelta, EChangeSide.OLD, true, false);
  }

  private List<IDeltaTextChangeEvent> _applyDeltaParts(IChangeDelta pChangeDelta, EChangeSide pApplyingSide, boolean pCreateTextEvents)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = new ArrayList<>();
    if (oldVersion == null || newVersion == null)
    {
      _loadFileContent();
    }
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IChangeDelta changedDelta = changeDeltas.get(deltaIndex);
      IOffsetsChange offsetsChange = new OffsetsChange(0, 0);
      for (IDelta linePartChangeDelta : changedDelta.getLinePartChanges())
      {
        offsetsChange = _updateTextWithDelta(linePartChangeDelta, deltaTextChangeEvents, pApplyingSide, offsetsChange, false);
      }

      // exchange delta with updated delta, then propagate additional characters/lines to all deltas that occur later on in the file
      changeDeltas.set(deltaIndex, changedDelta.acceptChange(EChangeSide.invert(pApplyingSide), offsetsChange));
      _applyOffsetToFollowingDeltas(deltaIndex, offsetsChange.getTextOffset(), offsetsChange.getLineOffset(), EChangeSide.invert(pApplyingSide));
    }
    else
    {
      deltaTextChangeEvents.add(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.invert(pApplyingSide)));
    }
    if (pCreateTextEvents)
      deltaTextChangeEvents.forEach(this::_fireTextChangeEvent);
    else
      _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.invert(pApplyingSide)));
    return deltaTextChangeEvents;
  }

  private List<IDeltaTextChangeEvent> _applyDelta(IChangeDelta pChangeDelta, EChangeSide pApplyingSide, boolean pCreateTextEvents, boolean pOverride)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = new ArrayList<>();
    if (oldVersion == null || newVersion == null)
    {
      _loadFileContent();
    }
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IDelta changeDelta = changeDeltas.get(deltaIndex);
      IOffsetsChange offsetsChange = new OffsetsChange(0, 0);
      offsetsChange = _updateTextWithDelta(changeDelta, deltaTextChangeEvents, pApplyingSide, offsetsChange, pOverride);

      int lineDifference = (pChangeDelta.getEndLine(pApplyingSide) - pChangeDelta.getStartLine(pApplyingSide))
          - (pChangeDelta.getEndLine(EChangeSide.invert(pApplyingSide)) - pChangeDelta.getStartLine(EChangeSide.invert(pApplyingSide)));
      // exchange delta with updated delta, then propagate additional characters/lines to all deltas that occur later on in the file
      changeDeltas.set(deltaIndex, changeDeltas.get(deltaIndex).acceptChange(EChangeSide.invert(pApplyingSide), offsetsChange));
      _applyOffsetToFollowingDeltas(deltaIndex, offsetsChange.getTextOffset(), lineDifference, EChangeSide.invert(pApplyingSide));
    }
    else
    {
      deltaTextChangeEvents.add(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.invert(pApplyingSide)));
    }
    // there's only ever one element in the list here, so we can do get(0) instead of a foreach
    if (pCreateTextEvents)
      _fireTextChangeEvent(deltaTextChangeEvents.get(0));
    else
      _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.invert(pApplyingSide)));
    return deltaTextChangeEvents;
  }


  /**
   * @param pChangeDelta           The Delta that should be applied
   * @param pDeltaTextChangeEvents List of IDeltaTextChangeEvent, an IDeltaTextChangeEvent describing the changes that happen due to the Delta is added to this list
   * @param pApplyingSide          EChangeSide that was accepted and should be applied to the other side
   * @param pOffsetsChange         number of characters and newlines that were removed/added so far in case several deltas are combined in one operation.
   *                               Influences the return value
   * @param pOverride              if true, the resulting textChangeEvent replaces the whole delta instead of potentially only adding parts
   * @return number of characters that were removed or added (negative in case of removal) plus pTextDifference
   */
  private IOffsetsChange _updateTextWithDelta(IDelta pChangeDelta, List<IDeltaTextChangeEvent> pDeltaTextChangeEvents, EChangeSide pApplyingSide,
                                              IOffsetsChange pOffsetsChange, boolean pOverride)
  {
    String prefix;
    String infix = "";
    String replacedText;
    boolean isChangeNewVersion = pApplyingSide == EChangeSide.OLD;
    int changedSideLength = EChangeSide.invert(pApplyingSide) == EChangeSide.NEW ? newVersion.length() : oldVersion.length();
    int appliedStartTextIndex = pChangeDelta.getStartTextIndex(pApplyingSide);
    int appliedEndTextIndex = pChangeDelta.getEndTextIndex(pApplyingSide);
    int changedStartTextIndex = pChangeDelta.getStartTextIndex(EChangeSide.invert(pApplyingSide)) + pOffsetsChange.getTextOffset();
    int changedEndTextIndex = pChangeDelta.getEndTextIndex(EChangeSide.invert(pApplyingSide)) + pOffsetsChange.getTextOffset();
    int startIndex;
    EChangeType deltaChangeType = pChangeDelta.getChangeType();
    String changedSideString = EChangeSide.invert(pApplyingSide) == EChangeSide.NEW ? newVersion : oldVersion;
    String appliedSideString = EChangeSide.invert(pApplyingSide) == EChangeSide.NEW ? oldVersion : newVersion;
    boolean isPointChange = !pOverride && ((isChangeNewVersion && deltaChangeType == EChangeType.ADD) || (!isChangeNewVersion && deltaChangeType == EChangeType.DELETE));
    boolean isPointChangeReverse = !pOverride && ((!isChangeNewVersion && deltaChangeType == EChangeType.ADD) || (isChangeNewVersion && deltaChangeType == EChangeType.DELETE));
    boolean isPointChangeAtEOL = safeIsNewlines(appliedEndTextIndex - 1, appliedSideString, changedEndTextIndex - 1, changedSideString)
        && ((deltaChangeType == EChangeType.ADD && isChangeNewVersion) || (deltaChangeType == EChangeType.DELETE && !isChangeNewVersion));
    // get the text before the changed lines
    // the if statement here may be true if e.g. the last line does not have a newline, yet the other side has modified or added lines beyond that
    if (changedSideLength < changedStartTextIndex)
    {
      prefix = changedSideString;
      infix = "\n";
      startIndex = changedSideLength;
    }
    else
    {
      startIndex = Math.max(0, changedStartTextIndex);
      if (isPointChangeAtEOL)
        startIndex = Math.max(0, startIndex - 1);
      prefix = changedSideString.substring(0, startIndex);
    }
    // calculate the changed text. ADD and DELETE have a special treatment here, because they are changes that cover only a point on one side of the change (e.g. an
    // insert happens between characters, doesn't affect the surrounding characters). To make highlighting easier, the indices of the ChangeDelta do not cover that
    // behaviour -> special treatment here
    if (isPointChange)
      infix = "";
    else
      infix += appliedSideString.substring(appliedStartTextIndex, appliedEndTextIndex);
    if (isPointChangeReverse)
      replacedText = "";
    else
      replacedText = changedSideString.substring(changedStartTextIndex, changedEndTextIndex);
    int postFixStartIndex;
    int textEventRemovalLength;
    if (isPointChangeReverse)
    {
      postFixStartIndex = Math.min(changedSideLength, changedStartTextIndex);
      textEventRemovalLength = 0;
    }
    else if (isPointChangeAtEOL)
    {
      postFixStartIndex = Math.min(changedSideLength, changedEndTextIndex - 1);
      textEventRemovalLength = changedEndTextIndex - changedStartTextIndex;
    }
    else
    {
      postFixStartIndex = Math.min(changedSideLength, changedEndTextIndex);
      textEventRemovalLength = changedEndTextIndex - changedStartTextIndex;
    }
    String postFix = changedSideString.substring(postFixStartIndex);

    // build new version from sum of prefix + infix + postfix
    if (isChangeNewVersion)
      newVersion = prefix + infix + postFix;
    else
      oldVersion = prefix + infix + postFix;
    pDeltaTextChangeEvents.add(new DeltaTextChangeEventImpl(startIndex, textEventRemovalLength, infix, this, EChangeSide.invert(pApplyingSide)));
    // calculate index differences for the following deltas
    int lineEndingDiff = (infix.split("\n", -1).length - 1) - (replacedText.split("\n", -1).length - 1);
    return pOffsetsChange.combineWith(infix.length() - textEventRemovalLength, lineEndingDiff);
  }

  /**
   * checks if the character at the given indices is a newline for both strings
   *
   * @param pIndexOne     index for the character of the first string
   * @param pFirstString  String whose character at index pIndexOne should be checked
   * @param pIndexTwo     index for the character of the second string
   * @param pSecondString String whose character at index pIndexTwo should be checked
   * @return true if both characters are a UNIX newline
   */
  private boolean safeIsNewlines(int pIndexOne, String pFirstString, int pIndexTwo, String pSecondString)
  {
    return Util.safeIsCharAt(pFirstString, pIndexOne, '\n')
        && Util.safeIsCharAt(pSecondString, pIndexTwo, '\n');
  }

  @Override
  public void discardDelta(@NotNull IChangeDelta pChangeDelta)
  {
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      changeDeltas.set(deltaIndex, changeDelta.discardChange());
    }
    // empty change on both, to notify both sides to adjust the highlights
    _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.OLD));
    _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.NEW));
  }

  @Override
  public void setResolved(@NotNull IChangeDelta pChangeDelta)
  {
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      if (changeDelta.getConflictType() == EConflictType.CONFLICTING || changeDelta.getConflictType() == EConflictType.NONE)
        throw new IllegalArgumentException("ChangeDelta is of a non-resolvable type " + pChangeDelta);
      changeDeltas.set(deltaIndex, changeDelta.setChangeStatus(new ChangeStatusImpl(EChangeStatus.ACCEPTED, changeDelta.getChangeType(), changeDelta.getConflictType())));
    }
    // empty change on both, to notify both sides to adjust the highlights
    _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.OLD));
    _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.NEW));
  }

  @Override
  public void processTextEvent(int pOffset, int pLength, @Nullable String pText, EChangeSide pChangeSide, boolean pTrySnapToDelta, boolean pPropagateChange)
  {
    if (newVersion == null || oldVersion == null)
      _loadFileContent();
    String prefix = pChangeSide == EChangeSide.NEW ? newVersion.substring(0, pOffset) : oldVersion.substring(0, pOffset);
    if (pText == null)
    {
      String postfix = pChangeSide == EChangeSide.NEW ? newVersion.substring(pOffset + pLength) : oldVersion.substring(pOffset + pLength);
      _processDeleteEvent(pOffset, pLength, pChangeSide);
      if (pChangeSide == EChangeSide.NEW)
        newVersion = prefix + postfix;
      else
        oldVersion = prefix + postfix;
    }
    else
    {
      int affectedDelta = -1;
      if (pLength > 0)
      {
        String postfix = pChangeSide == EChangeSide.NEW ? newVersion.substring(pOffset + pLength) : oldVersion.substring(pOffset + pLength);
        affectedDelta = _processDeleteEvent(pOffset, pLength, pChangeSide);
        if (pChangeSide == EChangeSide.NEW)
          newVersion = prefix + postfix;
        else
          oldVersion = prefix + postfix;
      }
      String postfix = pChangeSide == EChangeSide.NEW ? newVersion.substring(pOffset) : oldVersion.substring(pOffset);
      _processInsertEvent(pOffset, pText, pChangeSide, affectedDelta, pTrySnapToDelta);
      if (pChangeSide == EChangeSide.NEW)
        newVersion = prefix + pText + postfix;
      else
        oldVersion = prefix + pText + postfix;
    }
    if (pPropagateChange)
    {
      _fireTextChangeEvent(new DeltaTextChangeEventImpl(pOffset, pLength, pText, this, pChangeSide));
    }
    else
    // signal an empty change for UI updates, this method should be called in response to an update in a document or similar, not the other way round
    {
      _fireTextChangeEvent(new DeltaTextChangeEventImpl(0, 0, "", this, pChangeSide));
    }
  }

  /**
   * @param pOffset        index of the insertion event
   * @param pText          text that was inserted
   * @param pModifiedDelta indicates that the insert is part of a modify operation, and that a delta has been affected by the remove part of the removal part.
   *                       If the delta is e.g. a one-line change and the line is replaced by the modify operation, the delta should still span that line.
   *                       The argument is -1 if not a modify operation or no delta was affected
   */
  private void _processInsertEvent(int pOffset, @NotNull String pText, EChangeSide pChangeSide, int pModifiedDelta, boolean pTrySnapToDelta)
  {
    int lineOffset;
    lineOffset = pText.split("\n", -1).length - 1;
    BiPredicate<Integer, IChangeDelta> eval;
    if (pTrySnapToDelta)
      eval = (pOffsetEval, pChunk) -> pOffsetEval <= pChunk.getEndTextIndex(pChangeSide);
    else
      eval = (pOffsetEval, pChunk) -> pOffsetEval < pChunk.getEndTextIndex(pChangeSide);

    for (int index = 0; index < getChangeDeltas().size(); index++)
    {
      IChangeDelta currentDelta = changeDeltas.get(index);
      if (eval.test(pOffset, currentDelta) || (pModifiedDelta == index) && pOffset == currentDelta.getEndTextIndex(pChangeSide))
      {
        if (pOffset >= currentDelta.getStartTextIndex(pChangeSide))
        {
          // see IChangeDelta.processTextEvent case INSERT 3
          changeDeltas.set(index, currentDelta.processTextEvent(pOffset, pText.length(), 0, lineOffset, true, pChangeSide));
          _applyOffsetToFollowingDeltas(index, pText.length(), lineOffset, pChangeSide);
        }
        else
        {
          // see IChangeDelta.processTextEvent case INSERT 1
          _applyOffsetToFollowingDeltas(index - 1, pText.length(), lineOffset, pChangeSide);
        }
        break;
      }
    }
  }

  /**
   * @param pOffset index of the start of the delete event (first deleted character)
   * @param pLength number of deleted characters
   */
  private int _processDeleteEvent(int pOffset, int pLength, EChangeSide pChangeSide)
  {
    int affectedIndex = -1;
    int lineOffset;
    String infix = pChangeSide == EChangeSide.NEW ? newVersion.substring(pOffset, pOffset + pLength) : oldVersion.substring(pOffset, pOffset + pLength);
    lineOffset = -(infix.split("\n", -1).length - 1);
    for (int index = 0; index < getChangeDeltas().size(); index++)
    {
      IChangeDelta currentDelta = changeDeltas.get(index);
      if (pOffset < currentDelta.getEndTextIndex(pChangeSide))
      {
        // check if the event may affect a delta that comes after this one
        boolean isChangeBiggerThanDelta = pOffset + pLength > currentDelta.getEndTextIndex(pChangeSide);
        if (pOffset + pLength < currentDelta.getStartTextIndex(pChangeSide))
        {
          // see IChangeDelta.processTextEvent case DELETE 5
          changeDeltas.set(index, currentDelta.applyOffset(lineOffset, -pLength, pChangeSide));
        }
        else
        {
          // part of the delete operation text that is in front of the chunk
          String deletedBefore = pChangeSide == EChangeSide.NEW ? newVersion.substring(Math.min(currentDelta.getStartTextIndex(pChangeSide), pOffset),
                                                                                       currentDelta.getStartTextIndex(pChangeSide))
              : oldVersion.substring(Math.min(currentDelta.getStartTextIndex(pChangeSide), pOffset),
                                     currentDelta.getStartTextIndex(pChangeSide));
          // part of the delete operation text that is inside the chunk
          String deletedOfChunk = pChangeSide == EChangeSide.NEW ? newVersion.substring(Math.max(currentDelta.getStartTextIndex(pChangeSide), pOffset),
                                                                                        Math.min(currentDelta.getEndTextIndex(pChangeSide), pOffset + pLength))
              : oldVersion.substring(Math.max(currentDelta.getStartTextIndex(pChangeSide), pOffset),
                                     Math.min(currentDelta.getEndTextIndex(pChangeSide), pOffset + pLength));
          changeDeltas.set(index, currentDelta.processTextEvent(pOffset, -pLength, -(deletedBefore.split("\n", -1).length - 1),
                                                                -(deletedOfChunk.split("\n", -1).length - 1), false, pChangeSide));
        }
        affectedIndex = index;
        if (!isChangeBiggerThanDelta)
        {
          _applyOffsetToFollowingDeltas(index, -pLength, lineOffset, pChangeSide);
          break;
        }
      }
    }
    return affectedIndex;
  }

  @Override
  public String getText(@NotNull EChangeSide pChangeSide)
  {
    if (oldVersion == null || newVersion == null)
      _loadFileContent();
    return pChangeSide == EChangeSide.NEW ? newVersion : oldVersion;
  }

  @Override
  public @NotNull List<ConflictPair> markConflicting(@NotNull IFileDiff pOtherFileDiff, @NotNull EConflictSide pConflictSide,
                                                     @NotNull ResolveOptionsProvider pResolveOptionsProvider)
  {
    List<ConflictPair> conflictPairs = new ArrayList<>();
    if (oldVersion == null || newVersion == null)
      _loadFileContent();
    for (int index = 0; index < changeDeltas.size(); index++)
    {
      IChangeDelta changeDelta = changeDeltas.get(index);
      for (int otherDiffIndex = 0; otherDiffIndex < pOtherFileDiff.getChangeDeltas().size(); otherDiffIndex++)
      {
        ConflictType conflictType = pOtherFileDiff.getChangeDeltas().get(otherDiffIndex).isConflictingWith(changeDelta, pConflictSide, pResolveOptionsProvider, fileDiffHeader);
        if (conflictType.getConflictType() == EConflictType.CONFLICTING)
        {
          changeDeltas.set(index, changeDelta.setChangeStatus(new ChangeStatusImpl(changeDelta.getChangeStatus(), changeDelta.getChangeType(), conflictType.getConflictType())));
          conflictPairs.add(new ConflictPair(index, otherDiffIndex, conflictType));
          break;
        }
        else if (conflictType.getConflictType() == EConflictType.RESOLVABLE)
        {
          changeDeltas.set(index, changeDelta.setChangeStatus(new ChangeStatusImpl(changeDelta.getChangeStatus(), changeDelta.getChangeType(), conflictType.getConflictType())));
          conflictPairs.add(new ConflictPair(index, otherDiffIndex, conflictType));
          break;
        }
        else if (conflictType.getConflictType() == EConflictType.SAME)
        {
          conflictPairs.add(new ConflictPair(index, otherDiffIndex, conflictType));
          break;
        }
      }
    }
    return conflictPairs;
  }

  @Override
  public @NotNull File getFile()
  {
    String filePath = fileDiffHeader.getAbsoluteFilePath();
    if (filePath == null)
      filePath = fileDiffHeader.getFilePath();
    return new File(filePath);
  }

  @Override
  public @NotNull File getFile(@NotNull EChangeSide pChangeSide)
  {
    return new File(fileDiffHeader.getFilePath(pChangeSide));
  }

  @Override
  public @NotNull EChangeType getChangeType()
  {
    return fileDiffHeader.getChangeType();
  }


  /**
   * applies the given text and lineoffsets to the deltas for indices after pDeltaIndex
   *
   * @param pDeltaIndex     index for the list of deltas, given index is exclusive
   * @param pTextDifference offset that will be added to the textOffsets
   * @param pLineDifference offset that will be added to the lineOffsets
   */
  private void _applyOffsetToFollowingDeltas(int pDeltaIndex, int pTextDifference, int pLineDifference, EChangeSide pChangeSide)
  {
    for (int index = pDeltaIndex + 1; index < changeDeltas.size(); index++)
    {
      changeDeltas.set(index, changeDeltas.get(index).applyOffset(pLineDifference, pTextDifference, pChangeSide));
    }
  }

  /**
   * Inits or resets the list of changeDeltas
   */
  private void _initChangeDeltas()
  {
    changeDeltas = LineIndexDiffUtil.getTextOffsets(originalFileContentInfo.getFileContent().get(), newFileContentInfo.getFileContent().get(), editList,
                                                    new ChangeDeltaImplFactory());
  }

  /**
   * Factory, creates ChangeDeltaImpls from an Edit and a ChangeDeltaTextOffsets data object
   */
  private class ChangeDeltaImplFactory implements IChangeDeltaFactory<IChangeDelta>
  {

    @NotNull
    @Override
    public IChangeDelta createDelta(@NotNull Edit pEdit, @NotNull ChangeDeltaTextOffsets pDeltaTextOffsets)
    {
      return new ChangeDeltaImpl(pEdit, new ChangeStatusImpl(EChangeStatus.PENDING, EnumMappings.toChangeType(pEdit.getType()), EConflictType.NONE),
                                 pDeltaTextOffsets, FileDiffImpl.this::getText);
    }
  }

}
