package de.adito.git.impl;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * ArrayBlockingQueue that does its best to disregard any item that is offered/put if the queue is full.
 * Items offered/put when the queue is full just vanish, while the method still returns true
 *
 * @author m.kaspera, 20.02.2019
 */
public class VoidingPseudoBlockingArrayQueue<T> extends ArrayBlockingQueue<T>
{
  public VoidingPseudoBlockingArrayQueue(int pCapacity)
  {
    super(pCapacity);
  }

  public VoidingPseudoBlockingArrayQueue(int pCapacity, boolean pFair)
  {
    super(pCapacity, pFair);
  }

  public VoidingPseudoBlockingArrayQueue(int pCapacity, boolean pFair, Collection<? extends T> pCollection)
  {
    super(pCapacity, pFair, pCollection);
  }

  /**
   * offers an item and returns true, thus an object passed into a full queue just vanishes
   *
   * @param pO item to offer
   * @return true
   */
  @Override
  @SuppressWarnings("squid:S899") // This class is explicitly made so that any items that are entered while the queue is full are discarded -> not necessary to do
  // anything with the return from offer
  public boolean offer(T pO)
  {
    super.offer(pO);
    return true;
  }

  /**
   * just offers the item, thus an object passed into a full queue just vanishes
   *
   * @param pO object to put into the queue if it is not full
   */
  @Override
  @SuppressWarnings("squid:S899") // This class is explicitly made so that any items that are entered while the queue is full are discarded -> not necessary to do
  // anything with the return from offer
  public void put(T pO)
  {
    super.offer(pO);
  }

  /**
   * @param pO       the element to add
   * @param pTimeout how long to wait before giving up, in unit
   * @param pUnit    a time unit determining how to interpret timeout
   * @return true
   * @throws InterruptedException if interrupted while waiting
   */
  @Override
  @SuppressWarnings("squid:S899") // This class is explicitly made so that any items that are entered while the queue is full are discarded -> not necessary to do
  // anything with the return from offer
  public boolean offer(T pO, long pTimeout, TimeUnit pUnit) throws InterruptedException
  {
    super.offer(pO, pTimeout, pUnit);
    return true;
  }
}
