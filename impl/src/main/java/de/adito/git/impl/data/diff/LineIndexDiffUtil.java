package de.adito.git.impl.data.diff;

import org.eclipse.jgit.diff.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author m.kaspera, 27.02.2020
 */
final class LineIndexDiffUtil
{

  private LineIndexDiffUtil()
  {
  }

  /**
   * Creates a list of a kind of changeType from the given edits and information about the text
   *
   * @param pOriginalContent    Original version of the String or text
   * @param pChangedContent     Changed version of the String or text
   * @param pEditList           EditList containing information about the changed lines between pOriginalContent and pChangedContent
   * @param pChangeDeltaFactory IChangeDeltaFactory that creates some kind of changeType from the edits and line and text offsets
   * @param <T>                 Type of the ChangeType created by the IChangeDeltaFactory
   * @return List of IChangeTypes
   */
  static <T> @NotNull List<T> getTextOffsets(@NotNull String pOriginalContent, @NotNull String pChangedContent, @NotNull EditList pEditList, @NotNull IChangeDeltaFactory<T> pChangeDeltaFactory)
  {
    List<T> list = new ArrayList<>();
    List<LineInfo> oldTextLineInfos = LineIndexDiffUtil.getLineInfos(pOriginalContent);
    List<LineInfo> newTextLineInfos = LineIndexDiffUtil.getLineInfos(pChangedContent);

    for (Edit edit : pEditList)
    {
      int startIndexOld = LineIndexDiffUtil.getStartIndexSafely(oldTextLineInfos, edit.getBeginA());
      int endIndexOld = oldTextLineInfos.get(Math.min(oldTextLineInfos.size() - 1, Math.max(edit.getBeginA(), edit.getEndA() - 1))).getEndIndex();
      int startIndexNew = LineIndexDiffUtil.getStartIndexSafely(newTextLineInfos, edit.getBeginB());
      int endIndexNew;
      if (edit.getType() == Edit.Type.DELETE)
        endIndexNew = startIndexNew;
      else
        endIndexNew = newTextLineInfos.get(Math.min(newTextLineInfos.size() - 1, Math.max(edit.getBeginB(), edit.getEndB() - 1))).getEndIndex();
      list.add(pChangeDeltaFactory.createDelta(edit, new ChangeDeltaTextOffsets(startIndexOld, endIndexOld, startIndexNew, endIndexNew)));
    }
    return list;
  }

  /**
   * @param pLineInfos List of LineInfos
   * @param pIndex     index of the line
   * @return start index of the line, or +1 to the endIndex of the last line if index is out of bounds
   */
  static int getStartIndexSafely(@NotNull List<LineInfo> pLineInfos, int pIndex)
  {
    if (pLineInfos.size() > pIndex)
    {
      return pLineInfos.get(pIndex).getStartIndex();
    }
    else
    {
      // E.g. if there was no \n in the last line, we end up here. Take the last lineInfo and then add one character
      return pLineInfos.get(pLineInfos.size() - 1).getEndIndex() + 1;
    }
  }

  /**
   * Creates the LineInfo for a given String/text
   *
   * @param pText String or text for which to generate a list of LineInfos
   * @return List of LineInfos with the start and endIndices of the lines in the String or text
   */
  static @NotNull List<LineInfo> getLineInfos(@NotNull String pText)
  {
    List<LineInfo> lineInfos = new ArrayList<>();
    int startIndex = 0;
    for (String line : pText.split("\n", -1))
    {
      // + 1 because of the missing \n in line (that is actually there)
      lineInfos.add(new LineInfo(startIndex, startIndex + line.length() + 1));
      startIndex += line.length() + 1;
    }
    LineInfo lastLineInfo = lineInfos.get(lineInfos.size() - 1);
    lineInfos.set(lineInfos.size() - 1, new LineInfo(lastLineInfo.getStartIndex(), lastLineInfo.getEndIndex() - 1));
    return lineInfos;
  }

  /**
   * Performs a basic diff and returns an EditList denoting the changed lines
   *
   * @param pVersion1       String in original version
   * @param pVersion2       String in changed version
   * @param pTextComparator Determines the treatment of whitespaces
   * @return EditList denoting the changed lines
   */
  public static EditList getChangedLines(String pVersion1, String pVersion2, RawTextComparator pTextComparator)
  {
    RawText fileContents = new RawText(pVersion1.getBytes());
    RawText currentFileContents = new RawText(pVersion2.getBytes());

    return new HistogramDiff().diff(pTextComparator, fileContents, currentFileContents);
  }
}
