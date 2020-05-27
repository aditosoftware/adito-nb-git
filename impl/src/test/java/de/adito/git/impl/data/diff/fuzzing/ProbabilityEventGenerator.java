package de.adito.git.impl.data.diff.fuzzing;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author m.kaspera, 26.02.2020
 */
public class ProbabilityEventGenerator<EVENTTYPE>
{

  private final IRandomGenerator randomGenerator;
  private final TreeMap<Double, EVENTTYPE> probabilityMap = new TreeMap<>();

  ProbabilityEventGenerator(IRandomGenerator pRandomGenerator, List<EventShares<EVENTTYPE>> pProbabilities)
  {
    randomGenerator = pRandomGenerator;
    double probabilitySum = 0;
    long numTotalShares = pProbabilities.stream().map(EventShares::getShares).mapToInt(Integer::intValue).sum();
    for (EventShares<EVENTTYPE> eventShares : pProbabilities)
    {
      probabilityMap.put(probabilitySum, eventShares.getEvent());
      probabilitySum += (double) eventShares.getShares() / numTotalShares;
    }
  }

  /**
   * draws the next random Event
   *
   * @return a random event, with the likelyhood of each event being the one set in the builder
   */
  public EVENTTYPE randomEvent()
  {
    double value = randomGenerator.get();
    Double floorKey = probabilityMap.floorKey(value);
    return probabilityMap.get(floorKey);
  }

  /**
   * Builder for easy creation of the ProbabilityEventGenerator
   *
   * @param <EVENTTYPE> Type of the Event
   */
  public static class Builder<EVENTTYPE>
  {
    private final List<EventShares<EVENTTYPE>> probabilities = new ArrayList<>();
    private IRandomGenerator randomGenerator;

    /**
     * adds an event and its probability. The combined events should have a probability of 1.
     *
     * @param pEventShares Event and its Probability as EventProbability
     * @return this Builder
     */
    public Builder<EVENTTYPE> addEvent(EventShares<EVENTTYPE> pEventShares)
    {
      probabilities.add(pEventShares);
      return this;
    }

    /**
     * sets the IRandomGenerator used to random the events
     *
     * @param pRandomGenerator IRandomGenerator
     * @return this Builder
     */
    public Builder<EVENTTYPE> setRandomGenerator(IRandomGenerator pRandomGenerator)
    {
      randomGenerator = pRandomGenerator;
      return this;
    }

    /**
     * creates the ProbabilityEventGenerator, the random Generator has to be set or an IllegalArgumentException is thrown
     *
     * @return ProbabilityEventGenerator with the parameters set in this builder
     */
    public ProbabilityEventGenerator<EVENTTYPE> create()
    {
      if (randomGenerator == null)
        throw new IllegalArgumentException();
      return new ProbabilityEventGenerator<>(randomGenerator, probabilities);
    }
  }

  /**
   * Defines a single Event together with its amount of shares, the percentage of shares of this event compared to the total amount of shares
   * determines the probability of this event
   *
   * @param <EVENTTYPE> Type of the Event
   */
  public static class EventShares<EVENTTYPE>
  {
    private final int shares;
    private final EVENTTYPE event;

    /**
     * @param pShares number of shares, the percentage of shares of this event compared to the total amount of shares determines the probability of this event
     * @param pEvent  the Event
     */
    public EventShares(int pShares, EVENTTYPE pEvent)
    {
      shares = pShares;
      event = pEvent;
    }

    public EVENTTYPE getEvent()
    {
      return event;
    }

    public int getShares()
    {
      return shares;
    }
  }

}
