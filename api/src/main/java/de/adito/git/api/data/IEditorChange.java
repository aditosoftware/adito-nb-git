package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author m.kaspera, 27.02.2019
 */
public interface IEditorChange
{

  /**
   * @return the starting offset for the delete/add operation
   */
  int getOffset();

  /**
   * This is the number of characters that have to be deleted, not the number of characters that will be added
   *
   * @return the number of character that should be deleted, or -1 if characters are only added
   */
  int getLength();

  /**
   * @return the text that should be inserted at the startOffset
   */
  @Nullable
  String getText();

  /**
   * @return which kind of operation this change is
   */
  @NotNull
  EChangeType getType();
}
