package de.adito.git.impl.data.diff;

import de.adito.git.api.data.diff.EChangeSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author m.kaspera, 19.03.2020
 */
public final class DiffPathInfoImpl implements IDiffPathInfo
{

  private final File topLevelDirectory;
  private final String oldFilePath;
  private final String newFilePath;

  public DiffPathInfoImpl(@Nullable File pTopLevelDirectory, @NotNull String pOldFilePath, @NotNull String pNewFilePath)
  {
    topLevelDirectory = pTopLevelDirectory;
    oldFilePath = pOldFilePath;
    newFilePath = pNewFilePath;
  }

  @Override
  @Nullable
  public File getTopLevelDirectory()
  {
    return topLevelDirectory;
  }

  @NotNull
  @Override
  public String getFilePath(EChangeSide pChangeSide)
  {
    return pChangeSide == EChangeSide.NEW ? newFilePath : oldFilePath;
  }
}
