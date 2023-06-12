package de.adito.git.gui.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;

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

  @NonNull
  public static Future<?> submit(@NonNull Runnable pRunnable)
  {
    return DEFAULT_BACKGROUND_EXECUTOR.submit(pRunnable);
  }

  @NonNull
  public static <T> Future<T> submit(@NonNull Runnable pRunnable, @NonNull T pResult)
  {
    return DEFAULT_BACKGROUND_EXECUTOR.submit(pRunnable, pResult);
  }

}
