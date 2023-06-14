package de.adito.git.gui.concurrency;

import lombok.NonNull;

import java.util.*;
import java.util.concurrent.*;

/**
 * Executor with two different kinds of tasks: Computation and after-computation.
 * Computation tasks always start ASAP and if a new computation tasks is submitted while the previous computation is still running, the previous computation is cancelled
 * After-computation tasks are queued seperately and wait until a computation cycle is complete before they are activated. One computation cycle is the time between
 * two calls to computationDone().
 * The computation tasks are responsible for calling the computationDone method to signal that they did their computation successfully. They should not call the
 * computationDone method if they were cancelled. This is because in SwingWorkers, the status of the future you retrieve when submitting the task does not reflect
 * the completion state of the tasks because some of the computation is done in the EDT - this is done asynchronously and thus the future completes before the EDT part
 * of the computation of the SwingWorker is done.
 * In case a task might be queued several times per computation cycle but should only be executed once, a key can be given along with a after-computation task. Only one
 * task for any given key is executed per computation cycle, all others are silently discarded
 *
 * @author m.kaspera, 12.06.2019
 */
public class ComputationCycleExecutor extends ThreadPoolExecutor
{

  private final Queue<Future<?>> trackedTasks = new LinkedList<>();
  private final HashMap<String, Future<?>> keyedTasksMap = new HashMap<>();
  private final ExecutorService afterComputationsExecutor = Executors.newSingleThreadExecutor();
  private CountDownLatch countDownLatch = new CountDownLatch(1);

  public ComputationCycleExecutor()
  {
    super(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
  }

  @Override
  public void shutdown()
  {
    super.shutdown();
    afterComputationsExecutor.shutdown();
  }

  @Override
  public boolean isShutdown()
  {
    return super.isShutdown() && afterComputationsExecutor.isShutdown();
  }

  @NonNull
  @Override
  public List<Runnable> shutdownNow()
  {
    List<Runnable> runnables = afterComputationsExecutor.shutdownNow();
    runnables.addAll(super.shutdownNow());
    return runnables;
  }

  /**
   * Queues the Task in the Single-thread executor of this class
   *
   * @param pRunnable Runnable to execute
   */
  public void invokeAfterComputations(@NonNull Runnable pRunnable)
  {
    _removeFinishedFutures();
    afterComputationsExecutor.submit(_waitForLatch(pRunnable));
  }

  /**
   * Queues the Task and executes it once computationsDone is called the next time. This method only executes any action with the same key once per call to
   * computationsDone (the time between calls to computationsDone can be considered a computation cycle)
   *
   * @param pRunnable Runnable to execute
   * @param pKey      Key that is used to determine if there already is an action of this instance queued for this computation cycle
   */
  public void invokeAfterComputations(@NonNull Runnable pRunnable, String pKey)
  {
    _removeFinishedFutures();
    if (keyedTasksMap.containsKey(pKey))
    {
      if (!keyedTasksMap.get(pKey).isDone())
      {
        return;
      }
      else
      {
        trackedTasks.remove(keyedTasksMap.get(pKey));
        keyedTasksMap.remove(pKey);
      }
    }
    Future<?> future = afterComputationsExecutor.submit(_waitForLatch(pRunnable));
    keyedTasksMap.put(pKey, future);
  }

  /**
   * This method must be called to release the runnables queued by calling invokeAfterComputations
   */
  public void computationsDone()
  {
    countDownLatch.countDown();
    countDownLatch = new CountDownLatch(1);
  }

  /**
   * method to introduce a runnable with high priority to the pool, removes all other queued futures/runnables due to the assumption that those
   * need the values of the high-priority task and will eventually be re-queued anyways
   *
   * @param pRunnable Runnable that should be executed ASAP
   */
  public void invokeComputation(@NonNull Runnable pRunnable)
  {
    Future<?> trackedFuture;
    while ((trackedFuture = trackedTasks.peek()) != null)
    {
      boolean cancelled = trackedFuture.cancel(true);
      if (cancelled || trackedFuture.isDone())
        trackedTasks.poll();
    }
    purge();
    Future<?> future = submit(pRunnable);
    trackedTasks.add(future);
  }

  /**
   * Creates a new Runnable from the passed Runnable. The new Runnable waits with the execution of the passed Runnable until the
   *
   * @param pRunnable runnable that should wait for the countDownLatch to reach 0
   * @return passed runnable wrapped in an await call to the countDownLatch
   */
  private Runnable _waitForLatch(Runnable pRunnable)
  {
    return () -> {
      try
      {
        countDownLatch.await();
        pRunnable.run();
      }
      catch (InterruptedException pE)
      {
        Thread.currentThread().interrupt();
        // nothing, runnable/future cancelled
      }
    };
  }

  /**
   * removes all Futures that have the done state from the list of tracked tasks
   */
  private void _removeFinishedFutures()
  {
    while (trackedTasks.peek() != null && trackedTasks.peek().isDone())
    {
      trackedTasks.poll();
    }
  }

}
