package de.adito.git.gui.dialogs.panels.basediffpanel.diffpane;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Objects;

/**
 * Represents one of the colored areas beneath the line numbers
 *
 * @author m.kaspera, 15.01.2019
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LineNumberColor
{
  @NonNull
  private final Color color;
  @NonNull
  private final Rectangle coloredArea;

  /**
   * @return Color that the area should be colored in
   */
  @NonNull
  Color getColor()
  {
    return color;
  }

  /**
   * @return Area that should be colored as Rectangle
   */
  @NonNull
  Rectangle getColoredArea()
  {
    return coloredArea;
  }

  /**
   * Overridden equals and hashcode to be able to use this object in treeMaps
   *
   * @param pO object to check for equality
   * @return true if the given object is another LineNUmberColor with the same attributes as this object, false otherwise
   */
  @Override
  public boolean equals(Object pO)
  {
    if (this == pO) return true;
    if (pO == null || getClass() != pO.getClass()) return false;
    LineNumberColor that = (LineNumberColor) pO;
    return getColor().equals(that.getColor()) && getColoredArea().equals(that.getColoredArea());
  }

  /**
   * Overridden equals and hashcode to be able to use this object in treeMaps
   *
   * @return hashCode unique for an element with the same attributes as this object
   */
  @Override
  public int hashCode()
  {
    return Objects.hash(getColor(), getColoredArea());
  }
}
