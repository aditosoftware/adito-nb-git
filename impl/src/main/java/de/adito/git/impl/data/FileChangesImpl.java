package de.adito.git.impl.data;

import de.adito.git.api.data.*;
import io.reactivex.Observable;
import io.reactivex.subjects.*;
import org.eclipse.jgit.diff.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Object that stores the information about which changes happened to a file
 * in IFileChangeChunks
 *
 * @author m.kaspera 24.09.2018
 */
public class FileChangesImpl implements IFileChanges
{

  private final Subject<List<IFileChangeChunk>> changeChunks;
  private final String[] originalLines;
  private final String[] newLines;

  FileChangesImpl(EditList pEditList, String pOriginalFileContents, String pNewFileContents)
  {
    List<IFileChangeChunk> changeChunkList = new ArrayList<>();
    // combine the lineChanges with the information from editList and build chunks
    originalLines = pOriginalFileContents.split("\n", -1);
    newLines = pNewFileContents.split("\n", -1);
    if (!pEditList.isEmpty())
    {
      // from beginning of the file to the first chunk
      changeChunkList.add(_getUnchangedChunk(null, pEditList.get(0)));
      // first chunk extra, since the index in the for loop starts at 0
      changeChunkList.add(_getChangedChunk(pEditList.get(0)));
      for (int index = 1; index < pEditList.size(); index++)
      {
        changeChunkList.add(_getUnchangedChunk(pEditList.get(index - 1), pEditList.get(index)));
        changeChunkList.add(_getChangedChunk(pEditList.get(index)));
      }
      // from last chunk to end of file
      changeChunkList.add(_getUnchangedChunk(pEditList.get(pEditList.size() - 1), null));
    }
    else
    {
      Edit edit = new Edit(0, originalLines.length, 0, newLines.length);
      changeChunkList.add(new FileChangeChunkImpl(edit, pNewFileContents, pNewFileContents, EChangeType.SAME));
    }
    changeChunks = BehaviorSubject.createDefault(changeChunkList);
  }

  Subject<List<IFileChangeChunk>> getSubject()
  {
    return changeChunks;
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
  private IFileChangeChunk _getUnchangedChunk(@Nullable Edit pPreviousEdit, @Nullable Edit pNextEdit)
  {
    StringBuilder oldString = new StringBuilder();
    StringBuilder newString = new StringBuilder();
    int aStart, aEnd, bStart, bEnd;
    aStart = aEnd = bStart = bEnd = 0;
    if (pPreviousEdit == null && pNextEdit != null)
    {
      // aStart and bStart already set to 0
      aEnd = pNextEdit.getBeginA();
      bEnd = pNextEdit.getBeginB();
      for (int index = 0; index < pNextEdit.getBeginA(); index++)
      {
        oldString.append(originalLines[index]).append("\n");
      }
      for (int index = 0; index < pNextEdit.getBeginB(); index++)
      {
        newString.append(newLines[index]).append("\n");
      }
    }
    else if (pPreviousEdit != null && pNextEdit != null)
    {
      aStart = pPreviousEdit.getEndA();
      bStart = pPreviousEdit.getEndB();
      aEnd = pNextEdit.getBeginA();
      bEnd = pNextEdit.getBeginB();
      for (int index = pPreviousEdit.getEndA(); index < pNextEdit.getBeginA(); index++)
      {
        oldString.append(originalLines[index]).append("\n");
      }
      for (int index = pPreviousEdit.getEndB(); index < pNextEdit.getBeginB(); index++)
      {
        newString.append(newLines[index]).append("\n");
      }
      // current has to be null here, so not in the parentheses
    }
    else if (pPreviousEdit != null)
    {
      aStart = pPreviousEdit.getEndA();
      bStart = pPreviousEdit.getEndB();
      aEnd = originalLines.length;
      if (aEnd == 1 && "".equals(originalLines[0]))
      {
        aEnd = 0;
      }
      bEnd = newLines.length;
      for (int index = pPreviousEdit.getEndA(); index < aEnd; index++)
      {
        oldString.append(originalLines[index]).append("\n");
      }
      for (int index = pPreviousEdit.getEndB(); index < bEnd; index++)
      {
        newString.append(newLines[index]).append("\n");
      }
    }
    Edit currentEdit = new Edit(aStart, aEnd, bStart, bEnd);
    return new FileChangeChunkImpl(currentEdit, oldString.toString(), newString.toString(), EChangeType.SAME);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Observable<List<IFileChangeChunk>> getChangeChunks()
  {
    return changeChunks;
  }

  /**
   * {@inheritDoc}
   */
  public void applyChanges(@NotNull IFileChangeChunk pToChangeChunk)
  {
    List<IFileChangeChunk> currentFileChangeChunks = changeChunks.blockingFirst();
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
    changeChunks.onNext(currentFileChangeChunks);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetChanges(@NotNull IFileChangeChunk pToChangeChunk)
  {
    List<IFileChangeChunk> currentFileChangeChunks = changeChunks.blockingFirst();
    int indexInList = currentFileChangeChunks.indexOf(pToChangeChunk);
    Edit edit = new Edit(pToChangeChunk.getAStart(), pToChangeChunk.getAEnd(), pToChangeChunk.getBStart(),
                         pToChangeChunk.getBStart() + (pToChangeChunk.getAEnd() - pToChangeChunk.getAStart()));
    IFileChangeChunk changedChunk = new FileChangeChunkImpl(edit, pToChangeChunk.getALines(), pToChangeChunk.getALines(), "", "", EChangeType.SAME);
    _propagateAdditionalLines(currentFileChangeChunks, indexInList,
                              (pToChangeChunk.getAEnd() - pToChangeChunk.getAStart()) - (pToChangeChunk.getBEnd() - pToChangeChunk.getBStart()),
                              EChangeSide.NEW);
    currentFileChangeChunks.set(indexInList, changedChunk);
    changeChunks.onNext(currentFileChangeChunks);
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
          pFileChangeChunks.get(index).getChangeType());
      // replace the current FileChangeChunk with the updated one
      pFileChangeChunks.set(index, updated);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean replace(IFileChangeChunk pCurrent, IFileChangeChunk pReplaceWith)
  {
    List<IFileChangeChunk> tmpCopy;
    synchronized (changeChunks)
    {
      tmpCopy = changeChunks.blockingFirst();
    }
    int currentIndex = tmpCopy.indexOf(pCurrent);
    if (currentIndex == -1)
    {
      return false;
    }
    tmpCopy.set(currentIndex, pReplaceWith);
    changeChunks.onNext(tmpCopy);
    return true;
  }

}
