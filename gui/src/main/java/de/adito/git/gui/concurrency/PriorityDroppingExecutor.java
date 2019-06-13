package de.adito.git.gui.concurrency;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Executor that queues tasks, and if it gets a priority task tries to drop all queued and ongoing tasks and run the new priority task ASAP
 * This is useful if all tasks submitted to the executor depend on the latest, and only the latest, results of one task
 *
 * @author m.kaspera, 12.06.2019
 */
public class PriorityDroppingExecutor extends ThreadPoolExecutor
{

  private final Queue<Future<?>> trackedTasks = new LinkedList<>();

  public PriorityDroppingExecutor()
  {
    super(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
  }

  /**
   * Queues the Task in the Single-thread executor of this class
   *
   * @param pRunnable Runnable to execute
   */
  public void invokeAfterComputations(@NotNull Runnable pRunnable)
  {
    while (trackedTasks.peek() != null && trackedTasks.peek().isDone())
    {
      trackedTasks.poll();
    }
    Future<?> future = submit(pRunnable);
    trackedTasks.add(future);
  }

  /**
   * method to introduce a runnable with high priority to the pool, removes all other queued futures/runnables due to the assumption that those
   * need the values of the high-priority task and will eventually be re-queued anyways
   *
   * @param pRunnable Runnable that should be executed ASAP
   */
  public void invokePriority(@NotNull Runnable pRunnable)
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

}
