package de.adito.git.gui.swing;

import de.adito.git.gui.dialogs.EButtons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a base class for a button that can choose if it fires the "mouse pressed" event further or hides it.
 * To accomplish this, an actionlistener is added on the JButton that this class extends.
 * All other actionListeners are added to a local list of actionListeners. These two things in combination mean that the "button pressed" event would stop
 * at the custom ActionListener. However, that custom ActionListener can the decide to fire the action to the actionListeners in the local list, thereby
 * restoring the old functionality - in case it wants to
 *
 * @author m.kaspera, 01.07.2021
 */
public abstract class ConditionalDialogButton extends JButton
{

  final List<ActionListener> actionListeners = new ArrayList<>();

  protected ConditionalDialogButton()
  {
    super.addActionListener(getCustomListener());
  }

  protected ConditionalDialogButton(Icon icon)
  {
    super(icon);
    super.addActionListener(getCustomListener());
  }

  ConditionalDialogButton(String text)
  {
    super(text);
    super.addActionListener(getCustomListener());
  }

  protected ConditionalDialogButton(Action a)
  {
    super(a);
    super.addActionListener(getCustomListener());
  }

  protected ConditionalDialogButton(String text, Icon icon)
  {
    super(text, icon);
    super.addActionListener(getCustomListener());
  }


  @Override
  public void addActionListener(ActionListener l)
  {
    actionListeners.add(l);
  }

  /**
   * This is the custom listener that can choose to propagate the action events or insert a dialog between action event and its propagation
   *
   * @return ActionListener that will be added to the parent of this class
   */
  @NotNull
  abstract ActionListener getCustomListener();

  /**
   * Returns the pressed button of the added dialog, may also be the original button if no additional dialog is shown
   *
   * @return EButtons
   */
  @NotNull
  abstract EButtons getPressedButton();
}
