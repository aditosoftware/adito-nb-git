package de.adito.git.api;

import de.adito.git.api.data.ICommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the AncestryLines and their drawing coordinates for a commit
 * This allows a display of the branching and merging of the commits over time
 * <p>
 * The next item further down the list can be retrieved by calling the nextItem
 * method on this object and supplying it with the requested coordinates
 * <p>
 * Lo' and despair
 *
 * @author m.kaspera 16.11.2018
 */
public class CommitHistoryTreeListItem {

    private final ICommit commit;
    private final List<AncestryLine> ancestryLines;
    private final ColorRoulette colorRoulette;
    private final List<ColoredLineCoordinates> linesToDraw = new ArrayList<>();
    private KnotCoordinates knotCoordinates = null;
    private final int maxLineWidth;

    public CommitHistoryTreeListItem(@NotNull ICommit commit, @NotNull List<AncestryLine> ancestryLines, @NotNull ColorRoulette pColorRoulette) {
        this.commit = commit;
        this.ancestryLines = ancestryLines;
        colorRoulette = pColorRoulette;
        _resolveLines();
        maxLineWidth = _calculateMaxLineWidth();
    }

    /**
     * @return the commit for which the AncestryLines were gathered
     */
    public ICommit getCommit() {
        return commit;
    }

    /**
     * @return the List of AncestryLines that are running for this commit
     */
    public List<AncestryLine> getAncestryLines() {
        return ancestryLines;
    }

    /**
     * @return List with ColoredLineCoordinates for the renderer to draw
     */
    public List<ColoredLineCoordinates> getLinesToDraw() {
        return linesToDraw;
    }

    /**
     *
     * @return the KnotCoordinates for the renderer to draw
     */
    public KnotCoordinates getKnotCoordinates() {
        return knotCoordinates;
    }

    /**
     *
     * @return the amount of space/width that the lines for this CHTLI need
     */
    public int getMaxLineWidth() {
        return maxLineWidth;
    }

    /**
     * Provided with the next commit in the List, this method calculates the
     * CommitHistoryTreeListItem following this CommitHistoryTreeListItem
     *
     * @param pNext ICommit that follows the ICommit of this CommitHistoryTreeListItem in the log
     * @return ICommitHistoryTreeListItem that has the information for the next ICommit in the log
     */
    public CommitHistoryTreeListItem nextItem(@NotNull ICommit pNext, @Nullable ICommit pAfterNext) {
        List<AncestryLine> updatedAncestryLines = new ArrayList<>();
        /*
         gather candidates for STILLBORN lines here with the index of the AncestryLines that will spawn them.
         Gathering them is done because in order to identify them, all AncestryLines have to be known (else it is
         uncertain if the line is potentially the only one leading to the next commit and thus not STILLBORN)
          */
        Map<Integer, AncestryLine> checkForStillborns = new HashMap<>();
        // since several AncestryLines leading to the same commit only spawn lines once, remember if we already processed their children
        boolean processedChildren = false;
        // counter and foreach loop instead of counting loop because better readability
        int counter = 0;
        for (AncestryLine oldAncestryLine : ancestryLines) {
            // throw out STILLBORN lines since they end in the very next line after they're born
            if (!(oldAncestryLine.getLineType() == AncestryLine.LineType.STILLBORN)) {
                // Candidates for STILLBORN lines are lines whose parent equals the next commit
                if (oldAncestryLine.getParent().equals(pNext)) {
                    checkForStillborns.put(counter, oldAncestryLine);
                }
                // the only lines that are advanced are those whose parent is the current commit (lines do not fork/split/end spontaneously, only after encountering their parent commit
                if (oldAncestryLine.getParent().equals(commit)) {
                    _processChildren(processedChildren, oldAncestryLine, updatedAncestryLines, pNext, checkForStillborns, counter);
                    processedChildren = true;
                } else {
                    updatedAncestryLines.add(oldAncestryLine);
                }
            } else {
                colorRoulette.returnColor(oldAncestryLine.getColor());
            }
            counter++;
        }
        // All new AncestryLines are known now, so the candidates for being STILLBORN can now be verified
        for (Integer index : checkForStillborns.keySet()) {
            checkForStillborns.get(index).hasStillbornChildren(pAfterNext, updatedAncestryLines, index);
        }
        return new CommitHistoryTreeListItem(pNext, updatedAncestryLines, colorRoulette);
    }

