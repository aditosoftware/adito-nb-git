package de.adito.git.nbm.util;

import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.*;
import javax.swing.text.*;

/**
 * An Observable to check document changes inside the editor tab
 *
 * @author a.arnold, 26.11.2018
 */
public class DocumentObservable extends AbstractListenerObservable<DocumentListener, Document, String>
{

  /**
   * An Observable to check document changes inside the editor tab
   *
   * @param pListenableValue the document inside the editor tab
   */
  private DocumentObservable(@NotNull Document pListenableValue)
  {
    super(pListenableValue);
  }

  public static Observable<String> create(Document pDocument)
  {
    return Observable.create(new DocumentObservable(pDocument))
        .startWithItem(_getText(pDocument));
  }

  @NotNull
  @Override
  protected DocumentListener registerListener(@NotNull Document pDocument, @NotNull IFireable<String> pFireable)
  {
    DocumentListener listener = new _Listener(pFireable, pDocument);
    pDocument.addDocumentListener(listener);
    return listener;
  }

  @Override
  protected void removeListener(@NotNull Document pDocument, @NotNull DocumentListener pDocumentListener)
  {
    pDocument.removeDocumentListener(pDocumentListener);
  }

  /**
   * return the text of the document
   */
  private static String _getText(Document pDocument)
  {
    try
    {
      return pDocument.getText(0, pDocument.getLength());
    }
    catch (BadLocationException e)
    {
      return "";
    }
  }

  private static class _Listener implements DocumentListener
  {
    private final IFireable<String> fireable;
    private final Document document;

    _Listener(@NotNull IFireable<String> pFireable, @NotNull Document pDocument)
    {
      fireable = pFireable;
      document = pDocument;
    }

    @Override
    public void insertUpdate(DocumentEvent pE)
    {
      fireable.fireValueChanged(_getText(document));
    }

    @Override
    public void removeUpdate(DocumentEvent pE)
    {
      fireable.fireValueChanged(_getText(document));
    }

    @Override
    public void changedUpdate(DocumentEvent pE)
    {
      fireable.fireValueChanged(_getText(document));
    }
  }
}
