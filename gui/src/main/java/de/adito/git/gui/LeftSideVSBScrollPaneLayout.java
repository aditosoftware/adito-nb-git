package de.adito.git.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * <b>Functions of this class are all mostly copied from javax.swing.ScrollPaneLayout, only changes are that isLeftToRight is always false for this class,
 * which means the Vertical ScrollBar is always placed on the left side, without any further consequences like inverse scrolling or the like</b><br>
 *
 *
 * Overridden ScrollPaneLayout that always places the Vertical Scrollbar(VSB) on the left side instead of the right side. This Layout does not care about
 * the ComponentOrientation of the ScrollPane
 * Allows normal (instead of inverted) horizontal scrolling while still having the VSB on the left side
 *
 * @author m.kaspera, 24.09.2019
 */
public class LeftSideVSBScrollPaneLayout extends ScrollPaneLayout
{

  /**
   * <b>Copied method from javax.swing.ScrollPaneLayout, only replaced the isLeftToRight variable
   * with false and simplified the code</b><br>
   *
   *
   *
   *
   * Lays out the scrollpane. The positioning of components depends on
   * the following constraints:
   * <ul>
   * <li> The row header, if present and visible, gets its preferred
   * width and the viewport's height.
   *
   * <li> The column header, if present and visible, gets its preferred
   * height and the viewport's width.
   *
   * <li> If a vertical scrollbar is needed, i.e. if the viewport's extent
   * height is smaller than its view height or if the <code>displayPolicy</code>
   * is ALWAYS, it's treated like the row header with respect to its
   * dimensions and is made visible.
   *
   * <li> If a horizontal scrollbar is needed, it is treated like the
   * column header (see the paragraph above regarding the vertical scrollbar).
   *
   * <li> If the scrollpane has a non-<code>null</code>
   * <code>viewportBorder</code>, then space is allocated for that.
   *
   * <li> The viewport gets the space available after accounting for
   * the previous constraints.
   *
   * <li> The corner components, if provided, are aligned with the
   * ends of the scrollbars and headers. If there is a vertical
   * scrollbar, the right corners appear; if there is a horizontal
   * scrollbar, the lower corners appear; a row header gets left
   * corners, and a column header gets upper corners.
   * </ul>
   *
   * @param parent the <code>Container</code> to lay out
   */
  public void layoutContainer(Container parent)
  {
    /* Sync the (now obsolete) policy fields with the
     * JScrollPane.
     */
    JScrollPane scrollPane = (JScrollPane) parent;
    vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
    hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();

    Rectangle availR = scrollPane.getBounds();
    availR.x = availR.y = 0;

    Insets insets = parent.getInsets();
    availR.x = insets.left;
    availR.y = insets.top;
    availR.width -= insets.left + insets.right;
    availR.height -= insets.top + insets.bottom;

    /* If there's a visible column header remove the space it
     * needs from the top of availR.  The column header is treated
     * as if it were fixed height, arbitrary width.
     */

    Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);

    if ((colHead != null) && (colHead.isVisible()))
    {
      int colHeadHeight = Math.min(availR.height,
                                   colHead.getPreferredSize().height);
      colHeadR.height = colHeadHeight;
      availR.y += colHeadHeight;
      availR.height -= colHeadHeight;
    }

    /* If there's a visible row header remove the space it needs
     * from the left or right of availR.  The row header is treated
     * as if it were fixed width, arbitrary height.
     */

    Rectangle rowHeadR = new Rectangle(0, 0, 0, 0);

    if ((rowHead != null) && (rowHead.isVisible()))
    {
      int rowHeadWidth = Math.min(availR.width,
                                  rowHead.getPreferredSize().width);
      rowHeadR.width = rowHeadWidth;
      availR.width -= rowHeadWidth;
      rowHeadR.x = availR.x + availR.width;
    }

    /* If there's a JScrollPane.viewportBorder, remove the
     * space it occupies for availR.
     */

