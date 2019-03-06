package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author m.kaspera, 06.03.2019
 */
public class EditorKitChangeObservable extends AbstractListenerObservable<PropertyChangeListener, JEditorPane, Object>
{
  public EditorKitChangeObservable(@NotNull JEditorPane pListenableValue)
  {
    super(pListenableValue);
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull JEditorPane pEditorPane, @NotNull IFireable<Object> pIFireable)
  {
    PropertyChangeListener listener = evt -> {
      if ("editorKit".equals(evt.getPropertyName()))
      {
        pIFireable.fireValueChanged(evt.getNewValue());
      }
    };
    pEditorPane.addPropertyChangeListener(listener);
    return listener;
  }

  @Override
  protected void removeListener(@NotNull JEditorPane pEditorPane, @NotNull PropertyChangeListener pPropertyChangeListener)
  {
    pEditorPane.removePropertyChangeListener(pPropertyChangeListener);
  }
}
