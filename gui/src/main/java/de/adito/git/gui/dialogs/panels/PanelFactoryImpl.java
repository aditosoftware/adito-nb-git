package de.adito.git.gui.dialogs.panels;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import de.adito.git.gui.dialogs.AditoBaseDialog;
import de.adito.git.gui.icon.IIconLoader;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Self-implemented factory because Guice cannot deal with Generics without having to create one instace of a factory for each used Type
 *
 * @author m.kaspera, 26.06.2020
 */
@Singleton
public class PanelFactoryImpl implements IPanelFactory
{
  @Inject
  private IIconLoader iconLoader;
  @Inject
  private IGuicePanelFactory guicePanelFactory;

  @Override
  public <T> ExpandablePanel<T> getExpandablePanel(@NonNull AditoBaseDialog<T> pUpperComponent, @NonNull JComponent pLowerComponent)
  {
    return new ExpandablePanel<>(iconLoader, pUpperComponent, pLowerComponent);
  }

  @Override
  public CheckboxPanel createCheckboxPanel(@NonNull String pMessage, @NonNull String pCheckboxText)
  {
    return guicePanelFactory.createCheckboxPanel(pMessage, pCheckboxText);
  }

  @Override
  public UserPromptPanel createUserPromptPanel(@Nullable String pDefault)
  {
    return guicePanelFactory.createUserPromptPanel(pDefault);
  }

  @Override
  public NotificationPanel createNotificationPanel(@NonNull String pMessage)
  {
    return guicePanelFactory.createNotificationPanel(pMessage);
  }

  @Override
  public ComboBoxPanel<Object> createComboBoxPanel(@NonNull String pMessage, @NonNull List<Object> pOptions)
  {
    return guicePanelFactory.createComboBoxPanel(pMessage, pOptions);
  }

  interface IGuicePanelFactory
  {
    CheckboxPanel createCheckboxPanel(@NonNull @Assisted("message") String pMessage, @NonNull @Assisted("checkbox") String pCheckboxText);

    UserPromptPanel createUserPromptPanel(@Nullable String pDefault);

    NotificationPanel createNotificationPanel(@NonNull String pMessage);

    ComboBoxPanel<Object> createComboBoxPanel(@NonNull String pMessage, @NonNull List<Object> pOptions);
  }
}
