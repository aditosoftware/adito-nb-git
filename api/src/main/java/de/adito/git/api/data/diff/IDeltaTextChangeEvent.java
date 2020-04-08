package de.adito.git.api.data.diff;

import org.jetbrains.annotations.Nullable;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Describes a change that needs to happen after a Delta was accepted in order to keep a text in synch with the diff text
 *
 * @author m.kaspera, 06.03.2020
 */
public interface IDeltaTextChangeEvent
{

  /**
   * @return offset that the change happened at
   */
  int getOffset();

  /**
   * @return number of characters affected by the change in the existing text (0 for an insert, > 0 for a delete or modify operation)
   */
  int getLength();

  /**
   * @return the Text that gets inserted at the offset, empty String for a delete, non-empty for an insert or a modify operation
   */
  String getText();

  /**
   * Applies the changes to the given String, since Strings are immutable the resulting String is returned
   *
   * @param pTarget String to apply the changes to
   * @return resulting string
   */
  String apply(String pTarget);

  /**
   * Applies the changes to the given Document
   *
   * @param pDocument Document that the changes should be applied to
   * @throws BadLocationException if the changeEvent cannot be inserted into the document (due to length or similar)
   */
  void apply(Document pDocument) throws BadLocationException;

  @Nullable
  EChangeSide getSide();

  /**
   * @return the fileDiff that triggered this event
   */
  @Nullable
  IFileDiff getFileDiff();

}
