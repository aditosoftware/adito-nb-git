package de.adito.git.gui.guice.dummies;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.data.diff.IFileChangeType;
import lombok.NonNull;

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
  public void openFile(@NonNull String pAbsolutePath)
  {
    // no implementation
  }

  @Override
  public void openFile(@NonNull File pFile)
  {
    // no implementation
  }

  @Override
  public void preLoadIcons(@NonNull List<IFileChangeType> pFiles)
  {
    // no implementation
  }

  @Override
  public Image getIcon(@NonNull File pFile, boolean pIsOpened)
  {
    return null;
  }

  @NonNull
  @Override
  public Charset getEncoding(@NonNull File pFile)
  {
    return Charset.defaultCharset();
  }

  @NonNull
  @Override
  public Charset getEncoding(@NonNull byte[] pContent)
  {
    return StandardCharsets.UTF_8;
  }
}
