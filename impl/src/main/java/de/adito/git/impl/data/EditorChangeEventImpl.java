package de.adito.git.impl.data;

import de.adito.git.api.data.EChangeSide;
import de.adito.git.api.data.IEditorChange;
import de.adito.git.api.data.IEditorChangeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * contains the changes that have to happen to keep the editorPanes displaying the two sides of an IFileDiff up to date
 *
 * @author m.kaspera, 27.02.2019
 */
public class EditorChangeEventImpl implements IEditorChangeEvent
{

  private final IEditorChange aSideChange;
  private final IEditorChange bSideChange;

  EditorChangeEventImpl(IEditorChange pASideChange, IEditorChange pBSideChange)
  {
    aSideChange = pASideChange;
    bSideChange = pBSideChange;
  }

  @Override
  public @NotNull IEditorChange getChange(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.OLD ? aSideChange : bSideChange;
  }

}
