package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.*;
import de.adito.git.impl.EnumMappings;
import de.adito.git.impl.Util;
import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author m.kaspera, 24.02.2020
 */
public class FileDiffImpl implements IFileDiff
{

  private Subject<IDeltaTextChangeEvent> diffTextChangeObservable = ReplaySubject.create();
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
    oldVersion = originalFileContentInfo.getFileContent().get();
    newVersion = newFileContentInfo.getFileContent().get();
    _initChangeDeltas();
    diffTextChangeObservable = ReplaySubject.create();
    initialObservableStateSet = false;
    _checkInitialObservableState();
  }

  @Override
  public Observable<IDeltaTextChangeEvent> getDiffTextChangeObservable()
  {
    _checkInitialObservableState();
    return diffTextChangeObservable;
  }

  /**
   * check if the observable was initialised by sending a textChangeEvent with the fileContents. If the observable was not initalised, send those events
   */
  private void _checkInitialObservableState()
  {
    if (!initialObservableStateSet)
    {
      diffTextChangeObservable.onNext(new DeltaTextChangeEventImpl(0, 0, originalFileContentInfo.getFileContent().get(), this, EChangeSide.OLD));
      diffTextChangeObservable.onNext(new DeltaTextChangeEventImpl(0, 0, newFileContentInfo.getFileContent().get(), this, EChangeSide.NEW));
      initialObservableStateSet = true;
    }
  }

  @Override
  public List<IDeltaTextChangeEvent> acceptDelta(IChangeDelta pChangeDelta)
  {
    return _appyDelta(pChangeDelta, EChangeSide.NEW, EChangeSide.OLD);
  }

  @Override
  public List<IDeltaTextChangeEvent> revertDelta(IChangeDelta pChangeDelta)
  {
    return _appyDelta(pChangeDelta, EChangeSide.OLD, EChangeSide.NEW);
  }

  private List<IDeltaTextChangeEvent> _appyDelta(IChangeDelta pChangeDelta, EChangeSide pApplyingSide, EChangeSide pChangeSide)
  {
    List<IDeltaTextChangeEvent> deltaTextChangeEvents = new ArrayList<>();
    if (oldVersion == null || newVersion == null)
    {
      reset();
    }
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      boolean isChangeNewVersion = pApplyingSide == EChangeSide.OLD;
      int textDifference = 0;
      for (ILinePartChangeDelta linePartChangeDelta : pChangeDelta.getLinePartChanges())
      {
        String prefix;
        String infix = "";
        int changedSideLength = pChangeSide == EChangeSide.NEW ? newVersion.length() : oldVersion.length();
        int appliedStartTextIndex = linePartChangeDelta.getStartTextIndex(pApplyingSide);
        int appliedEndTextIndex = linePartChangeDelta.getEndTextIndex(pApplyingSide);
        int changedStartTextIndex = linePartChangeDelta.getStartTextIndex(pChangeSide) + textDifference;
        int changedEndTextIndex = linePartChangeDelta.getEndTextIndex(pChangeSide) + textDifference;
        int startIndex;
        EChangeType deltaChangeType = linePartChangeDelta.getChangeType();
        String changedSideString = pChangeSide == EChangeSide.NEW ? newVersion : oldVersion;
        String appliedSideString = pChangeSide == EChangeSide.NEW ? oldVersion : newVersion;
        boolean isPointChange = (isChangeNewVersion && deltaChangeType == EChangeType.ADD) || (!isChangeNewVersion && deltaChangeType == EChangeType.DELETE);
        boolean isPointChangeAtEOL = Util.safeIsCharAt(appliedSideString, appliedEndTextIndex - 1, '\n')
            && Util.safeIsCharAt(changedSideString, changedEndTextIndex - 1, '\n')
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
        // insert happens between characters, doesnt affect the characters around it). To make highlighting easier, the indices of the ChangeDelta do not cover that
        // behaviour -> special treatment here
        if (isPointChange)
          infix = "";
        else
          infix += appliedSideString.substring(appliedStartTextIndex, appliedEndTextIndex);
        int postFixStartIndex;
        int textEventRemovalLength;
        if ((!isChangeNewVersion && deltaChangeType == EChangeType.ADD) || (isChangeNewVersion && deltaChangeType == EChangeType.DELETE))
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
        deltaTextChangeEvents.add(new DeltaTextChangeEventImpl(startIndex, textEventRemovalLength, infix, this, pChangeSide));
        // calculate index differences for the following deltas
        textDifference += infix.length() - textEventRemovalLength;
      }

      int lineDifference = (pChangeDelta.getEndLine(pApplyingSide) - pChangeDelta.getStartLine(pApplyingSide))
          - (pChangeDelta.getEndLine(pChangeSide) - pChangeDelta.getStartLine(pChangeSide));
      // exchange delta with updated delta, then propagate additional characters/lines to all deltas that occur later on in the file
      changeDeltas.set(deltaIndex, changeDeltas.get(deltaIndex).acceptChange(pChangeSide));
      _applyOffsetToFollowingDeltas(deltaIndex, textDifference, lineDifference, pChangeSide);
    }
    else
    {
      deltaTextChangeEvents.add(new DeltaTextChangeEventImpl(0, 0, "", this, pChangeSide));
    }
    _checkInitialObservableState();
    deltaTextChangeEvents.forEach(pDeltaTextChangeEvent -> diffTextChangeObservable.onNext(pDeltaTextChangeEvent));
    return deltaTextChangeEvents;
  }

  @Override
  public void discardDelta(IChangeDelta pChangeDelta)
  {
    int deltaIndex = changeDeltas.indexOf(pChangeDelta);
    if (deltaIndex != -1)
    {
      IChangeDelta changeDelta = changeDeltas.get(deltaIndex);
      changeDeltas.set(deltaIndex, changeDelta.discardChange());
    }
    _checkInitialObservableState();
    // empty change on both, to notify both sides to adjust the highlights
    diffTextChangeObservable.onNext(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.OLD));
    diffTextChangeObservable.onNext(new DeltaTextChangeEventImpl(0, 0, "", this, EChangeSide.NEW));
  }

  @Override
  public void processTextEvent(int pOffset, int pLength, @Nullable String pText, EChangeSide pChangeSide)
  {
    if (newVersion == null || oldVersion == null)
      reset();
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
      _processInsertEvent(pOffset, pText, pChangeSide, affectedDelta);
      if (pChangeSide == EChangeSide.NEW)
        newVersion = prefix + pText + postfix;
      else
        oldVersion = prefix + pText + postfix;
    }
    _checkInitialObservableState();
    // signal an empty change for UI updates, this method should be called in response to an update in a document or similar, not the other way round
    diffTextChangeObservable.onNext(new DeltaTextChangeEventImpl(0, 0, "", this, pChangeSide));
  }

  /**
   * @param pOffset        index of the insertion event
   * @param pText          text that was inserted
   * @param pModifiedDelta indicates that the insert is part of a modify operation, and that a delta has been affected by the remove part of the removal part.
   *                       If the delta is e.g. a one-line change and the line is replaced by the modify operation, the delta should still span that line.
   *                       The argument is -1 if not a modify operation or no delta was affected
   */
  private void _processInsertEvent(int pOffset, @NotNull String pText, EChangeSide pChangeSide, int pModifiedDelta)
  {
    int lineOffset;
    lineOffset = pText.split("\n", -1).length - 1;

    for (int index = 0; index < getChangeDeltas().size(); index++)
    {
      IChangeDelta currentDelta = changeDeltas.get(index);
      if (pOffset < currentDelta.getEndTextIndex(pChangeSide) || (pModifiedDelta == index) && pOffset == currentDelta.getEndTextIndex(pChangeSide))
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
  public String getText(EChangeSide pChangeSide)
  {
    if (oldVersion == null || newVersion == null)
      reset();
    return pChangeSide == EChangeSide.NEW ? newVersion : oldVersion;
  }

  @Override
  public void markConflicting(IFileDiff pOtherFileDiff)
  {
    if (oldVersion == null || newVersion == null)
      reset();
    for (int index = 0; index < changeDeltas.size(); index++)
    {
      IChangeDelta changeDelta = changeDeltas.get(index);
      if (pOtherFileDiff.getChangeDeltas().stream().anyMatch(pChangeDelta -> pChangeDelta.isConflictingWith(changeDelta)))
      {
        changeDeltas.set(index, changeDelta.setChangeStatus(new ChangeStatusImpl(changeDelta.getChangeStatus().getChangeStatus(), EChangeType.CONFLICTING)));
      }
    }
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
      return new ChangeDeltaImpl(pEdit, new ChangeStatusImpl(EChangeStatus.PENDING, EnumMappings.toChangeType(pEdit.getType())),
                                 pDeltaTextOffsets, FileDiffImpl.this::getText);
    }
  }

}