    /**
     *
     * @param pProcessedChildren if the childLines of the AncestryLine with the current commit were already processed
     * @param pOldAncestryLine AncestryLine with the current commit as parent
     * @param pUpdatedAncestryLines the list of AncestryLines for the pNext
     * @param pNext ICommit following the current commit in the list of commits
     * @param pCheckForStillBorns Map of AncestryLines that should be checked for being STILLBORN
     * @param pCounter index of pOldAncestryLine in the list of ancestryLines
     */
    private void _processChildren(boolean pProcessedChildren, AncestryLine pOldAncestryLine, List<AncestryLine> pUpdatedAncestryLines, ICommit pNext, Map<Integer, AncestryLine> pCheckForStillBorns, int pCounter) {
        if (!pProcessedChildren) {
            if (pOldAncestryLine.getChildLines().isEmpty())
                pOldAncestryLine.reInitChildLines(commit);
            for (AncestryLine childLine : pOldAncestryLine.getChildLines()) {
                if (childLine.getLineType() == AncestryLine.LineType.STILLBORN) {
                    pUpdatedAncestryLines.add(new AncestryLine(childLine.getParent(),
                            childLine.getColor(), AncestryLine.LineType.STILLBORN, childLine.getStillBornMeetingIndex(), colorRoulette));
                } else {
                    AncestryLine newAncestryLine = new AncestryLine(childLine.getParent(), childLine.getColor(), colorRoulette);
                    // check again for parent matching the next commit here, since if the parent being the current commit can hide the next commit being the parent of the child line
                    if (newAncestryLine.getParent().equals(pNext))
                        pCheckForStillBorns.put(pCounter, newAncestryLine);
                    pUpdatedAncestryLines.add(newAncestryLine);
                }
            }
        } else {
            for (AncestryLine childLine : pOldAncestryLine.getChildLines()) {
                colorRoulette.returnColor(childLine.getColor());
            }
            colorRoulette.returnColor(pOldAncestryLine.getColor());
        }
    }

    /**
     * Analyze the AncestryLines and pre-calculate the coordinates of the lines that
     * the renderer will have to draw
     */
    private void _resolveLines() {
        List<AncestryLine> drawLater = new ArrayList<>();
        int knotIndex = Integer.MAX_VALUE;
        // numChildren: number of children spawned from the current commit, mostly used by the other children
        // numClosing: number of lines that end in this line/commit (basically number of commits that have this commit as parent - 1 || 0. Used by the lower part of most lines
        // numStillBorn: number of lines that will spawn here and end in the next line
        int index = 0, numOfChildren = 0, numClosing = 0, numStillBorn = 0;
        for (AncestryLine currentAncestryLine : ancestryLines) {
            if (currentAncestryLine.getParent().equals(commit)) {
                // if true, the first reference to the current commit was found -> set knotIndex to currentIndex so all following references can point to the location of this line
                if (knotIndex == Integer.MAX_VALUE) {
                    if (currentAncestryLine.getLineType() == AncestryLine.LineType.STILLBORN) {
                        drawLater.add(currentAncestryLine);
                        numStillBorn++;
                    } else {
                        knotIndex = index - numStillBorn;
                        knotCoordinates = new KnotCoordinates(ColoredLineCoordinates.LEFT_OFFSET + (knotIndex * ColoredLineCoordinates.LINE_SEPARATION) - KnotCoordinates.RADIUS / 2
                                , currentAncestryLine.getColor());
                        numOfChildren = _doChildLines(knotIndex, numOfChildren, currentAncestryLine);
                    }
                } else {
                    if (currentAncestryLine.getLineType() == AncestryLine.LineType.STILLBORN) {
                        linesToDraw.add(_getCoordinatesForIndices(currentAncestryLine.getStillBornMeetingIndex(), knotIndex, true, currentAncestryLine.getColor()));
                        numStillBorn++;
                    } else {
                        numClosing++;
                        // draw line from top of the cell (at the incoming point of the line) to the dot/knot on the line that this particular commit is on
                        linesToDraw.add(_getCoordinatesForIndices(index - numStillBorn, knotIndex, true, currentAncestryLine.getColor()));
                    }
                }
            } else {
                // draw straight line from the incoming top of the cell to the middle
                linesToDraw.add(_getCoordinatesForIndices(index - numStillBorn, index - numStillBorn, true, currentAncestryLine.getColor()));
                // draw line from the middle of the cell to the bottom of the cell, x value is determined by the position that this line will be in in the next line
                // influence of numOfChildren: if 1 or 0 same position, if n > 1 line will continue n - 1 lineIndices to the right
                // influence of numClosing: this is the number of lines that will be gone in the next line, so line will be n lineIndices to the left
                linesToDraw.add(_getCoordinatesForIndices(index - numStillBorn, index + (numOfChildren > 0 ? numOfChildren - 1 : 0) - numClosing - numStillBorn, false, currentAncestryLine.getColor()));
            }
            index++;
        }
        // if a STILLBORN line would have been drawn before any other line had referenced the commit in the current line, the STILLBORN line would not have
        // known where the knotIndex would be. That's why it is drawn in the end
        for (AncestryLine stillbornLine : drawLater) {
            linesToDraw.add(_getCoordinatesForIndices(stillbornLine.getStillBornMeetingIndex(), knotIndex, true, stillbornLine.getColor()));
        }
        // draw the dot/knot signaling the line/position of the comment in the particular line at the very last, so no lines interfere with it
    }

