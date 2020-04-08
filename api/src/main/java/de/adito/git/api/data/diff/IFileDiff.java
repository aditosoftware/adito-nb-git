package de.adito.git.api.data.diff;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Interface for the data object containing the information about the changes to a file
 *
 * @author m.kaspera 20.09.2018
 */
public interface IFileDiff extends IFileChangeType
{

  /**
   * return an IFileDiffHeader containing information about the id/path/... of the old and new Version of the File as well as the type of change
   *
   * @return IFileDiffHeader of this IFileDiff
   */
  @NotNull
  IFileDiffHeader getFileHeader();

  /**
   * returns the encoding used to convert the fileContents, as String, to a byte array or vice versa
   *
   * @param pChangeSide {@link EChangeSide} that tells if the older or later branch/commit/... should be inspected
   * @return the encoding used to represent the fileContents as byte array
   */
  Charset getEncoding(@NotNull EChangeSide pChangeSide);

  /**
   * get the lineEndings used for the given side
   *
   * @param pChangeSide which side of the change should be taken
   * @return ELineEnding used
   */
  ELineEnding getUsedLineEndings(EChangeSide pChangeSide);

  /**
   * @return List of IChangeDeltas containing the changes to the file
   */
  List<IChangeDelta> getChangeDeltas();

  /**
   * Resets all ChangeDeltas and the Strings of the two versions back to their original state (before any changes were accepted or discarded)
   */
  void reset();

  /**
   * @return Observable that fires events with information about changes that need to happen to keep a textdocument up to date with the diff data
   */
  Observable<IDeltaTextChangeEvent> getDiffTextChangeObservable();

  /**
   * Accepts the changes introduced by the given Delta (applies changes from NEW side to OLD side)
   *
   * @param pChangeDelta ChangeDelta to accept
   * @return Event describing the changes done to the new side of the diff
   */
  List<IDeltaTextChangeEvent> acceptDelta(IChangeDelta pChangeDelta);

  /**
   * Reverts the changes introduced by the given Delta (applies changes from OLD side to NEW side)
   *
   * @param pChangeDelta ChangeDelta to accept
   * @return Event describing the changes done to the new side of the diff
   */
  List<IDeltaTextChangeEvent> revertDelta(IChangeDelta pChangeDelta);

  /**
   * Discards the changes introduced by the given Delta
   *
   * @param pChangeDelta ChangeDelta to discard
   */
  void discardDelta(IChangeDelta pChangeDelta);

  /**
   * Incorporates the changes done in the DocumentEvent into this diff
   * The text should be filtered in such a way that it contains only \n as newlines (indices still have to match)
   *  @param pOffset offset from the start of the text to where the change begins
   * @param pLength length of the changed block, 0 for insert
   * @param pText   inserted text, null for a removal operation. If this is an empty insert, use ""
   * @param pChangeSide Side of the change that the text was inserted in
   */
  void processTextEvent(int pOffset, int pLength, @Nullable String pText, EChangeSide pChangeSide);

  /**
   * Get the text for one side of this diff
   *
   * @param pChangeSide Which side of the diff
   * @return Text if the passed side, up-to-date if any deltas were accepted
   */
  String getText(EChangeSide pChangeSide);

  /**
   * Mark all changeDeltas as conflicting that clash with any of the changeDeltas from the other IFileDiff
   * Intended direction of the changes here is always NEW -> OLD (so the old version of the text between the IFileDiffs should be the same)
   *
   * @param pOtherFileDiff IFileDiff for which to mark conflicting changes
   */
  void markConflicting(IFileDiff pOtherFileDiff);

  /**
   * checks if the IFileDiff matches the filePath, works with renames
   *
   * @param pFilePath given path to a file
   * @param pFileDiff IFileDiff
   * @return true if the new or the old path of the IFileDiff match the given path, false otherwise
   */
  static boolean isSameFile(@NotNull String pFilePath, @NotNull IFileDiff pFileDiff)
  {
    return pFilePath.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW)) || pFilePath.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD));
  }

  /**
   * checks if the IFileDiffs reference the same file, works with renames
   *
   * @param pFileDiff      first IFileDiff
   * @param pOtherFileDiff second IFileDiff
   * @return true if any combination of the paths of the IFileDiffs are the same, except if the matching path is the VOID_PATH. false in all other cases
   */
  static boolean isSameFile(@NotNull IFileDiff pFileDiff, @NotNull IFileDiff pOtherFileDiff)
  {
    if (!IRepository.VOID_PATH.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW))
        && ((pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.NEW)))
        || pFileDiff.getFileHeader().getFilePath(EChangeSide.NEW).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.OLD))))
    {
      return true;
    }
    else return !IRepository.VOID_PATH.equals(pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD))
        && (pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.OLD))
        || pFileDiff.getFileHeader().getFilePath(EChangeSide.OLD).equals(pOtherFileDiff.getFileHeader().getFilePath(EChangeSide.NEW)));
  }

}
