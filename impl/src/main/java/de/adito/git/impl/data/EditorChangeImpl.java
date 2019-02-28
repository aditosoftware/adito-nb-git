package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeType;
import de.adito.git.api.data.IEditorChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains the changes that have to happen to keep an editorPane up to date with a list of IFileChangeChunks that changed.
 *
 * @author m.kaspera, 27.02.2019
 */
public class EditorChangeImpl implements IEditorChange
{

  private final int offset;
  private final int length;
  private final String text;

  EditorChangeImpl(int pOffset, int pLength, @Nullable String pText)
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

  @Nullable
  @Override
  public String getText()
  {
    return text;
  }

  @NotNull
  @Override
  public EChangeType getType()
  {
    if (text == null && length <= 0)
      return EChangeType.SAME;
    if (text == null)
      return EChangeType.DELETE;
    if (length == -1)
      return EChangeType.ADD;
    if (length > 0)
      return EChangeType.MODIFY;
    return EChangeType.SAME;
  }
}
