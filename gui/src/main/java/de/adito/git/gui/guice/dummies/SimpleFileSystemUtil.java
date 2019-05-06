package de.adito.git.gui.guice.dummies;

import de.adito.git.api.IFileSystemUtil;
import de.adito.git.api.exception.AditoGitException;
import org.jetbrains.annotations.NotNull;

import java.awt.Image;
import java.io.File;
import java.nio.charset.Charset;

/**
 * @author w.glanzer, 07.02.2019
 */
public class SimpleFileSystemUtil implements IFileSystemUtil
{
  @Override
  public void openFile(String pAbsolutePath) throws AditoGitException
  {
    // no implementation
  }

  @Override
  public void openFile(File pFile) throws AditoGitException
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
}
