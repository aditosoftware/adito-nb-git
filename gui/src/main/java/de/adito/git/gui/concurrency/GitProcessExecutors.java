package de.adito.git.gui.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides access to shared ThreadPools and Executors for e.g. background tasks
 *
 * @author m.kaspera, 01.04.2021
 */
public class GitProcessExecutors
{

  private static final ExecutorService DEFAULT_BACKGROUND_EXECUTOR = Executors.newSingleThreadExecutor();

  public static ExecutorService getDefaultBackgroundExecutor()
  {
    return DEFAULT_BACKGROUND_EXECUTOR;
  }

}
