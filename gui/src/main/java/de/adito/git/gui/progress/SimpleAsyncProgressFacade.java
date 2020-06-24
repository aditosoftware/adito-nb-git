package de.adito.git.gui.progress;

import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Dummy Swing ProgressFacade
 * Executes the given task immediately
 *
 * @author w.glanzer, 13.12.2018
 */
public class SimpleAsyncProgressFacade implements IAsyncProgressFacade
{

  @NotNull
  @Override
  public <T, Ex extends Throwable> Future<T> executeInBackground(@NotNull String pSimpleName, @NotNull IExec<T, Ex> pExecutor)
  {
    try
    {
      return CompletableFuture.completedFuture(pExecutor.get(new _DummyHandle()));
    }
    catch (Throwable ex)
    {
      throw new RuntimeException("Exception in simple progress facade for task " + pSimpleName, ex);
    }
  }

  @NotNull
  @Override
  public <T, Ex extends Throwable> T executeAndBlockWithProgress(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor)
  {
    try
    {
      return CompletableFuture.completedFuture(pExecutor.get(new _DummyHandle())).get();
    }
    catch (Throwable ex)
    {
      throw new RuntimeException("Exception in simple progress facade for task " + pDisplayName, ex);
    }
  }

  private static class _DummyHandle implements IProgressHandle
  {
    @Override
    public void setDisplayName(@Nullable String pMessage)
    {
      //nothing
    }

    @Override
    public void setDescription(@Nullable String pMessage)
    {
      // nothing
    }

    @Override
    public void progress(int pUnitsCompleted)
    {
      // nothing
    }

    @Override
    public void switchToDeterminate(int pUnits)
    {
      // nothing
    }

    @Override
    public void switchToIndeterminate()
    {
      // nothing
    }

    @Override
    public void finish()
    {
      // nothing
    }
  }
}
