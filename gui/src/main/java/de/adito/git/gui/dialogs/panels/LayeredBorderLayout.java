package de.adito.git.gui.dialogs.panels;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * javax.awt.BorderLayout customized to support layered components in each of the five regions (NORTH, SOUTH, ...). The preferred/min size of a region is defined by
 * the max height/width for the preferred/min size of the contained components
 *
 * @author m.kaspera, 26.09.2019
 */
public class LayeredBorderLayout implements LayoutManager2
{

  private final List<Component> centerComponents = new ArrayList<>();
  private final List<Component> northComponents = new ArrayList<>();
  private final List<Component> southComponents = new ArrayList<>();
  private final List<Component> eastComponents = new ArrayList<>();
  private final List<Component> westComponents = new ArrayList<>();
  private final int hgap;
  private final int vgap;

  @SuppressWarnings("WeakerAccess")
  public LayeredBorderLayout(int pHgap, int pVgap)
  {
    hgap = pHgap;
    vgap = pVgap;
  }

  @SuppressWarnings("WeakerAccess")
  public LayeredBorderLayout()
  {
    this(0, 0);
  }

  /**
   * <b>Copied from BorderLayout, removed the old component names though (so only EAST/WEST/NORTH/SOUTH/CENTER is supported)</b><br>
   *
   * Adds the specified component to the layout, using the specified
   * constraint object.  For border layouts, the constraint must be
   * one of the following constants:  {@code NORTH},
   * {@code SOUTH}, {@code EAST},
   * {@code WEST}, or {@code CENTER}.
   * <p>
   * Most applications do not call this method directly. This method
   * is called when a component is added to a container using the
   * {@code Container.add} method with the same argument types.
   *
   * @param comp        the component to be added.
   * @param constraints an object that specifies how and where
   *                    the component is added to the layout.
   * @throws IllegalArgumentException if the constraint object is not
   *                                  a string, or if it not one of the five specified constants.
   * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
   * @since 1.1
   */
  @Override
  public void addLayoutComponent(Component comp, Object constraints)
  {
    synchronized (comp.getTreeLock())
    {
      if ((constraints == null) || (constraints instanceof String))
      {
        addLayoutComponent((String) constraints, comp);
      }
      else
      {
        throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
      }
    }
  }

