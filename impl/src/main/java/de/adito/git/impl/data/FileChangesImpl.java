package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Object that stores the information about which changes happened to a file
 * in IFileChangeChunks
 *
 * @author m.kaspera 24.09.2018
 */
public class FileChangesImpl implements IFileChanges
{

  private final List<IFileChangeChunk> changeChunks;
  private final Subject<IFileChangesEvent> changeEventObservable;
  private final String[] originalLines;
  private final String[] newLines;

  FileChangesImpl(EditList pEditList, String pOriginalFileContents, String pNewFileContents)
  {
    changeChunks = new ArrayList<>();
    // combine the lineChanges with the information from editList and build chunks
    originalLines = pOriginalFileContents.split("\n", -1);
    newLines = pNewFileContents.split("\n", -1);
    if (!pEditList.isEmpty())
    {
      // from beginning of the file to the first chunk
      if ((pEditList.get(0).getBeginA() != 0 || pEditList.get(0).getBeginB() != 0))
      {
        changeChunks.add(_getUnchangedChunk(null, pEditList.get(0)));
      }
      // first chunk extra, so that the index for the first unchanged chunk in the loop can start at 0
      changeChunks.add(_getChangedChunk(pEditList.get(0)));
      for (int index = 1; index < pEditList.size(); index++)
      {
        changeChunks.add(_getUnchangedChunk(pEditList.get(index - 1), pEditList.get(index)));
        changeChunks.add(_getChangedChunk(pEditList.get(index)));
      }
      // from last chunk to end of file
      if ((pEditList.get(pEditList.size() - 1).getEndB() != newLines.length || pEditList.get(pEditList.size() - 1).getEndA() != originalLines.length))
      {
        changeChunks.add(_getUnchangedChunk(pEditList.get(pEditList.size() - 1), null));
      }
    }
    else
    {
      Edit edit = new Edit(0, originalLines.length, 0, newLines.length);
      changeChunks.add(new FileChangeChunkImpl(edit, pNewFileContents, pNewFileContents, EChangeType.SAME));
    }
    changeEventObservable = BehaviorSubject.createDefault(new FileChangesEventImpl(true, changeChunks));
  }

  Subject<IFileChangesEvent> getSubject()
  {
    return changeEventObservable;
  }

  /**
   * Creates an IFileChangeChunk for the Edit, if one of the sides has more
   * lines than the other, newlines are added to the shorter side so that
   * the sides match up in number of lines
   *
   * @param pEdit Edit for which a {@link IFileChangeChunk} should be created
   * @return the IFileChangeChunk for the Edit
   */
  private IFileChangeChunk _getChangedChunk(@NotNull Edit pEdit)
  {
    StringBuilder oldString = new StringBuilder();
    StringBuilder newString = new StringBuilder();
    StringBuilder oldParityString = new StringBuilder();
    StringBuilder newParityString = new StringBuilder();
    for (int count = 0; count < pEdit.getEndA() - pEdit.getBeginA(); count++)
    {
      oldString.append(originalLines[pEdit.getBeginA() + count]).append("\n");
    }
    for (int count = 0; count < pEdit.getEndB() - pEdit.getBeginB(); count++)
    {
      newString.append(newLines[pEdit.getBeginB() + count]).append("\n");
    }
    for (int count = (pEdit.getEndA() - pEdit.getBeginA()) - (pEdit.getEndB() - pEdit.getBeginB()); count < 0; count++)
    {
      oldParityString.append("\n");
    }
    for (int count = (pEdit.getEndB() - pEdit.getBeginB() - (pEdit.getEndA() - pEdit.getBeginA())); count < 0; count++)
    {
      newParityString.append("\n");
    }
    return new FileChangeChunkImpl(pEdit, oldString.toString(), newString.toString(), oldParityString.toString(), newParityString.toString());
  }

