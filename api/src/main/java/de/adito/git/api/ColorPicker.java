package de.adito.git.api;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ColorPicker {

    public final static Color INFO_TEXT = getColor("infoText");
    public final static Color VERSIONING_ADDED = getColor("nb.versioning.added.color");
    public final static Color DIFF_ADDED = getColor("nb.diff.added.color");
    public final static Color VERSIONING_MODIFIED = getColor("nb.versioning.modified.color");
    public final static Color DIFF_MODIFIED = getColor("nb.diff.changed.color");
    public final static Color VERSIONING_DELETED = getColor("nb.versioning.deleted.color");
    public final static Color DIFF_DELETED = getColor("nb.diff.deleted.color");
    public final static Color VERSIONING_CONFLICTING = getColor("nb.versioning.conflicted.color");
    public final static Color DIFF_UNRESOLVED = getColor("nb.diff.unresolved.color");
    public final static Color LIST_SELECTION_BACKGROUND = getColor("List.selectionBackground");

    @NotNull
    private static Color getColor(String key) {
        Color colorForKey = UIManager.getColor(key);
        if (defaultColors == null) {
            _initDefaultColors();
        }
        if (colorForKey == null) {
            colorForKey = defaultColors.get(key);
        }
        if (colorForKey == null) {
            throw new RuntimeException("Could not find Color in both Look and Feel and default values for key" + key);
        }
        return colorForKey;
    }

    private static Map<String, Color> defaultColors;

    private static void _initDefaultColors() {
        defaultColors = Map.ofEntries(
                Map.entry("infoText", new Color(187, 187, 187)),
                Map.entry("nb.versioning.added.color", new Color(73, 210, 73)),
                Map.entry("nb.diff.added.color", new Color(43, 85, 43)),
                Map.entry("nb.versioning.modified.color", new Color(26, 184, 255)),
                Map.entry("nb.diff.changed.color", new Color(40, 85, 112)),
                Map.entry("nb.versioning.deleted.color", new Color(255, 175, 175)),
                Map.entry("nb.diff.deleted.color", new Color(85, 43, 43)),
                Map.entry("nb.versioning.conflicted.color", new Color(255, 100, 100)),
                Map.entry("nb.diff.unresolved.color", new Color(130, 30, 30)),
                Map.entry("List.selectionBackground", new Color(52, 152, 219))
        );
    }

}
