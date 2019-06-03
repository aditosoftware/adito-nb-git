package de.adito.git.impl.observables;

import de.adito.util.reactive.AbstractListenerObservable;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeListener;

/**
 * Observes a JEditorPane and fires the new Document in case the document changes
 *
 * @author m.kaspera, 03.06.2019
 */
public class DocumentChangeObservable extends AbstractListenerObservable<PropertyChangeListener, JTextComponent, Document>
{

  public DocumentChangeObservable(@NotNull JTextComponent pListenableValue)
  {
    super(pListenableValue);
  }

  @NotNull
  @Override
  protected PropertyChangeListener registerListener(@NotNull JTextComponent pTextComponent, @NotNull IFireable<Document> pIFireable)
  {
    PropertyChangeListener listener = evt -> {
      if ("document".equals(evt.getPropertyName()))
        pIFireable.fireValueChanged(pTextComponent.getDocument());
    };
    pTextComponent.addPropertyChangeListener(listener);
    return listener;
  }

  @Override
  protected void removeListener(@NotNull JTextComponent pTextComponent, @NotNull PropertyChangeListener pPropertyChangeListener)
  {
    pTextComponent.removePropertyChangeListener(pPropertyChangeListener);
  }
}
