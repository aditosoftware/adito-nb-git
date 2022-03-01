package de.adito.git.api.progress;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

/**
 * Facade to execute async (background) progresses
 *
 * @author w.glanzer, 13.12.2018
 */
public interface IAsyncProgressFacade
{

  /**
   * Executes an Action in Background with progess handle
   *
   * @param pDisplayName Name of the Background Process
   * @param pExecutor    Executing-Function
   */
  default <Ex extends Throwable> void executeInBackground(@NotNull String pDisplayName, @NotNull IVoidExec<Ex> pExecutor)
  {
    executeInBackground(pDisplayName, pProgressHandle -> {
      pExecutor.get(pProgressHandle);
      return null;
    });
  }

  /**
   * Executes an Action in Background with progess handle
   *
   * @param pDisplayName Name of the Background Process
   * @param pExecutor    Executing-Function
   * @return A Future containing the calculated value
   */
  @NotNull <T, Ex extends Throwable> Future<T> executeInBackground(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor);

  /**
   * Executes an Action in Background with progess handle
   *
   * @param pDisplayName Name of the Background Process
   * @param pExecutor    Executing-Function
   */
  default <Ex extends Throwable> void executeInBackgroundWithoutIndexing(@NotNull String pDisplayName, @NotNull IVoidExec<Ex> pExecutor)
  {
    executeInBackgroundWithoutIndexing(pDisplayName, pProgressHandle -> {
      pExecutor.get(pProgressHandle);
      return null;
    });
  }

  /**
   * Executes an Action in Background with progess handle
   *
   * @param pDisplayName Name of the Background Process
   * @param pExecutor    Executing-Function
   * @return A Future containing the calculated value
   */
  @NotNull <T, Ex extends Throwable> Future<T> executeInBackgroundWithoutIndexing(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor);

  /**
   * Executes an Action. While the action is running, the UI is blocked for the user. However, a progress bar is shown.
   * The progress bar can either show indeterminate progress or display the actual progress in work units
   *
   * @param pDisplayName Name of the Process, shown in the progress bar
   * @param pExecutor    Executing-Function
   */
  default <Ex extends Throwable> void executeAndBlockWithProgress(@NotNull String pDisplayName, @NotNull IVoidExec<Ex> pExecutor)
  {
    executeAndBlockWithProgress(pDisplayName, pProgressHandle -> {
      pExecutor.get(pProgressHandle);
      return null;
    });
  }

  /**
   * Executes an Action. While the action is running, the UI is blocked for the user. However, a progress bar is shown.
   * The progress bar can either show indeterminate progress or display the actual progress in work units
   *
   * @param pDisplayName Name of the Process, shown in the progress bar
   * @param pExecutor    Executing-Function
   * @param <T>          return type
   * @return the resulting value of the computation. Since this method is blocking there is no need for a future
   */
  @NotNull <T, Ex extends Throwable> T executeAndBlockWithProgress(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor);

  /**
   * Executes an Action. While the action is running, the UI is blocked for the user. However, a progress bar is shown.
   * The progress bar can either show indeterminate progress or display the actual progress in work units
   *
   * @param pDisplayName Name of the Process, shown in the progress bar
   * @param pExecutor    Executing-Function
   */
  default <Ex extends Throwable> void executeAndBlockWithProgressWithoutIndexing(@NotNull String pDisplayName, @NotNull IVoidExec<Ex> pExecutor)
  {
    executeAndBlockWithProgressWithoutIndexing(pDisplayName, pProgressHandle -> {
      pExecutor.get(pProgressHandle);
      return null;
    });
  }

  /**
   * Executes an Action. While the action is running, the UI is blocked for the user. However, a progress bar is shown.
   * The progress bar can either show indeterminate progress or display the actual progress in work units
   *
   * @param pDisplayName Name of the Process, shown in the progress bar
   * @param pExecutor    Executing-Function
   * @param <T>          return type
   * @return the resulting value of the computation. Since this method is blocking there is no need for a future
   */
  @NotNull <T, Ex extends Throwable> T executeAndBlockWithProgressWithoutIndexing(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor);

  /**
   * Exec-Description with no return value
   */
  interface IVoidExec<Ex extends Throwable>
  {
    void get(@NotNull IProgressHandle pProgressHandle) throws Ex;
  }

  /**
   * Exec-Description with Return value
   */
  interface IExec<T, Ex extends Throwable>
  {
    @Nullable
    T get(@NotNull IProgressHandle pProgressHandle) throws Ex;
  }

}