    Border viewportBorder = scrollPane.getViewportBorder();
    Insets vpbInsets;
    if (viewportBorder != null)
    {
      vpbInsets = viewportBorder.getBorderInsets(parent);
      availR.x += vpbInsets.left;
      availR.y += vpbInsets.top;
      availR.width -= vpbInsets.left + vpbInsets.right;
      availR.height -= vpbInsets.top + vpbInsets.bottom;
    }
    else
    {
      vpbInsets = new Insets(0, 0, 0, 0);
    }


    /* At this point availR is the space available for the viewport
     * and scrollbars. rowHeadR is correct except for its height and y
     * and colHeadR is correct except for its width and x.  Once we're
     * through computing the dimensions  of these three parts we can
     * go back and set the dimensions of rowHeadR.height, rowHeadR.y,
     * colHeadR.width, colHeadR.x and the bounds for the corners.
     *
     * We'll decide about putting up scrollbars by comparing the
     * viewport views preferred size with the viewports extent
     * size (generally just its size).  Using the preferredSize is
     * reasonable because layout proceeds top down - so we expect
     * the viewport to be laid out next.  And we assume that the
     * viewports layout manager will give the view it's preferred
     * size.  One exception to this is when the view implements
     * Scrollable and Scrollable.getViewTracksViewport{Width,Height}
     * methods return true.  If the view is tracking the viewports
     * width we don't bother with a horizontal scrollbar, similarly
     * if view.getViewTracksViewport(Height) is true we don't bother
     * with a vertical scrollbar.
     */

    Component view = (viewport != null) ? viewport.getView() : null;
    Dimension viewPrefSize =
        (view != null) ? view.getPreferredSize()
            : new Dimension(0, 0);

    Dimension extentSize =
        (viewport != null) ? viewport.toViewCoordinates(availR.getSize())
            : new Dimension(0, 0);

    boolean viewTracksViewportWidth = false;
    boolean viewTracksViewportHeight = false;
    boolean isEmpty = (availR.width < 0 || availR.height < 0);
    Scrollable sv;
    // Don't bother checking the Scrollable methods if there is no room
    // for the viewport, we aren't going to show any scrollbars in this
    // case anyway.
    if (!isEmpty && view instanceof Scrollable)
    {
      sv = (Scrollable) view;
      viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
      viewTracksViewportHeight = sv.getScrollableTracksViewportHeight();
    }
    else
    {
      sv = null;
    }

