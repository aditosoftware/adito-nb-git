package de.adito.git.impl.util;

import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

/**
 * Navigateable map that is bi-directional, similar to BiMap from the Guava package, just with a Navigateable map
 * Does not extend TreeMap because that would mean that most functions would have to be overwritten when the methods defined here should do for
 * the expected use case
 *
 * @author m.kaspera, 04.03.2019
 */
public class BiNavigateAbleMap<K extends Comparable, V extends Comparable> extends TreeMap<K, V> implements NavigableMap<K, V>
{

  private final BiNavigateAbleMap<V, K> inverseMap;

  public BiNavigateAbleMap()
  {
    inverseMap = new BiNavigateAbleMap<>(this);
  }

  private BiNavigateAbleMap(BiNavigateAbleMap<V, K> pInverseMap)
  {
    inverseMap = pInverseMap;
  }

  @Override
  public V put(K pKey, V pValue)
  {
    V returnValue = get(pKey);
    if (pValue.equals(returnValue))
      return pValue;
    K inverseValue = inverseMap.get(pValue);
    if (inverseValue != null && inverseValue != pKey)
      throw new IllegalArgumentException("value " + pValue + " already bound to another pKey");
    else
    {
      super.put(pKey, pValue);
      if (returnValue != null)
      {
        // remove binding of old value in inverse
        inverseMap.remove(returnValue);
      }
      inverseMap.put(pValue, pKey);
    }
    return returnValue;
  }

  @Override
  public V remove(Object pKey)
  {
    V value = super.remove(pKey);
    if (value != null)
      inverseMap.remove(value);
    return value;
  }

  @Override
  public Map.Entry<K, V> pollFirstEntry()
  {
    Map.Entry<K, V> firstEntry = super.pollFirstEntry();
    inverseMap.remove(firstEntry.getKey());
    return firstEntry;
  }

  @Override
  public Map.Entry<K, V> pollLastEntry()
  {
    Map.Entry<K, V> lastEntry = super.pollLastEntry();
    inverseMap.remove(lastEntry.getKey());
    return lastEntry;
  }

  @Override
  public boolean replace(K pKey, V pOldValue, V pNewValue)
  {
    K removed = inverseMap.remove(pOldValue);
    inverseMap.put(pNewValue, pKey);
    return removed != null;
  }

  @Override
  public boolean remove(Object pKey, Object pValue)
  {
    return pValue.equals(remove(pKey));
  }

  @Override
  public V replace(K pKey, V pValue)
  {
    V oldValue = get(pKey);
    replace(pKey, oldValue, pValue);
    return oldValue;
  }

  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> pFunction)
  {
    throw new NotImplementedException("not yet implemented");
  }

  @Override
  public void clear()
  {
    if (!isEmpty())
    {
      super.clear();
      inverseMap.clear();
    }
  }

  @Override
  public void putAll(@NonNull Map<? extends K, ? extends V> pMap)
  {
    for (Map.Entry<? extends K, ? extends V> entry : pMap.entrySet())
    {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Nullable
  @Override
  public V putIfAbsent(K pKey, V pValue)
  {
    V currentValue = get(pKey);
    if (currentValue != null)
      return currentValue;
    else
      put(pKey, pValue);
    return null;
  }

  public BiNavigateAbleMap<V, K> getInverse()
  {
    return inverseMap;
  }
}
