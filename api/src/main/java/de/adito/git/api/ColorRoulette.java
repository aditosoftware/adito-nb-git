package de.adito.git.api;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Queue-backed list of colors that delivers colors/takes them back. Operates on FIFO principle
 *
 * @author m.kaspera 20.11.2018
 */
public class ColorRoulette {

    ColorRoulette() {
        availableColors.addAll(initialColors);
    }

    private final List<Color> initialColors = Arrays.asList(
            new Color(0, 255, 0),
            new Color(0, 255, 255),
            new Color(255, 255, 0),
            new Color(200, 200, 200),
            new Color(255, 0, 0),
            new Color(0, 0, 255),
            new Color(200, 100, 0),
            new Color(150, 200, 200),
            new Color(200, 200, 0),
            new Color(0, 200, 200),
            new Color(200, 100, 100),
            new Color(0, 100, 200),
            new Color(100, 255, 255)
    );
    private final LinkedList<Color> availableColors = new LinkedList<>();

    /**
     * @return the next Color in the queue or null if an error occurs/the queue is empty
     */
    @NotNull
    public Color get() {
        try {
            return availableColors.remove();
        } catch (NoSuchElementException e) {
            return Color.red;
        }
    }

    /**
     * @param color the Color that should be returned to the queue
     */
    void returnColor(@NotNull Color color) {
        if (!availableColors.contains(color)) {
            availableColors.add(color);
        }
    }

    /**
     * resets the contents of the Queue back to its original state
     */
    public void reset() {
        availableColors.clear();
        availableColors.addAll(initialColors);
    }

}
