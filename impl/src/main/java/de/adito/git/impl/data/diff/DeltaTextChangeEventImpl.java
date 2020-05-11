package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;
import de.adito.git.api.data.diff.IDeltaTextChangeEvent;
import de.adito.git.api.data.diff.IFileDiff;
import org.jetbrains.annotations.Nullable;

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
  private final IFileDiff fileDiff;
  private final EChangeSide changeSide;
  private final boolean isInitFlag;

  public DeltaTextChangeEventImpl(int pOffset, int pLength, String pText, IFileDiff pFileDiff)
  {
    this(pOffset, pLength, pText, pFileDiff, null);
  }

  public DeltaTextChangeEventImpl(int pOffset, int pLength, String pText, IFileDiff pFileDiff, EChangeSide pChangeSide)
  {
    this(pOffset, pLength, pText, pFileDiff, pChangeSide, false);
  }

  public DeltaTextChangeEventImpl(int pOffset, int pLength, String pText, IFileDiff pFileDiff, EChangeSide pChangeSide, boolean pIsInitFlag)
  {
    offset = pOffset;
    length = pLength;
    text = pText;
    fileDiff = pFileDiff;
    changeSide = pChangeSide;
    isInitFlag = pIsInitFlag;
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

  @Nullable
  @Override
  public EChangeSide getSide()
  {
    return changeSide;
  }

  @Nullable
  @Override
  public IFileDiff getFileDiff()
  {
    return fileDiff;
  }

  @Override
  public boolean isInit()
  {
    return isInitFlag;
  }
}
