package de.adito.git.gui;

import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * This Util class is for easy use the Tablelayout
 *
 * @author Thomas Tasior 17.07.2006, 14:07:52
 */
public class TableLayoutUtil
{
  private JComponent container;

  /**
   * Initialise the components
   *
   * @param pContainer The Container for the JComponents to add
   */
  public TableLayoutUtil(JComponent pContainer)
  {
    container = pContainer;
  }

  /**
   * Put the components at the row and column
   *
   * @param pCol       The column where the component has to add.
   * @param pRow       The row where the component has to add.
   * @param pComponent The component to add.
   */
  public final void add(int pCol, int pRow, JComponent pComponent)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(pCol).append(", ").append(pRow);
    container.add(pComponent, sb.toString());
  }

  /**
   * Adds the component at the relevant row and column.
   *
   * @param pCol       The column where the component has to add.
   * @param pRow       The row where the component has to add.
   * @param pAlignment Information for horizontal and vertical alignment.
   * @param pComponent The Component to add.
   */
  public final void add(int pCol, int pRow, String pAlignment, JComponent pComponent)
  {
    container.add(pComponent, String.valueOf(pCol) + ", " + pRow + ", " + pAlignment);
  }

  /**
   * Adds the component at the relevant row and column.
   * The component claim the area till pCol_2 and pRow2.
   *
   * @param pCol       The column where the component has to add.
   * @param pRow       The row where the component has to add.
   * @param pCol2     The row till the component claim the area.
   * @param pRow2     The column till the component claim the area.
   * @param pComponent The Component to add.
   */
  public final void add(int pCol, int pRow, int pCol2, int pRow2, JComponent pComponent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(pCol).append(", ").append(pRow).append(", ");
    sb.append(pCol2).append(", ").append(pRow2);
    container.add(pComponent, sb.toString());
  }


  /**
   * Fügt eine Komponente in der entsprechenden Spalte und Zeile ein.
   * Die Komponente beansprucht die Fläche bis zu pCol_2 und pRow_2.
   * Nimmt weitere Informationen für horizontale und vertikale Ausrichtung auf.
   *
   * @param pCol       The column where the component has to add.
   * @param pRow       The row where the component has to add.
   * @param pCol2     The row till the component claim the area.
   * @param pRow2     The column till the component claim the area.
   * @param pComponent The Component to add.
   * @param pAlignment Alignment for vertical and horizontal claim. (siehe TLUConstants)
   */
  public final void add(int pCol, int pRow, int pCol2, int pRow2, String pAlignment,
                        JComponent pComponent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(pCol).append(", ").append(pRow).append(", ");
    sb.append(pCol2).append(", ").append(pRow2).append(", ").append(pAlignment);
    container.add(pComponent, sb.toString());
  }

  /**
   * Getter method for the TableLayout
   *
   * @param pDefault if the component doesn't own a TableLayout it returns pDefault.
   * @return TableLayout of the component or pDefault, if there is no TableLayout
   */
  @NotNull
  public final TableLayout getLayout(TableLayout pDefault)
  {
    if (!(container.getLayout() instanceof TableLayout))
      return pDefault;

    return (TableLayout) container.getLayout();
  }
}