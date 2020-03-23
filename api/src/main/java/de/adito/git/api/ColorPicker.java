package de.adito.git.api;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Color;
import java.util.Map;

/**
 * Class that stores Colors in a central place, tries to retrieve the Color by UI manager key first. If no Color for the key is found,
 * gets a pre-defined color for the key from a default-value map
 *
 * @author m.kaspera 28.11.2018
 */
public class ColorPicker
{

  public static final Color INFO_TEXT = _getColor("infoText");
  public static final Color VERSIONING_ADDED = _getColor("nb.versioning.added.color");
  public static final Color DIFF_ADDED = _getColor("nb.diff.added.color");
  public static final Color VERSIONING_MODIFIED = _getColor("nb.versioning.modified.color");
  public static final Color DIFF_MODIFIED = _getColor("nb.diff.changed.color");
  public static final Color VERSIONING_DELETED = _getColor("nb.versioning.deleted.color");
  public static final Color DIFF_DELETED = _getColor("nb.diff.deleted.color");
  public static final Color VERSIONING_CONFLICTING = _getColor("nb.versioning.conflicted.color");
  public static final Color DIFF_UNRESOLVED = _getColor("nb.diff.unresolved.color");
  public static final Color LIST_SELECTION_BACKGROUND = _getColor("List.selectionBackground");
  public static final Color DIFF_LINE_NUM = _getColor("diff.line.num");
  public static final Color DIFF_BACKGROUND = _getColor("diff.background");
  public static final Color DIFF_MODIFIED_SECONDARY = _getColor("nb.diff.changedArea.color");

  private static Map<String, Color> defaultColors;

  private ColorPicker()
  {
  }

  @NotNull
  private static Color _getColor(String pKey)
  {
    Color colorForKey = UIManager.getColor(pKey);
    if (defaultColors == null)
    {
      _initDefaultColors();
    }
    if (colorForKey == null)
    {
      colorForKey = defaultColors.get(pKey);
    }
    if (colorForKey == null)
    {
      throw new RuntimeException("Could not find Color in both Look and Feel and default values for key" + pKey);
    }
    return colorForKey;
  }

  private static void _initDefaultColors()
  {
    defaultColors = Map.ofEntries(
        Map.entry("infoText", new Color(187, 187, 187)),
        Map.entry("nb.versioning.added.color", new Color(73, 210, 73)),
        Map.entry("nb.diff.added.color", new Color(43, 85, 43)),
        Map.entry("nb.diff.changedArea.color", new Color(46, 58, 72)),
        Map.entry("nb.versioning.modified.color", new Color(26, 184, 255)),
        Map.entry("nb.diff.changed.color", new Color(40, 85, 112)),
        Map.entry("nb.versioning.deleted.color", new Color(255, 175, 175)),
        Map.entry("nb.diff.deleted.color", new Color(85, 43, 43)),
        Map.entry("nb.versioning.conflicted.color", new Color(255, 100, 100)),
        Map.entry("nb.diff.unresolved.color", new Color(130, 30, 30)),
        Map.entry("List.selectionBackground", new Color(52, 152, 219)),
        Map.entry("diff.line.num", new Color(0xff888888, true)),
        Map.entry("diff.background", new Color(0xff313335, true))
    );
  }

}