    /**
     * since we are in the line of the comment of this line, go through all children commits. This is the only place where
     * additional lines can branch off (which is the case if there is more than one child).
     *
     * @param knotIndex            Index of the line that has the knot displaying the current commit
     * @param numOfChildren        number of children so far
     * @param pCurrentAncestryLine the first AncestryLine of this object that has the current commit as parent
     * @return number of children of the AncestryLine
     */
    private int _doChildLines(int knotIndex, int numOfChildren, AncestryLine pCurrentAncestryLine) {
        for (AncestryLine childLine : pCurrentAncestryLine.getChildLines()) {
            // set color to color of child line
            if (childLine.getLineType() == AncestryLine.LineType.STILLBORN) {
                linesToDraw.add(_getCoordinatesForIndices(knotIndex, childLine.getStillBornMeetingIndex(), false, childLine.getColor()));
            } else {
                // draw line from the dot to the bottom of the cell, x value on the bottom is determined by the previous lines and the number of children before this one
                linesToDraw.add(_getCoordinatesForIndices(knotIndex, knotIndex + numOfChildren, false, childLine.getColor()));
                numOfChildren++;
            }
        }
        // draw line from top of the cell (at the incoming point of the line) to the dot/knot on the line that this particular commit is on
        linesToDraw.add(_getCoordinatesForIndices(knotIndex, knotIndex, true, pCurrentAncestryLine.getColor()));
        return numOfChildren;
    }

    private ColoredLineCoordinates _getCoordinatesForIndices(double pIndexStart, double pIndexEnd, boolean pUpperPart, Color pColor) {
        return new ColoredLineCoordinates((int) (ColoredLineCoordinates.LEFT_OFFSET + pIndexStart * ColoredLineCoordinates.LINE_SEPARATION),
                (int) (ColoredLineCoordinates.LEFT_OFFSET + pIndexEnd * ColoredLineCoordinates.LINE_SEPARATION), pUpperPart, pColor);
    }

    /**
     * @return the maximum x-value of any of the lines. This is the minimum width that the lines need
     */
    private int _calculateMaxLineWidth() {
        int tmpMax = 0;
        for (ColoredLineCoordinates lineCoordinate : linesToDraw) {
            if (lineCoordinate.getX1() > tmpMax)
                tmpMax = lineCoordinate.getX1();
            if (lineCoordinate.getX2() > tmpMax)
                tmpMax = lineCoordinate.getX2();
        }
        return tmpMax;
    }

    /**
     * Class for storing information about the position/color of the
     * knot that symbolizes the current commit. Only contains the x-value
     * of the upper left corner for the oval, calculate the rest by using
     * the height of the cell that the knot is drawn in and the radius of the circle
     */
    public static class KnotCoordinates {

        public final static int RADIUS = 7;
        private final int xCoordinate;
        private final Color color;

        KnotCoordinates(int pXCoordinate, Color pColor) {
            xCoordinate = pXCoordinate;
            color = pColor;
        }

        /**
         * @return x-Value for the upper left corner of the oval that forms the filled circle
         */
        public int getXCoordinate() {
            return xCoordinate;
        }

        /**
         * @return Color that the circle/knot should be drawn in
         */
        public Color getColor() {
            return color;
        }
    }

    /**
     * Symbolizes a line with color, only has the two x-Coordinates of the line though
     * The y-Coordinates are to be determined by the height of the cell the line is drawn
     * in and the change of the upperPart boolean (if true draw from top of the cell to middle,
     * if false draw from middle to lower part of cell)
     */
    public static class ColoredLineCoordinates {

        private final static int LEFT_OFFSET = 10;
        private final static int LINE_SEPARATION = 20;

        private final int x1;
        private final int x2;
        private final boolean upperPart;
        private final Color color;

        ColoredLineCoordinates(int pX1, int pX2, boolean pUpperPart, Color pColor) {
            x1 = pX1;
            x2 = pX2;
            upperPart = pUpperPart;
            color = pColor;
        }

        /**
         * @return x-Value of the starting point of the line
         */
        public int getX1() {
            return x1;
        }

        /**
         * @return x-Value of the ending point of the line
         */
        public int getX2() {
            return x2;
        }

        /**
         * @return does the line go from the upper part of a cell to the middle?
         */
        public boolean isUpperPart() {
            return upperPart;
        }

        /**
         * @return Color that the line should be drawn in
         */
        public Color getColor() {
            return color;
        }
    }
}
