package de.adito.git.gui.dialogs.panels.basediffpanel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

import java.awt.Component;
import java.awt.event.*;
import java.util.Optional;

/**
 * Wrapper around an Observable that fires once on the first event of a mouse or mouseWheelListener, then removes all its listeners and does not fire again.
 * This means only the very first action of the mouse on any of the given components is detected by the observable
 *
 * @author m.kaspera, 13.05.2020
 */
public class MouseFirstActionObservableWrapper extends MouseAdapter implements MouseWheelListener
{

  private final BehaviorSubject<Optional<Object>> subject = BehaviorSubject.createDefault(Optional.empty());
  private final Component[] components;

  public MouseFirstActionObservableWrapper(Component... pComponents)
  {
    components = pComponents;
    for (Component component : components)
    {
      component.addMouseWheelListener(this);
      component.addMouseListener(this);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
    _eventFired();
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    _eventFired();
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    _eventFired();
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    _eventFired();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    _eventFired();
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    _eventFired();
  }

  /**
   * A mouseAction took place, fire a new value on the observable and remove this listener from all components it is registered to
   */
  private void _eventFired()
  {
    subject.onNext(Optional.of(new Object()));
    for (Component component : components)
    {
      component.removeMouseWheelListener(this);
      component.removeMouseListener(this);
    }
  }

  public Observable<Optional<Object>> getObservable()
  {
    return subject;
  }
}
