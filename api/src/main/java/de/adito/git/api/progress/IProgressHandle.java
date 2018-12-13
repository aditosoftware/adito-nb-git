package de.adito.git.api.progress;

import org.jetbrains.annotations.Nullable;

/**
 * @author w.glanzer, 13.12.2018
 */
public interface IProgressHandle
{

  /**
   * Sets the Description of this handle.
   *
   * @param pMessage The new Message or <tt>null</tt>
   */
  void setDescription(@Nullable String pMessage);

  /**
   * Sets the new units counter
   *
   * @param pUnitsCompleted complete amount of units, already completed
   */
  void progress(int pUnitsCompleted);

  /**
   * Currently indeterminate task can be switched to show percentage completed.
   *
   * @param pUnits Units
   */
  void switchToDeterminate(int pUnits);

  /**
   * Currently determinate task (with percentage or time estimate) can be
   * switched to indeterminate mode.
   */
  void switchToIndeterminate();

  /**
   * Finishes this handle -> it will hide from taskbar
   */
  void finish();

}
