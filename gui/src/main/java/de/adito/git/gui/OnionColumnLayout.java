package de.adito.git.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager that has a central component around which a variable amount of components can be added on both sides.
 *
 * Allocation of space is done in the following way:
 * - the right components get their preferred space allocated, in the order they were added
 * - the left components get their preferred space allocated, in the order they were added
 * - the center component gets the remaining space
 *
 * @author m.kaspera, 08.01.2019
 */
public class OnionColumnLayout implements LayoutManager2
{

  public static final String CENTER = "CENTER";
  public static final String LEFT = "LEFT";
  public static final String RIGHT = "RIGHT";

  private Component centerComponent;
  private List<Component> leftComponents = new ArrayList<>();
  private List<Component> rightComponents = new ArrayList<>();

  @Override
  public void addLayoutComponent(Component pComp, Object pConstraints)
  {
    if (pConstraints == null)
    {
      addLayoutComponent(CENTER, pComp);
    }
    else if (pConstraints instanceof String)
    {
      addLayoutComponent((String) pConstraints, pComp);
    }
  }

  @Override
  public Dimension maximumLayoutSize(Container pTarget)
  {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  @Override
  public float getLayoutAlignmentX(Container pTarget)
  {
    return 0.5f;
  }

  @Override
  public float getLayoutAlignmentY(Container pTarget)
  {
    return 0.5f;
  }

  @Override
  public void invalidateLayout(Container pTarget)
  {
    // empty in BorderLayout as well
  }

  @Override
  public void addLayoutComponent(String pName, Component pComp)
  {
    if (pName == null || CENTER.equals(pName))
      centerComponent = pComp;
    else if (LEFT.equals(pName))
    {
      leftComponents.add(0, pComp);
    }
    else if (RIGHT.equals(pName))
    {
      // right components have to be inserted at the front of the list because else the last-added one is next to CENTER instead of the outside
      rightComponents.add(0, pComp);
    }
  }

  @Override
  public void removeLayoutComponent(Component pComp)
  {
    synchronized (pComp.getTreeLock())
    {
      if (pComp == centerComponent)
        centerComponent = null;
      else
      {
        leftComponents.remove(pComp);
        rightComponents.remove(pComp);
      }
    }
  }

  @Override
  public Dimension preferredLayoutSize(Container pParent)
  {
    synchronized (pParent.getTreeLock())
    {
      Dimension dim = new Dimension(0, 0);
      if (centerComponent != null)
      {
        Dimension d = centerComponent.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      for (Component comp : leftComponents)
      {
        Dimension d = comp.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      for (Component comp : rightComponents)
      {
        Dimension d = comp.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      Insets insets = pParent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

  @Override
  public Dimension minimumLayoutSize(Container pParent)
  {
    synchronized (pParent.getTreeLock())
    {
      Dimension dim = new Dimension(0, 0);
      if (centerComponent != null)
      {
        Dimension d = centerComponent.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      for (Component comp : leftComponents)
      {
        Dimension d = comp.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      for (Component comp : rightComponents)
      {
        Dimension d = comp.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      Insets insets = pParent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

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
      for (Component rightComp : rightComponents)
      {
        rightComp.setSize(rightComp.getWidth(), bottom - top);
        Dimension d = rightComp.getPreferredSize();
        rightComp.setBounds(right - d.width, top, d.width, bottom - top);
        right -= d.width;
      }
      for (Component leftComp : leftComponents)
      {
        leftComp.setSize(leftComp.getWidth(), bottom - top);
        Dimension d = leftComp.getPreferredSize();
        leftComp.setBounds(left, top, d.width, bottom - top);
        left += d.width;
      }
      if (centerComponent != null)
        centerComponent.setBounds(left, top, right - left, bottom - top);
    }
  }

}