  /**
   * <b>Copied from BorderLayout</b><br>
   *
   * Returns the maximum dimensions for this layout given the components
   * in the specified target container.
   *
   * @param pParent the component which needs to be laid out
   * @see Container
   * @see #minimumLayoutSize
   * @see #preferredLayoutSize
   */
  @Override
  public Dimension maximumLayoutSize(Container pParent)
  {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * <b>Copied from BorderLayout</b><br>
   *
   * Returns the alignment along the x axis.  This specifies how
   * the component would like to be aligned relative to other
   * components.  The value should be a number between 0 and 1
   * where 0 represents alignment along the origin, 1 is aligned
   * the furthest away from the origin, 0.5 is centered, etc.
   */
  @Override
  public float getLayoutAlignmentX(Container target)
  {
    return 0.5f;
  }

  /**
   * <b>Copied from BorderLayout</b><br>
   *
   * Returns the alignment along the y axis.  This specifies how
   * the component would like to be aligned relative to other
   * components.  The value should be a number between 0 and 1
   * where 0 represents alignment along the origin, 1 is aligned
   * the furthest away from the origin, 0.5 is centered, etc.
   */
  @Override
  public float getLayoutAlignmentY(Container target)
  {
    return 0.5f;
  }

  /**
   * <b>Copied from BorderLayout</b><br>
   *
   * Invalidates the layout, indicating that if the layout manager
   * has cached information it should be discarded.
   */
  @Override
  public void invalidateLayout(Container target)
  {
    // do nothings, same as the BorderLayout
  }

  /**
   * <b>Copied from BorderLayout, removed the old component names though (so only EAST/WEST/NORTH/SOUTH/CENTER is supported)</b><br>
   *
   * @deprecated replaced by {@code addLayoutComponent(Component, Object)}.
   */
  @Deprecated(since = "0.0")
  @Override
  public void addLayoutComponent(String name, Component comp)
  {
    if (BorderLayout.CENTER.equals(name))
    {
      centerComponents.add(comp);
    }
    else if (BorderLayout.EAST.equals(name))
    {
      eastComponents.add(comp);
    }
    else if (BorderLayout.WEST.equals(name))
    {
      westComponents.add(comp);
    }
    else if (BorderLayout.NORTH.equals(name))
    {
      northComponents.add(comp);
    }
    else if (BorderLayout.SOUTH.equals(name))
    {
      southComponents.add(comp);
    }
    else
    {
      throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
    }
  }

  /**
   * Removes the specified component from all regions of this layout
   *
   * @param comp component to be removed from this layout
   */
  @Override
  public void removeLayoutComponent(Component comp)
  {
    centerComponents.remove(comp);
    eastComponents.remove(comp);
    westComponents.remove(comp);
    northComponents.remove(comp);
    southComponents.remove(comp);
  }

  /**
   * <b>Basic Code is identical to BorderLayout, only that instead of a singular component determining the size of a region the biggest component of a region
   * determines the size of that region</b><br>
   *
   * Determines the preferred size of the {@code target}
   * container using this layout manager, based on the components
   * in the container.
   * <p>
   * Most applications do not call this method directly. This method
   * is called when a container calls its {@code getPreferredSize}
   * method.
   *
   * @param pParent the container in which to do the layout.
   * @return the preferred dimensions to lay out the subcomponents
   * of the specified container.
   * @see java.awt.Container
   * @see java.awt.BorderLayout#minimumLayoutSize
   * @see java.awt.Container#getPreferredSize()
   */
  @Override
  public Dimension preferredLayoutSize(Container pParent)
  {
    return _getLayoutSize(pParent, Component::getPreferredSize);
  }

  /**
   * <b>Basic Code is identical to BorderLayout, only that instead of a singular component determining the size of a region the biggest component of a region
   * determines the size of that region</b><br>
   *
   * Determines the minimum size of the {@code target} container
   * using this layout manager.
   * <p>
   * This method is called when a container calls its
   * {@code getMinimumSize} method. Most applications do not call
   * this method directly.
   *
   * @param pParent the container in which to do the layout.
   * @return the minimum dimensions needed to lay out the subcomponents
   * of the specified container.
   * @see java.awt.Container
   * @see java.awt.BorderLayout#preferredLayoutSize
   * @see java.awt.Container#getMinimumSize()
   */
  @Override
  public Dimension minimumLayoutSize(Container pParent)
  {
    return _getLayoutSize(pParent, Component::getMinimumSize);
  }


  /**
   * <b>Basic Code is identical to BorderLayout, only that instead of a singular component determining the size of a region the biggest component of a region
   * determines the size of that region</b><br>
   *
   * Lays out the container argument using this border layout.
   * <p>
   * This method actually reshapes the components in the specified
   * container in order to satisfy the constraints of this
   * {@code BorderLayout} object. The {@code NORTH}
   * and {@code SOUTH} components, if any, are placed at
   * the top and bottom of the container, respectively. The
   * {@code WEST} and {@code EAST} components are
   * then placed on the left and right, respectively. Finally,
   * the {@code CENTER} object is placed in any remaining
   * space in the middle.
   * <p>
   * Most applications do not call this method directly. This method
   * is called when a container calls its {@code doLayout} method.
   *
   * @param pParent the container in which to do the layout.
   * @see java.awt.Container
   * @see java.awt.Container#doLayout()
   */
  @Override
  public void layoutContainer(Container pParent)
  {
    synchronized (pParent.getTreeLock())
    {
      Insets insets = pParent.getInsets();
      int top = insets.top;
      int bottom = pParent.getHeight() - insets.bottom;
      int left = insets.left;
      int right = pParent.getWidth() - insets.right;

      int height = _getMaxSize(northComponents, Component::getHeight);
      Dimension d = _getMaxDimension(northComponents, Component::getPreferredSize);
      for (Component component : northComponents)
      {
        component.setSize(right - left, height);
        component.setBounds(left, top, right - left, d.height);
      }
      top += d.height + vgap;

      height = _getMaxSize(southComponents, Component::getHeight);
      d = _getMaxDimension(southComponents, Component::getPreferredSize);
      for (Component component : southComponents)
      {
        component.setSize(right - left, height);
        component.setBounds(left, bottom - d.height, right - left, d.height);
      }
      bottom -= d.height + vgap;

      int width = _getMaxSize(eastComponents, Component::getWidth);
      d = _getMaxDimension(eastComponents, Component::getPreferredSize);
      for (Component component : eastComponents)
      {
        component.setSize(width, bottom - top);
        component.setBounds(right - d.width, top, d.width, bottom - top);
      }
      right -= d.width + hgap;

      width = _getMaxSize(westComponents, Component::getWidth);
      d = _getMaxDimension(westComponents, Component::getPreferredSize);
      for (Component component : westComponents)
      {
        component.setSize(width, bottom - top);
        component.setBounds(left, top, d.width, bottom - top);
      }
      left += d.width + hgap;

      for (Component component : centerComponents)
      {
        component.setBounds(left, top, right - left, bottom - top);
      }
    }

  }

  /**
   * Calculates the layout size for a given kind of dimension (preferred/min/max, specified by pDimensionFunction) in the style of the BorderLayout, just with
   * the size of a region being determined by the max of a list of components instead of a single one
   *
   * @param pParent            the container in which to do the layout
   * @param pDimensionFunction Function that returns the wanted type of dimension (e.g. min/max/preferred)
   * @return the dimensions needed to lay out the subcomponents
   */
  private Dimension _getLayoutSize(Container pParent, Function<Component, Dimension> pDimensionFunction)
  {
    synchronized (pParent.getTreeLock())
    {
      Dimension dim = new Dimension(0, 0);

      Dimension d = _getMaxDimension(eastComponents, pDimensionFunction);
      dim.width += d.width + hgap;
      dim.height = Math.max(d.height, dim.height);
      d = _getMaxDimension(westComponents, pDimensionFunction);
      dim.width += d.width + hgap;
      dim.height = Math.max(d.height, dim.height);
      d = _getMaxDimension(centerComponents, pDimensionFunction);
      dim.width += d.width;
      dim.height = Math.max(d.height, dim.height);
      d = _getMaxDimension(northComponents, pDimensionFunction);
      dim.width = Math.max(d.width, dim.width);
      dim.height += d.height + vgap;
      d = _getMaxDimension(southComponents, pDimensionFunction);
      dim.width = Math.max(d.width, dim.width);
      dim.height += d.height + vgap;

      Insets insets = pParent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

  /**
   * Retrieves the maximum size of all components
   *
   * @param pComponents   List of components
   * @param pSizeFunction Function that returns the kind of size that is wanted (e.g. height or width)
   * @return maximum size of all components
   */
  private int _getMaxSize(List<Component> pComponents, Function<Component, Integer> pSizeFunction)
  {
    int max = 0;
    for (Component component : pComponents)
    {
      Integer size = pSizeFunction.apply(component);
      if (size > max)
        max = size;
    }
    return max;
  }

  /**
   * Get the minimum bounding box for the sizes of the components contained in the list
   *
   * @param pComponents        List of components
   * @param pDimensionFunction Functions that returns the kind of Dimension (e.g. preferred/min/max) that is wanted
   * @return Dimension of max width and max height of all components of the list
   */
  private Dimension _getMaxDimension(List<Component> pComponents, Function<Component, Dimension> pDimensionFunction)
  {
    Dimension maxDimension = new Dimension(0, 0);
    for (Component pComponent : pComponents)
    {
      Dimension dimension = pDimensionFunction.apply(pComponent);
      if (dimension.height > maxDimension.height)
      {
        maxDimension.setSize(maxDimension.width, dimension.height);
      }
      if (dimension.width > maxDimension.width)
        maxDimension.setSize(dimension.width, maxDimension.height);
    }
    return maxDimension;
  }
}
