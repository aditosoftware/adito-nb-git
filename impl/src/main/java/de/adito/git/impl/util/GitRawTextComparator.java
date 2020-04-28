package de.adito.git.impl.util;

import org.eclipse.jgit.diff.RawTextComparator;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In addition to the {@linkplain RawTextComparator} in this class a display-Value can be stored.
 *
 * @author s.seemann, 23.04.2020
 */
public class GitRawTextComparator
{
  public static GitRawTextComparator CURRENT;
  public static final List<GitRawTextComparator> INSTANCES;

  private final String displayValue;
  private final RawTextComparator value;

  static
  {
    INSTANCES = new ArrayList<>(5);
    INSTANCES.add(new GitRawTextComparator("No special treatment", RawTextComparator.DEFAULT));
    INSTANCES.add(new GitRawTextComparator("Ignore all whitespaces", RawTextComparator.WS_IGNORE_ALL));
    INSTANCES.add(new GitRawTextComparator("Ignore whitespaces between characters", RawTextComparator.WS_IGNORE_CHANGE));
    INSTANCES.add(new GitRawTextComparator("Ignore leading whitespaces", RawTextComparator.WS_IGNORE_LEADING));
    INSTANCES.add(new GitRawTextComparator("Ignore trailing whitespaces", RawTextComparator.WS_IGNORE_TRAILING));

    if (CURRENT == null)
      CURRENT = INSTANCES.get(0);
  }

  private GitRawTextComparator(String pDisplayValue, RawTextComparator pValue)
  {
    displayValue = pDisplayValue;
    value = pValue;
  }

  public RawTextComparator getValue()
  {
    return value;
  }

  @Override
  public String toString()
  {
    return displayValue;
  }

  /**
   * Parses the display value into a GitRawTextComparator. If there isn't one, null is returned.
   *
   * @param pDisplayValue the display value
   * @return null or the GitRawTextComparator found
   */
  @Nullable
  public static GitRawTextComparator parse(@Nullable String pDisplayValue)
  {
    List<GitRawTextComparator> results = INSTANCES.stream()
        .filter(pGitRawTextComparator -> pGitRawTextComparator.displayValue.equals(pDisplayValue))
        .collect(Collectors.toList());
    if (results.size() == 1)
      return results.get(0);

    return null;
  }

  /**
   * Sets the current active RawTextComparator.The Selection can be changed in the GitConfigDialog. If nothing is set, the default is returned.
   *
   * @param pComparator The display value of the current active RawTextComparator
   */
  public static void setCurrent(String pComparator)
  {
    GitRawTextComparator setComparator = parse(pComparator);

    if (setComparator == null)
      CURRENT = INSTANCES.get(0);
    else
      CURRENT = setComparator;
  }
}