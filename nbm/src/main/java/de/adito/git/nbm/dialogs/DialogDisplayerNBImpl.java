package de.adito.git.nbm.dialogs;

import com.google.inject.Inject;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.dialogs.*;
import de.adito.git.gui.swing.SwingUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of the IActionProvider that shows the dialogs in the
 * fashion of Netbeans
 *
 * @author m.kaspera 28.09.2018
 */
class DialogDisplayerNBImpl implements IDialogDisplayer
{

  private static final String RECT_STRING_SEPARATOR = ";";
  private final IPrefStore prefStore;

  @Inject
  DialogDisplayerNBImpl(IPrefStore pPrefStore)
  {
    prefStore = pPrefStore;
  }


  /**
   * @param pTitle String with title of the dialogs
   * @return {@code true} if the "okay" button was pressed, {@code false} if the dialogs was cancelled
   */
  @Override
  public <S extends AditoBaseDialog<T>, T> DialogResult<S, T> showDialog(Function<IDescriptor, S> pDialogContentSupplier, String pTitle, Object[] pButtons)
  {
    Object[] descriptorButtons = new Object[pButtons.length];
    System.arraycopy(pButtons, 0, descriptorButtons, 0, pButtons.length);

    JButton defaultButton;
    if (pButtons[0] instanceof JButton)
    {
      defaultButton = (JButton) pButtons[0];
    }
    else
    {
      defaultButton = new JButton(pButtons[0].toString());
    }
    descriptorButtons[0] = defaultButton;

    DialogDescriptor dialogDescriptor = new DialogDescriptor(null, pTitle, true, descriptorButtons,
                                                             descriptorButtons[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
    S content = pDialogContentSupplier.apply(defaultButton::setEnabled);
    String preferencesKey = content.getClass().getName();
    Rectangle storedBounds = Optional.ofNullable(prefStore.get(preferencesKey))
        .map(DialogDisplayerNBImpl::parseRectangle)
        .orElse(null);

    JPanel borderPane = new _NonScrollablePanel(new BorderLayout())
    {
    };
    borderPane.add(content, BorderLayout.CENTER);
    borderPane.setBorder(new EmptyBorder(7, 7, 0, 7));
    dialogDescriptor.setMessage(borderPane);
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
    dialog.setResizable(true);
    dialog.setMinimumSize(new Dimension(250, 50));
    if (storedBounds != null && SwingUtil.isCompletelyVisible(storedBounds))
    {
      dialog.setBounds(storedBounds);
      dialog.setPreferredSize(storedBounds.getSize());
    }
    if (dialog instanceof JDialog)
      ((JDialog) dialog).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    ResizeListener resizeListener = new ResizeListener(prefStore, preferencesKey);
    dialog.addComponentListener(resizeListener);
    dialog.pack();
    dialog.setVisible(true);

    Object pressedButtonObject = dialogDescriptor.getValue();
    Object pressedButton;
    if (pressedButtonObject.equals(defaultButton))
      pressedButton = pButtons[0];
    else if (Arrays.asList(descriptorButtons).contains(pressedButtonObject))
      pressedButton = pressedButtonObject;
    else
      pressedButton = EButtons.ESCAPE;
    dialog.removeComponentListener(resizeListener);

    return new DialogResult<>(content, pressedButton, content.getMessage(), content.getInformation());
  }

  /**
   * Extended JPanel that makes it so that the Panel does not provide scrolling in a ScrollPane, but instead requires wrapping or hiding of information
   * This can be of use if you want to make sure a panel can be resized to a smaller size even if it is in a ScrollPane
   */
  private static class _NonScrollablePanel extends JPanel implements Scrollable
  {
    public _NonScrollablePanel(LayoutManager layout)
    {
      super(layout);
    }

    /*
    Implementation of Scrollable, to disable scrolling in a scrollpane
     */
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
      return 16;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
      return true;
    }

  }

  /**
   * Listen for resize or move events and update the preferences with the new bounds
   */
  private static class ResizeListener implements ComponentListener
  {

    private final IPrefStore prefStore;
    private final String preferencesKey;

    /**
     * @param pPrefStore      PrefStore for storing the current bounds
     * @param pPreferencesKey this is the key for storing the bounds value
     */
    public ResizeListener(@NotNull IPrefStore pPrefStore, @NotNull String pPreferencesKey)
    {
      prefStore = pPrefStore;
      preferencesKey = pPreferencesKey;
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
      prefStore.put(preferencesKey, DialogDisplayerNBImpl.rectangleToString(e.getComponent().getBounds()));
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
      prefStore.put(preferencesKey, DialogDisplayerNBImpl.rectangleToString(e.getComponent().getBounds()));
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
      // no-op - this listener only cares about resize and move events
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
      // no-op - this listener only cares about resize and move events
    }
  }

  /**
   * Creates a string representing the rectangle. This string can be used to store the rectangle in the preferences, the string can be reverted to a rectangle by
   * calling the DialogDisplayerNBImpl.parseRectangle method
   *
   * @param pRectangle rectangle that should be transformed to a string
   * @return string representation of the rectangle
   */
  @NotNull
  static String rectangleToString(@NotNull Rectangle pRectangle)
  {
    return (int) pRectangle.getX() + RECT_STRING_SEPARATOR + (int) pRectangle.getY() + RECT_STRING_SEPARATOR
        + (int) pRectangle.getWidth() + RECT_STRING_SEPARATOR + (int) pRectangle.getHeight();
  }

  /**
   * Parses a string created for representing a rectangle by the DialogDisplayerNBImpl.rectangleToString method for storing the rectangle
   *
   * @param pStringVersion String to be converted back into a rectangle
   * @return Rectangle representation of the string, or null if no rectangle can be parsed from the string
   */
  @Nullable
  static Rectangle parseRectangle(@NotNull String pStringVersion)
  {
    // remove spaces and split the string along the separator
    String[] splits = pStringVersion.replaceAll(" ", "").split(RECT_STRING_SEPARATOR);
    if (splits.length == 4)
    {
      try
      {
        return new Rectangle(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2]), Integer.parseInt(splits[3]));
      }
      catch (NumberFormatException ignored)
      {
        // ignore the exception and just return null
      }
    }
    return null;
  }

}
