package de.adito.git.nbm.progress;

import com.google.inject.Singleton;
import de.adito.git.api.progress.IAsyncProgressFacade;
import de.adito.git.api.progress.IProgressHandle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author w.glanzer, 13.12.2018
 */
@Singleton
public class AsyncProgressFacadeImpl implements IAsyncProgressFacade
{
  private final RequestProcessor PROCESSOR = new RequestProcessor("ADITO VCS Async Processor");

  /**
   * a wrapper for the {@link ProgressHandle}
   *
   * @param pHandle the progressHandler
   * @return a NetBeans {@link ProgressHandle} wrap
   */
  @NotNull
  public static IProgressHandle wrapNBHandle(@NotNull ProgressHandle pHandle)
  {
    _NetBeansHandle handle = new _NetBeansHandle(null, pHandle);
    handle.inProgress = true; //workaround
    return handle;
  }

  @NotNull
  @Override
  public <T, Ex extends Throwable> Future<T> executeInBackground(@NotNull String pDisplayName, @NotNull IExec<T, Ex> pExecutor)
  {
    _NetBeansHandle handle = new _NetBeansHandle(pDisplayName, ProgressHandle.createHandle(pDisplayName));
    return PROCESSOR.submit(new _Runner<>(handle, pExecutor));
  }

  /**
   * Executes the given Task and completes the underlying progress handle
   */
  private static class _Runner<T, Ex extends Throwable> implements Callable<T>
  {
    private final _NetBeansHandle handle;
    private final IExec<T, Ex> exec;

    _Runner(@NotNull _NetBeansHandle pProgressHandle, @NotNull IExec<T, Ex> pExec)
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
        throw new RuntimeException(NbBundle.getMessage(AsyncProgressFacadeImpl.class, "Error.Async", handle.getDisplayName()), ex);
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
    private final ProgressHandle handle;
    private final String displayName;
    private boolean inProgress = false;

    _NetBeansHandle(@Nullable String pDisplayName, @NotNull ProgressHandle pHandle)
    {
      displayName = pDisplayName;
      handle = pHandle;
    }

    @Override
    public void setDisplayName(@Nullable String pMessage)
    {
      handle.setDisplayName(pMessage == null ? "" : pMessage);
    }

    @Override
    public void setDescription(@Nullable String pMessage)
    {
      if (!inProgress)
        return;
      handle.progress(pMessage == null ? "" : pMessage);
    }

    @Override
    public void progress(int pUnitsCompleted)
    {
      if (!inProgress)
        return;
      handle.progress(pUnitsCompleted);
    }

    @Override
    public void switchToDeterminate(int pUnits)
    {
      if (!inProgress)
        return;
      handle.switchToDeterminate(pUnits);
    }

    @Override
    public void switchToIndeterminate()
    {
      if (!inProgress)
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
    @Nullable
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
