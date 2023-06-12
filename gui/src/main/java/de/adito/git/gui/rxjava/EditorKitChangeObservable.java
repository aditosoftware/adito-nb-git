package de.adito.git.gui.rxjava;

import de.adito.util.reactive.AbstractListenerObservable;
import lombok.NonNull;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author m.kaspera, 06.03.2019
 */
public class EditorKitChangeObservable extends AbstractListenerObservable<PropertyChangeListener, JEditorPane, Object>
{
  public EditorKitChangeObservable(@NonNull JEditorPane pListenableValue)
  {
    super(pListenableValue);
  }

  @NonNull
  @Override
  protected PropertyChangeListener registerListener(@NonNull JEditorPane pEditorPane, @NonNull IFireable<Object> pIFireable)
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
  protected void removeListener(@NonNull JEditorPane pEditorPane, @NonNull PropertyChangeListener pPropertyChangeListener)
  {
    pEditorPane.removePropertyChangeListener(pPropertyChangeListener);
  }
}
