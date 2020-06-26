package de.adito.git.gui.dialogs.panels;

import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Defines methods for Guice, methods create panels that can be used for dialogs
 *
 * @author m.kaspera, 18.06.2020
 */
public interface IPanelFactory
{

  /**
   * Creates a Panel with 3 parts: One Component on top that is always shown, one pre-determined component in the middle that is also always shown and changed its
   * state when the user clicks on it, and one component on the bottom that is only shown once the user clicked on the middle component. T
   *
   * @param pUpperComponent Component that is always shown. Should contain all fields for information necessary
   * @param pLowerComponent Component that is only shown if the user clicks on the "Show more" button.
   * @return JPanel with the components laid out as described
   */
  <T> ExpandablePanel<T> getExpandablePanel(@NotNull @Assisted("upper") AditoBaseDialog<T> pUpperComponent, @NotNull @Assisted("lower") JComponent pLowerComponent);

  /**
   * Creates a panel that has a label that will take the value of pMessage and a checkbox below. The checkbox will have the description from pCheckboxText
   *
   * @param pMessage      Text to be shown for the label on top
   * @param pCheckboxText Text set as description for the checkbox
   * @return Panel with a label and a checkbox laid out as described above
   */
  CheckboxPanel createCheckboxPanel(@NotNull @Assisted("message") String pMessage, @NotNull @Assisted("checkbox") String pCheckboxText);

  /**
   * Creates a panel with a textfield and some borders, also gives the textField the pre-set value of pDefault
   *
   * @param pDefault Text that will be set as default to the TextField
   * @return Panel with a textfield
   */
  UserPromptPanel createUserPromptPanel(@Nullable String pDefault);

  /**
   * Creates a simple, styled panel with a label that has the value of pMessage
   *
   * @param pMessage String used as value for the central label
   * @return Styled panel displaying a simple message
   */
  NotificationPanel createNotificationPanel(@NotNull String pMessage);

  /**
   * Creates a panel with a label of value pMessage above a combobox that has the options passed in pOptions.
   * The Combobox does not have a default value and as such no element will be selected at the start
   *
   * @param pMessage Message used as value for the label
   * @param pOptions Options for the ComboBox
   * @return Panel with a label and a combobox, laid out as described above
   */
  ComboBoxPanel<Object> createComboBoxPanel(@NotNull String pMessage, @NotNull List<Object> pOptions);

}
