package de.adito.git.api.data;

import org.jetbrains.annotations.NotNull;

/**
 * Specifies the changes the editor has to make in order to keep the contents of the editor synced up with the list of IFileChangeChunks
 *
 * @author m.kaspera, 27.02.2019
 */
public interface IEditorChangeEvent
{

  @NotNull
  IEditorChange getChange(EChangeSide pChangeSide);

}
