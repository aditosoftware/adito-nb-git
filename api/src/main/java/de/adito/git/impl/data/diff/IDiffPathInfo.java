package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author m.kaspera, 19.03.2020
 */
public interface IDiffPathInfo
{

  @Nullable
  File getTopLevelDirectory();

  @NotNull
  String getFilePath(EChangeSide pChangeSide);

}
