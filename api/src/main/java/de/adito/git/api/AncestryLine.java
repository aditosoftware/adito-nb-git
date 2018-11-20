package de.adito.git.api;


import de.adito.git.api.data.ICommit;
import org.jetbrains.annotations.NotNull;

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

    private final ICommit parent;
    private final Color color;
    private final List<AncestryLine> childLines = new ArrayList<>();

    private enum LineType {FULL, INFANT}

    /**
     * @param pParent ICommit that is the next commit in the line symbolized by this class
     * @param pColor  Color of the line
     */
    public AncestryLine(@NotNull ICommit pParent, @NotNull Color pColor) {
        this(pParent, pColor, LineType.FULL);
    }

    /**
     * @param pParent   ICommit that is the next commit in the line symbolized by this class
     * @param pColor    Color of the line
     * @param pLineType LineType, INFANT for unborn lines, FULL for lines that are already active in the row of parent
     */
    private AncestryLine(@NotNull ICommit pParent, @NotNull Color pColor, LineType pLineType) {
        parent = pParent;
        color = pColor;
        if (pLineType == LineType.FULL) {
            _initChildLines();
        }
    }

    public List<AncestryLine> getChildLines() {
        return childLines;
    }

    /**
     * @return the next ICommit in the line
     */
    public ICommit getParent() {
        return parent;
    }

    /**
     * @return the color of the line
     */
    public Color getColor() {
        return color;
    }

    /**
     * calculates the lines that form the parent (in the log: following) lines of the ICommit parent. Since those lines
     * are not active in the row of parent but only in the next, they have the LineType INFANT
     */
    private void _initChildLines() {
        if (!parent.getParents().isEmpty()) {
            childLines.add(new AncestryLine(parent.getParents().get(0), color, LineType.INFANT));
            if (parent.getParents().size() > 1) {
                for (ICommit grandParent : parent.getParents().subList(1, parent.getParents().size())) {
                    Color lineColor = ColorRoulette.get();
                    childLines.add(new AncestryLine(grandParent, lineColor == null ? Color.green : lineColor, LineType.INFANT));
                }
            }
        }
    }

}
