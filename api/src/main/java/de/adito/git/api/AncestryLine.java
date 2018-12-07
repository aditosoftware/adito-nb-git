package de.adito.git.api;


import de.adito.git.api.data.ICommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that symbolizes one line in the history view of commits.
 * Stores the line color and the next parent commit in that line
 *
 * @author m.kaspera 16.11.2018
 */
public class AncestryLine {

    private final Color color;
    private final List<AncestryLine> childLines = new ArrayList<>();
    private final LineType lineType;
    private final ColorRoulette colorRoulette;
    private double stillBornMeetingIndex = 0;
    private final boolean isBranchHead;
    private ICommit parent;

    /**
     * FULL: active line
     * INFANT: line that has yet to spawn
     * STILLBORN: line that will spawn and be gone in the very next line (usually a merge, but not all merges are STILLBORN)
     */
    public enum LineType {
        FULL, INFANT, STILLBORN
    }

    /**
     * @param pParent ICommit that is the next commit in the line symbolized by this class
     * @param pColor  Color of the line
     */
    AncestryLine(@NotNull ICommit pParent, @NotNull Color pColor, @NotNull ColorRoulette pColorRoulette) {
        this(pParent, pColor, LineType.FULL, pColorRoulette, false);
    }

    /**
     * @param pParent ICommit that is the next commit in the line symbolized by this class
     * @param pColor  Color of the line
     */
    public AncestryLine(@NotNull ICommit pParent, @NotNull Color pColor, @NotNull ColorRoulette pColorRoulette, boolean pIsBranchHead) {
        this(pParent, pColor, LineType.FULL, pColorRoulette, pIsBranchHead);

    }

    AncestryLine(@NotNull ICommit pParent, @NotNull Color pColor, @NotNull LineType pLineType, double pStillBornMeetingIndex, @NotNull ColorRoulette pColorRoulette) {
        this(pParent, pColor, pLineType, pColorRoulette, false);
        stillBornMeetingIndex = pStillBornMeetingIndex;
    }

    /**
     * @param pParent   ICommit that is the next commit in the line symbolized by this class
     * @param pColor    Color of the line
     * @param pLineType LineType, INFANT for unborn lines, FULL for lines that are already active in the row of parent
     */
    private AncestryLine(@NotNull ICommit pParent, @NotNull Color pColor, @NotNull LineType pLineType, @NotNull ColorRoulette pColorRoulette, boolean pIsBranchHead) {
        colorRoulette = pColorRoulette;
        parent = pParent;
        color = pColor;
        lineType = pLineType;
        isBranchHead = pIsBranchHead;
        if (pLineType == LineType.FULL) {
            _initChildLines();
        }
    }

    /**
     * @return List of all AncestryLines that will spawn from this line/continue it. the returned lines will all have type INFANT or STILLBORN
     */
    List<AncestryLine> getChildLines() {
        return childLines;
    }

    /**
     * @return the next ICommit in the line
     */
    ICommit getParent() {
        return parent;
    }

    /**
     * @return the color of the line
     */
    Color getColor() {
        return color;
    }

    /**
     * @return LineType, FULL for an already active line, INFANT for an yet unborn line and STILLBORN for a line
     * will spawn and be gone in the very next row
     */
    LineType getLineType() {
        return lineType;
    }

    /**
     * @return if the current parent is the very first commit in this line/branch
     */
    boolean isBranchHead() {
        return isBranchHead;
    }

    /**
     * @return index where the two parts of a stillborn line meet. 0 for all other types of AncestryLines
     */
    double getStillBornMeetingIndex() {
        return stillBornMeetingIndex;
    }

    /**
     * checks if any of the children of this AncestryLine are stillborn children (and changes their type if they indeed are)
     * NOTE: "current commit" in the following does not necessarily mean the parent of this line. It is possible that is the
     * case, but it does not have to be
     *
     * @param pAfterNext            ICommit following the parent of the current ICommit
     * @param pCurrentAncestryLines List of all AncestryLines in the current row/for the current commit
     * @param pParentLineNumber     the index of this AncestryLine in the list of all AncestryLines for the current commit
     */
    void hasStillbornChildren(@Nullable ICommit pAfterNext, List<AncestryLine> pCurrentAncestryLines, int pParentLineNumber) {
        // if we only have one child the line cannot be stillborn
        if (!childLines.isEmpty() && childLines.size() > 1) {
            // first line will continue the line, so only lines with index >= 1 can be stillborn
            for (int childIndex = 1; childIndex < childLines.size(); childIndex++) {
                // if parent of childLine is the next commit the line will lead to the index, however that may still mean it is the only line that does (and thus not STILLBORN)
                if (pAfterNext != null && pAfterNext.equals(childLines.get(childIndex).getParent())) {
                    for (int lineIndex = 0; lineIndex < pCurrentAncestryLines.size(); lineIndex++) {
                        // only if another line leads to next commit as well (or several, we're only interested in the first) the line is of type STILLBORN
                        if (pCurrentAncestryLines.get(lineIndex).getParent().equals(pAfterNext)) {
                            stillBornMeetingIndex = (double) (lineIndex + (pParentLineNumber + childIndex - 1)) / 2;
                            childLines.set(childIndex, new AncestryLine(childLines.get(childIndex).getParent(),
                                    childLines.get(childIndex).getColor(), LineType.STILLBORN, stillBornMeetingIndex, colorRoulette));
                        }

                    }
                }
            }
        }
    }

    /**
     * When loading only parts of the log, the last ICommits don't get their parents set,
     * and as such the last AncestryLines may have faulty information about their child lines
     * (aka the AncestryLine saying there are none, while in reality there are)
     * If you pass the freshly loaded commit from the new parts of the logs here, the child lines
     * are re-evaluated (and possibly fixed).
     * Only call this when the child lines are suspicious, such as when there are none (as this would
     * only ever be the case by the very first commit in a repository)
     *
     * @param pParent ICommit that is the next commit in the line symbolized by this class
     */
    void reInitChildLines(ICommit pParent) {
        childLines.clear();
        parent = pParent;
        _initChildLines();
    }

    /**
     * calculates the lines that form the parent (in the log: following) lines of the ICommit parent. Since those lines
     * are not active in the row of parent but only in the next, they have the LineType INFANT
     */
    private void _initChildLines() {
        if (!parent.getParents().isEmpty()) {
            childLines.add(new AncestryLine(parent.getParents().get(0), color, LineType.INFANT, colorRoulette, false));
            if (parent.getParents().size() > 1) {
                for (int parentIndex = 1; parentIndex < parent.getParents().size(); parentIndex++) {
                    childLines.add(new AncestryLine(parent.getParents().get(parentIndex), colorRoulette.get(), LineType.INFANT, colorRoulette, false));
                }
            }
        }
    }

}