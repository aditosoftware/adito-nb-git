package de.adito.git.nbm.sidebar;

import javax.swing.event.*;

/**
 * @author a.arnold, 26.11.2018
 */
public abstract class DocumentUpdateChangeListener implements DocumentListener
{
  public void changedUpdate(DocumentEvent pEvent)
  {
    update(pEvent);
  }

  public void insertUpdate(DocumentEvent pEvent)
  {
    update(pEvent);
  }

  public void removeUpdate(DocumentEvent pEvent)
  {
    update(pEvent);
  }

  public abstract void update(DocumentEvent pE);
}
