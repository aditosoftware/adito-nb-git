package de.adito.git.api;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track if the
 *
 * @author m.kaspera, 15.11.2019
 */
public class UpdateFlag
{

  private static UpdateFlag instance = null;
  private final AtomicInteger permits;

  private UpdateFlag()
  {
    permits = new AtomicInteger(1);
  }

  public static UpdateFlag getInstance()
  {
    if (instance == null)
      instance = new UpdateFlag();
    return instance;
  }

  /**
   * activate git status updates
   */
  public void activate()
  {
    permits.incrementAndGet();
  }

  /**
   * deactivate git status updates
   */
  public void deactivate()
  {
    permits.getAndDecrement();
  }

  public boolean isActive()
  {
    return permits.get() > 0;
  }

}
