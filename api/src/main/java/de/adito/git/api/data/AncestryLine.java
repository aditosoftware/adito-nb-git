package de.adito.git.api.data;

import java.awt.*;

/**
 * Class that symbolizes one line in the history view of commits.
 * Stores the line color and the next parent commit in that line
 *
 * @author m.kaspera 16.11.2018
 */
public class AncestryLine {

    private final ICommit parent;
    private final Color color;

    public AncestryLine(ICommit parent, Color color) {
        this.parent = parent;
        this.color = color;
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

}
