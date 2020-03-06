package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.IDeltaTextChangeEvent;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * @author m.kaspera, 06.03.2020
 */
public class DeltaTextChangeEventImpl implements IDeltaTextChangeEvent
{

  private final int offset;
  private final int length;
  private final String text;

  public DeltaTextChangeEventImpl(int pOffset, int pLength, String pText)
  {
    offset = pOffset;
    length = pLength;
    text = pText;
  }

  @Override
  public int getOffset()
  {
    return offset;
  }

  @Override
  public int getLength()
  {
    return length;
  }

  @Override
  public String getText()
  {
    return text;
  }

  @Override
  public String apply(String pTarget)
  {
    String prefix = pTarget.substring(0, offset);
    String postfix = pTarget.substring(offset + length);
    return prefix + text + postfix;
  }

  @Override
  public void apply(Document pDocument) throws BadLocationException
  {
    // text is empty -> delete operation
    if (text.isEmpty())
    {
      pDocument.remove(offset, length);
    }
    else
    {
      // length != 0 -> modify operation -> remove existing part to modify first
      if (length != 0)
      {
        pDocument.remove(offset, length);
      }
      pDocument.insertString(offset, text, null);
    }
  }
}
