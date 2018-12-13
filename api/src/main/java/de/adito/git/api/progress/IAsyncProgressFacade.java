package de.adito.git.api.progress;

import org.jetbrains.annotations.*;

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
  @NotNull
  <T, Ex extends Throwable> Future<T> executeInBackground(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor);

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
