package de.adito.git.gui.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * DocumentListener that merges all possible events from the DocumentListener into one method
 *
 * @author m.kaspera, 30.07.2019
 */
public abstract class ADocumentListener implements DocumentListener
{

  @Override
  public void insertUpdate(DocumentEvent pE)
  {
    updated(pE);
  }

  @Override
  public void removeUpdate(DocumentEvent pE)
  {
    updated(pE);
  }

  @Override
  public void changedUpdate(DocumentEvent pE)
  {
    updated(pE);
  }

  public abstract void updated(DocumentEvent pE);

}