  /**
   * @param pPreviousEdit {@link Edit} that describes the changes just before the unchanged part. Can be null (if unchanged part is the start of the file)
   * @param pNextEdit     {@code Edit} that describes the changes just after the unchanged part. Can be null(if unchanged part is the end of the file)
   * @return {@link IFileChangeChunk} with the lines of the unchanged part between the edits and EChangeType.SAME
   */
  @Nullable
  private IFileChangeChunk _getUnchangedChunk(@Nullable Edit pPreviousEdit, @Nullable Edit pNextEdit)
  {
    IFileChangeChunk unchangedChunk = null;
    if (pPreviousEdit == null && pNextEdit != null)
    {
      unchangedChunk = _firstUnchangedChunk(pNextEdit);
    }
    else if (pPreviousEdit != null && pNextEdit != null)
    {
      StringBuilder oldString = new StringBuilder();
      StringBuilder newString = new StringBuilder();
      int aStart = pPreviousEdit.getEndA();
      int bStart = pPreviousEdit.getEndB();
      int aEnd = pNextEdit.getBeginA();
      int bEnd = pNextEdit.getBeginB();
      for (int index = pPreviousEdit.getEndA(); index < pNextEdit.getBeginA(); index++)
      {
        oldString.append(originalLines[index]).append("\n");
      }
      for (int index = pPreviousEdit.getEndB(); index < pNextEdit.getBeginB(); index++)
      {
        newString.append(newLines[index]).append("\n");
      }
      Edit currentEdit = new Edit(aStart, aEnd, bStart, bEnd);
      unchangedChunk = new FileChangeChunkImpl(currentEdit, oldString.toString(), newString.toString(), EChangeType.SAME);
    }
    // current has to be null here, so not in the parentheses
    else if (pPreviousEdit != null)
    {
      unchangedChunk = _finalUnchangedChunk(pPreviousEdit);
    }
    return unchangedChunk;
  }

  /**
   * @param pNextEdit Edit that comes after the unchanged chunk
   * @return FileChangeChunkImpl that symbolizes the unchanged part between two edits
   */
  private FileChangeChunkImpl _firstUnchangedChunk(@NotNull Edit pNextEdit)
  {
    StringBuilder oldString = new StringBuilder();
    StringBuilder newString = new StringBuilder();
    int aEnd = pNextEdit.getBeginA();
    int bEnd = pNextEdit.getBeginB();
    for (int index = 0; index < pNextEdit.getBeginA(); index++)
    {
      oldString.append(originalLines[index]).append("\n");
    }
    for (int index = 0; index < pNextEdit.getBeginB(); index++)
    {
      newString.append(newLines[index]).append("\n");
    }
    // aStart and bStart set to 0 because we're at the very start of the file
    Edit currentEdit = new Edit(0, aEnd, 0, bEnd);
    return new FileChangeChunkImpl(currentEdit, oldString.toString(), newString.toString(), EChangeType.SAME);
  }

