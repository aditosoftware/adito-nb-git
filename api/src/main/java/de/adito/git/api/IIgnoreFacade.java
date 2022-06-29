package de.adito.git.api;

import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;

/**
 * @author w.glanzer, 28.06.2022
 */
public interface IIgnoreFacade
{

  /**
   * Determines if a file is ignored or excluded
   *
   * @param pFile File to check
   * @return true if this file is ignored, false if it is not or it is undeterminable
   */
  boolean isIgnored(@NotNull File pFile);

  /**
   * Adds the given files to the gitignore file
   *
   * @param pFiles Files to ignore
   */
  void ignore(@NotNull List<File> pFiles) throws IOException;

  /**
   * Adds the given files to the git exclusion file
   *
   * @param pFiles Files to exclude
   */
  void exclude(@NotNull List<File> pFiles) throws IOException;

  /**
   * @return Observable to track, if something changed
   */
  @NotNull
  Observable<Long> observeIgnorationChange();

}