    /* If there's a vertical scrollbar and we need one, allocate
     * space for it (we'll make it visible later). A vertical
     * scrollbar is considered to be fixed width, arbitrary height.
     */

    Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);

    boolean vsbNeeded;
    if (vsbPolicy == VERTICAL_SCROLLBAR_ALWAYS)
    {
      vsbNeeded = true;
    }
    else if (vsbPolicy == VERTICAL_SCROLLBAR_NEVER)
    {
      vsbNeeded = false;
    }
    else
    {  // vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED
      vsbNeeded = !viewTracksViewportHeight && (viewPrefSize.height > extentSize.height);
    }


    if ((vsb != null) && vsbNeeded)
    {
      adjustForVSB(true, availR, vsbR, vpbInsets);
      extentSize = viewport.toViewCoordinates(availR.getSize());
    }

    /* If there's a horizontal scrollbar and we need one, allocate
     * space for it (we'll make it visible later). A horizontal
     * scrollbar is considered to be fixed height, arbitrary width.
     */

    Rectangle hsbR = new Rectangle(availR.x - vpbInsets.left, 0, 0, 0);
    boolean hsbNeeded;
    if (hsbPolicy == HORIZONTAL_SCROLLBAR_ALWAYS)
    {
      hsbNeeded = true;
    }
    else if (hsbPolicy == HORIZONTAL_SCROLLBAR_NEVER)
    {
      hsbNeeded = false;
    }
    else
    {  // hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED
      hsbNeeded = !viewTracksViewportWidth && (viewPrefSize.width > extentSize.width);
    }

    if ((hsb != null) && hsbNeeded)
    {
      adjustForHSBCopied(true, availR, hsbR, vpbInsets);

      /* If we added the horizontal scrollbar then we've implicitly
       * reduced  the vertical space available to the viewport.
       * As a consequence we may have to add the vertical scrollbar,
       * if that hasn't been done so already.  Of course we
       * don't bother with any of this if the vsbPolicy is NEVER.
       */
      if ((vsb != null) && !vsbNeeded &&
          (vsbPolicy != VERTICAL_SCROLLBAR_NEVER))
      {

        extentSize = viewport.toViewCoordinates(availR.getSize());
        vsbNeeded = viewPrefSize.height > extentSize.height;

        if (vsbNeeded)
        {
          adjustForVSB(true, availR, vsbR, vpbInsets);
        }
      }
    }

    /* Set the size of the viewport first, and then recheck the Scrollable
     * methods. Some components base their return values for the Scrollable
     * methods on the size of the Viewport, so that if we don't
     * ask after resetting the bounds we may have gotten the wrong
     * answer.
     */

    if (viewport != null)
    {
      viewport.setBounds(availR);

      if (sv != null)
      {
        extentSize = viewport.toViewCoordinates(availR.getSize());

        boolean oldHSBNeeded = hsbNeeded;
        boolean oldVSBNeeded = vsbNeeded;
        viewTracksViewportWidth = sv.
            getScrollableTracksViewportWidth();
        viewTracksViewportHeight = sv.
            getScrollableTracksViewportHeight();
        if (vsb != null && vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED)
        {
          boolean newVSBNeeded = !viewTracksViewportHeight &&
              (viewPrefSize.height > extentSize.height);
          if (newVSBNeeded != vsbNeeded)
          {
            vsbNeeded = newVSBNeeded;
            adjustForVSB(vsbNeeded, availR, vsbR, vpbInsets
            );
            extentSize = viewport.toViewCoordinates
                (availR.getSize());
          }
        }
        if (hsb != null && hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED)
        {
          boolean newHSBbNeeded = !viewTracksViewportWidth &&
              (viewPrefSize.width > extentSize.width);
          if (newHSBbNeeded != hsbNeeded)
          {
            hsbNeeded = newHSBbNeeded;
            adjustForHSBCopied(hsbNeeded, availR, hsbR, vpbInsets);
            if ((vsb != null) && !vsbNeeded &&
                (vsbPolicy != VERTICAL_SCROLLBAR_NEVER))
            {

              extentSize = viewport.toViewCoordinates
                  (availR.getSize());
              vsbNeeded = viewPrefSize.height >
                  extentSize.height;

              if (vsbNeeded)
              {
                adjustForVSB(true, availR, vsbR, vpbInsets
                );
              }
            }
          }
        }
        if (oldHSBNeeded != hsbNeeded ||
            oldVSBNeeded != vsbNeeded)
        {
          viewport.setBounds(availR);
          // You could argue that we should recheck the
          // Scrollable methods again until they stop changing,
          // but they might never stop changing, so we stop here
          // and don't do any additional checks.
        }
      }
    }

    /* We now have the final size of the viewport: availR.
     * Now fixup the header and scrollbar widths/heights.
     */
    vsbR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
    hsbR.width = availR.width + vpbInsets.left + vpbInsets.right;
    rowHeadR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
    rowHeadR.y = availR.y - vpbInsets.top;
    colHeadR.width = availR.width + vpbInsets.left + vpbInsets.right;
    colHeadR.x = availR.x - vpbInsets.left;

    /* Set the bounds of the remaining components.  The scrollbars
     * are made invisible if they're not needed.
     */

    if (rowHead != null)
    {
      rowHead.setBounds(rowHeadR);
    }

    if (colHead != null)
    {
      colHead.setBounds(colHeadR);
    }

    if (vsb != null)
    {
      if (vsbNeeded)
      {
        if (colHead != null &&
            UIManager.getBoolean("ScrollPane.fillUpperCorner"))
        {
          if (upperLeft == null)
          {
            // This is used primarily for GTK L&F, which needs to
            // extend the vertical scrollbar to fill the upper
            // corner near the column header.  Note that we skip
            // this step (and use the default behavior) if the
            // user has set a custom corner component.
            vsbR.y = colHeadR.y;
            vsbR.height += colHeadR.height;
          }
        }
        vsb.setVisible(true);
        vsb.setBounds(vsbR);
      }
      else
      {
        vsb.setVisible(false);
      }
    }

    if (hsb != null)
    {
      if (hsbNeeded)
      {
        if (rowHead != null &&
            UIManager.getBoolean("ScrollPane.fillLowerCorner"))
        {
          if (lowerRight == null)
          {
            // This is used primarily for GTK L&F, which needs to
            // extend the horizontal scrollbar to fill the lower
            // corner near the row header.  Note that we skip
            // this step (and use the default behavior) if the
            // user has set a custom corner component.
            hsbR.width += rowHeadR.width;
          }
        }
        hsb.setVisible(true);
        hsb.setBounds(hsbR);
      }
      else
      {
        hsb.setVisible(false);
      }
    }

    if (lowerLeft != null)
    {
      lowerLeft.setBounds(vsbR.x,
                          hsbR.y,
                          vsbR.width,
                          hsbR.height);
    }

    if (lowerRight != null)
    {
      lowerRight.setBounds(rowHeadR.x,
                           hsbR.y,
                           rowHeadR.width,
                           hsbR.height);
    }

    if (upperLeft != null)
    {
      upperLeft.setBounds(vsbR.x,
                          colHeadR.y,
                          vsbR.width,
                          colHeadR.height);
    }

    if (upperRight != null)
    {
      upperRight.setBounds(rowHeadR.x,
                           colHeadR.y,
                           rowHeadR.width,
                           colHeadR.height);
    }
  }

  /**
   * <b>Copied method from javax.swing.ScrollPaneLayout, only removed the isLeftToRight parameter because it is always false here</b><br>
   *
   *
   *
   * Adjusts the <code>Rectangle</code> <code>available</code> based on if
   * the vertical scrollbar is needed (<code>wantsVSB</code>).
   * The location of the vsb is updated in <code>vsbR</code>, and
   * the viewport border insets (<code>vpbInsets</code>) are used to offset
   * the vsb. This is only called when <code>wantsVSB</code> has
   * changed, eg you shouldn't invoke adjustForVSB(true) twice.
   */
  private void adjustForVSB(boolean wantsVSB, Rectangle available,
                            Rectangle vsbR, Insets vpbInsets)
  {
    int oldWidth = vsbR.width;
    if (wantsVSB)
    {
      int vsbWidth = Math.max(0, Math.min(vsb.getPreferredSize().width,
                                          available.width));

      available.width -= vsbWidth;
      vsbR.width = vsbWidth;

      vsbR.x = available.x - vpbInsets.left;
      available.x += vsbWidth;
    }
    else
    {
      available.width += oldWidth;
    }
  }

  /**
   * <b>Copied method from javax.swing.ScrollPaneLayout</b><br>
   *
   *
   *
   * Adjusts the <code>Rectangle</code> <code>available</code> based on if
   * the horizontal scrollbar is needed (<code>wantsHSB</code>).
   * The location of the hsb is updated in <code>hsbR</code>, and
   * the viewport border insets (<code>vpbInsets</code>) are used to offset
   * the hsb.  This is only called when <code>wantsHSB</code> has
   * changed, eg you shouldn't invoked adjustForHSB(true) twice.
   */
  private void adjustForHSBCopied(boolean wantsHSB, Rectangle available,
                                  Rectangle hsbR, Insets vpbInsets)
  {
    int oldHeight = hsbR.height;
    if (wantsHSB)
    {
      int hsbHeight = Math.max(0, Math.min(available.height,
                                           hsb.getPreferredSize().height));

      available.height -= hsbHeight;
      hsbR.y = available.y + available.height + vpbInsets.bottom;
      hsbR.height = hsbHeight;
    }
    else
    {
      available.height += oldHeight;
    }
  }
}