  /**
   * @param pPreviousEdit Edit that comes before the unchanged chunk
   * @return FileChangeChunkImpl that symbolizes the unchanged part between two edits
   */
  private FileChangeChunkImpl _finalUnchangedChunk(@NotNull Edit pPreviousEdit)
  {
    StringBuilder oldString = new StringBuilder();
    StringBuilder newString = new StringBuilder();
    int aStart = pPreviousEdit.getEndA();
    int bStart = pPreviousEdit.getEndB();
    int aEnd = originalLines.length;
    if (aEnd == 1 && "".equals(originalLines[0]))
    {
      aEnd = 0;
    }
    int bEnd = newLines.length;
    for (int index = pPreviousEdit.getEndA(); index < aEnd; index++)
    {
      oldString.append(originalLines[index]).append("\n");
    }
    for (int index = pPreviousEdit.getEndB(); index < bEnd; index++)
    {
      newString.append(newLines[index]).append("\n");
    }
    Edit currentEdit = new Edit(aStart, aEnd, bStart, bEnd);
    return new FileChangeChunkImpl(currentEdit, oldString.toString(), newString.toString(), EChangeType.SAME);
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public Observable<IFileChangesEvent> getChangeChunks()
  {
    return changeEventObservable;
  }

  /**
   * {@inheritDoc}
   */
  public void applyChanges(@NotNull IFileChangeChunk pToChangeChunk)
  {
    List<IFileChangeChunk> currentFileChangeChunks = changeEventObservable.blockingFirst().getNewValue();
    int indexInList = currentFileChangeChunks.indexOf(pToChangeChunk);
    // create new IFileChangeChunks since IFileChangeChunks are effectively final
    Edit edit = new Edit(pToChangeChunk.getAStart(), pToChangeChunk.getAStart() + (pToChangeChunk.getBEnd() - pToChangeChunk.getBStart()),
                         pToChangeChunk.getBStart(), pToChangeChunk.getBEnd());
    IFileChangeChunk changedChunk = new FileChangeChunkImpl(edit, pToChangeChunk.getBLines(), pToChangeChunk.getBLines(), "", "", EChangeType.SAME);
    // adjust line numbers in the following lines/changeChunks
    _propagateAdditionalLines(currentFileChangeChunks, indexInList + 1,
                              (pToChangeChunk.getBEnd() - pToChangeChunk.getBStart()) - (pToChangeChunk.getAEnd() - pToChangeChunk.getAStart()),
                              EChangeSide.OLD);
    // save the changes to the list and fire a change on the list
    currentFileChangeChunks.set(indexInList, changedChunk);
    changeEventObservable.onNext(new FileChangesEventImpl(true, currentFileChangeChunks));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetChanges(@NotNull IFileChangeChunk pToChangeChunk)
  {
    List<IFileChangeChunk> currentFileChangeChunks = changeEventObservable.blockingFirst().getNewValue();
    int indexInList = currentFileChangeChunks.indexOf(pToChangeChunk);
    Edit edit = new Edit(pToChangeChunk.getAStart(), pToChangeChunk.getAEnd(), pToChangeChunk.getBStart(),
                         pToChangeChunk.getBStart() + (pToChangeChunk.getAEnd() - pToChangeChunk.getAStart()));
    IFileChangeChunk changedChunk = new FileChangeChunkImpl(edit, pToChangeChunk.getALines(), pToChangeChunk.getALines(), "", "", EChangeType.SAME);
    _propagateAdditionalLines(currentFileChangeChunks, indexInList,
                              (pToChangeChunk.getAEnd() - pToChangeChunk.getAStart()) - (pToChangeChunk.getBEnd() - pToChangeChunk.getBStart()),
                              EChangeSide.NEW);
    currentFileChangeChunks.set(indexInList, changedChunk);
    changeEventObservable.onNext(new FileChangesEventImpl(true, currentFileChangeChunks));
  }

  /**
   * Adjust the start/end indices of the IFileChangeChunks in the list, starting from the given index
   *
   * @param pFileChangeChunks the list of IFileChangeChunks
   * @param pListIndex        the first index that gets the offset
   * @param pNumLines         the number of lines the IFileChangeChunks have been set back
   */
  private void _propagateAdditionalLines(@NotNull List<IFileChangeChunk> pFileChangeChunks, int pListIndex, int pNumLines, EChangeSide pSide)
  {
    for (int index = pListIndex; index < pFileChangeChunks.size(); index++)
    {
      Edit edit;
      if (pSide == EChangeSide.OLD)
      {
        edit = new Edit(pFileChangeChunks.get(index).getAStart() + pNumLines, pFileChangeChunks.get(index).getAEnd() + pNumLines,
                        pFileChangeChunks.get(index).getBStart(), pFileChangeChunks.get(index).getBEnd());
      }
      else
      {
        edit = new Edit(pFileChangeChunks.get(index).getAStart(), pFileChangeChunks.get(index).getAEnd(),
                        pFileChangeChunks.get(index).getBStart() + pNumLines, pFileChangeChunks.get(index).getBEnd() + pNumLines);
      }
      // FileChangeChunks don't have setters, so create a new one
      IFileChangeChunk updated = new FileChangeChunkImpl(
          edit,
          pFileChangeChunks.get(index).getALines(),
          pFileChangeChunks.get(index).getBLines(),
          pFileChangeChunks.get(index).getAParityLines(),
          pFileChangeChunks.get(index).getBParityLines(),
          pFileChangeChunks.get(index).getChangeType());
      // replace the current FileChangeChunk with the updated one
      pFileChangeChunks.set(index, updated);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean replace(IFileChangeChunk pCurrent, IFileChangeChunk pReplaceWith, boolean pTriggerUpdate)
  {
    List<IFileChangeChunk> tmpCopy;
    synchronized (changeChunks)
    {
      tmpCopy = changeEventObservable.blockingFirst().getNewValue();
    }
    int currentIndex = tmpCopy.indexOf(pCurrent);
    if (currentIndex == -1)
    {
      return false;
    }
    tmpCopy.set(currentIndex, pReplaceWith);
    changeEventObservable.onNext(new FileChangesEventImpl(pTriggerUpdate, tmpCopy));
    return true;
  }

}
