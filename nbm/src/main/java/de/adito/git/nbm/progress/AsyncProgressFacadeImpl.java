package de.adito.git.nbm.progress;

import com.google.inject.Singleton;
import de.adito.git.api.progress.*;
import org.jetbrains.annotations.*;
import org.netbeans.api.progress.*;
import org.openide.util.RequestProcessor;

import java.util.concurrent.*;

/**
 * @author w.glanzer, 13.12.2018
 */
@Singleton
public class AsyncProgressFacadeImpl implements IAsyncProgressFacade
{

  private final RequestProcessor _PROCESSOR = new RequestProcessor("ADITO VCS Async Processor");

  @NotNull
  @Override
  public <T, Ex extends Throwable> Future<T> executeInBackground(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor)
  {
    _NetBeansHandle handle = new _NetBeansHandle(pDisplayName);
    return _PROCESSOR.submit(new _Runner<>(handle, pExecutor));
  }

  /**
   * Executes the given Task and completes the underlying progress handle
   */
  private static class _Runner<T, Ex extends Throwable> implements Callable<T>
  {
    private final _NetBeansHandle handle;
    private final IExec<T, Ex> exec;

    public _Runner(@NotNull _NetBeansHandle pProgressHandle, @NotNull IExec<T, Ex> pExec)
    {
      handle = pProgressHandle;
      exec = pExec;
    }

    @Override
    public T call()
    {
      try
      {
        handle.start();
        return exec.get(handle);
      }
      catch (Throwable ex)
      {
        // Delegate all Exceptions to our uncaught exception handler -> otherwise they won't be shown on GUI
        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
        throw new RuntimeException("Error in async task " + handle.getDisplayName(), ex);
      }
      finally
      {
        handle.finish();
      }
    }
  }

  /**
   * ProgressHandle-Impl
   */
  private static class _NetBeansHandle implements IProgressHandle
  {
    private final String displayName;
    private final ProgressHandle handle;
    private boolean inProgress = false;

    public _NetBeansHandle(@NotNull String pDisplayName)
    {
      displayName = pDisplayName;
      handle = ProgressHandle.createHandle(pDisplayName);
    }

    @Override
    public void setDescription(@Nullable String pMessage)
    {
      if(!inProgress)
        return;
      handle.progress(pMessage == null ? "" : pMessage);
    }

    @Override
    public void progress(int pUnitsCompleted)
    {
      if(!inProgress)
        return;
      handle.progress(pUnitsCompleted);
    }

    @Override
    public void switchToDeterminate(int pUnits)
    {
      if(!inProgress)
        return;
      handle.switchToDeterminate(pUnits);
    }

    @Override
    public void switchToIndeterminate()
    {
      if(!inProgress)
        return;
      handle.switchToIndeterminate();
    }

    /**
     * Finish this task
     */
    @Override
    public void finish()
    {
      inProgress = false;
      handle.finish();
    }

    /**
     * @return Current displayName
     */
    @NotNull
    protected String getDisplayName()
    {
      return displayName;
    }

    /**
     * Start the task
     */
    protected void start()
    {
      inProgress = true;
      handle.start();
    }
  }

}
