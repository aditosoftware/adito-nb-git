package de.adito.git.gui.progress;

import de.adito.git.api.progress.*;
import org.jetbrains.annotations.*;

import java.util.concurrent.*;

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
