package de.adito.git.gui.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Provides access to shared ThreadPools and Executors for e.g. background tasks
 *
 * @author m.kaspera, 01.04.2021
 */
public class GitProcessExecutors
{

  private GitProcessExecutors()
  {
  }

  private static final ExecutorService DEFAULT_BACKGROUND_EXECUTOR = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 4),
                                                                                                  new ThreadFactoryBuilder()
                                                                                                      .setNameFormat("GitBackgroundThread-%d")
                                                                                                      .build());

  @NotNull
  public static Future<?> submit(@NotNull Runnable pRunnable)
  {
    return DEFAULT_BACKGROUND_EXECUTOR.submit(pRunnable);
  }

  @NotNull
  public static <T> Future<T> submit(@NotNull Runnable pRunnable, @NotNull T pResult)
  {
    return DEFAULT_BACKGROUND_EXECUTOR.submit(pRunnable, pResult);
  }

}
