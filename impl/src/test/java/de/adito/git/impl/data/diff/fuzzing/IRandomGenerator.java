package de.adito.git.impl.data.diff.fuzzing;

/**
 * Defines a random number generator that returns doubles in the range from 0 (inclusive) and 1 (exclusive)
 *
 * @author m.kaspera, 26.02.2020
 */
public interface IRandomGenerator
{

  /**
   * Get a new random number, interval from [0, 1[
   *
   * @return a new random number between 0 (inclusive) and 1 (exclusive)
   */
  double get();

}
