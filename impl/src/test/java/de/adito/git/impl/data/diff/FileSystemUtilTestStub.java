package de.adito.git.impl.data.diff;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.IFileChangeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author m.kaspera, 20.02.2020
 */
class FileSystemUtilTestStub implements IFileSystemUtil
{
  @Override
  public void openFile(@NotNull String pAbsolutePath)
  {
    // not used for tests
  }

  @Override
  public void openFile(@NotNull File pFile)
  {
    // not used for tests
  }

  public void preLoadIcons(@NotNull List<IFileChangeType> pFiles)
  {
    // not used for tests
  }

  @Override
  public @Nullable Image getIcon(@NotNull File pFile, boolean pIsOpened)
  {
    return null;
  }

  @Override
  public @NotNull Charset getEncoding(@NotNull File pFile)
  {
    return StandardCharsets.UTF_8;
  }

  @Override
  public @NotNull Charset getEncoding(@NotNull byte[] pContent)
  {
    return StandardCharsets.UTF_8;
  }
}
