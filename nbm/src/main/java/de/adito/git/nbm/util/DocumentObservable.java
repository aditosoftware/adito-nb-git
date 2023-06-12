package de.adito.git.nbm.util;

import de.adito.util.reactive.AbstractListenerObservable;
import io.reactivex.rxjava3.core.Observable;
import lombok.NonNull;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
  private DocumentObservable(@NonNull Document pListenableValue)
  {
    super(pListenableValue);
  }

  public static Observable<String> create(Document pDocument)
  {
    return Observable.create(new DocumentObservable(pDocument))
        .startWithItem(_getText(pDocument));
  }

  @NonNull
  @Override
  protected DocumentListener registerListener(@NonNull Document pDocument, @NonNull IFireable<String> pFireable)
  {
    DocumentListener listener = new _Listener(pFireable, pDocument);
    pDocument.addDocumentListener(listener);
    return listener;
  }

  @Override
  protected void removeListener(@NonNull Document pDocument, @NonNull DocumentListener pDocumentListener)
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

    _Listener(@NonNull IFireable<String> pFireable, @NonNull Document pDocument)
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
