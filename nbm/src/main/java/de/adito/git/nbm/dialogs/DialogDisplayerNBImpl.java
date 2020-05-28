package de.adito.git.nbm.dialogs;

import com.google.inject.Inject;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import de.adito.git.gui.dialogs.DialogResult;
import de.adito.git.gui.dialogs.IDialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Function;

/**
 * Implementation of the IActionProvider that shows the dialogs in the
 * fashion of Netbeans
 *
 * @author m.kaspera 28.09.2018
 */
class DialogDisplayerNBImpl implements IDialogDisplayer
{

  @Inject
  DialogDisplayerNBImpl()
  {
  }


  /**
   * @param pTitle String with title of the dialogs
   * @return {@code true} if the "okay" button was pressed, {@code false} if the dialogs was cancelled
   */
  @Override
  public <S extends AditoBaseDialog<T>, T> DialogResult<S, T> showDialog(Function<IDescriptor, S> pDialogContentSupplier, String pTitle, EButtons[] pButtons)
  {
    Object[] descriptorButtons = new Object[pButtons.length];
    System.arraycopy(pButtons, 0, descriptorButtons, 0, pButtons.length);

    JButton defaultButton = new JButton(pButtons[0].toString());
    descriptorButtons[0] = defaultButton;

    DialogDescriptor dialogDescriptor = new DialogDescriptor(null, pTitle, true, descriptorButtons,
                                                             descriptorButtons[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
    S content = pDialogContentSupplier.apply(defaultButton::setEnabled);

    JPanel borderPane = new _NonScrollablePanel(new BorderLayout())
    {
    };
    borderPane.add(content, BorderLayout.CENTER);
    borderPane.setBorder(new EmptyBorder(7, 7, 0, 7));
    dialogDescriptor.setMessage(borderPane);
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
    dialog.setResizable(true);
    dialog.setMinimumSize(new Dimension(250, 50));
    dialog.pack();
    dialog.setVisible(true);

    Object pressedButtonObject = dialogDescriptor.getValue();
    EButtons pressedButton;
    if (pressedButtonObject.equals(defaultButton))
      pressedButton = pButtons[0];
    else if (pressedButtonObject instanceof EButtons)
      pressedButton = (EButtons) pressedButtonObject;
    else
      pressedButton = EButtons.ESCAPE;

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

}
