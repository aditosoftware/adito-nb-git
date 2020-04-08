package de.adito.git.gui.guice.dummies;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.diff.IFileChangeType;
import org.jetbrains.annotations.NotNull;

import java.awt.Image;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleFileSystemUtil implements IFileSystemUtil
{
  @Override
  public void openFile(@NotNull String pAbsolutePath)
  {
    // no implementation
  }

  @Override
  public void openFile(@NotNull File pFile)
  {
    // no implementation
  }

  @Override
  public void preLoadIcons(@NotNull List<IFileChangeType> pFiles)
  {
    // no implementation
  }

  @Override
  public Image getIcon(@NotNull File pFile, boolean pIsOpened)
  {
    return null;
  }

  @NotNull
  @Override
  public Charset getEncoding(@NotNull File pFile)
  {
    return Charset.defaultCharset();
  }

  @NotNull
  @Override
  public Charset getEncoding(@NotNull byte[] pContent)
  {
    return StandardCharsets.UTF_8;
  }
}
