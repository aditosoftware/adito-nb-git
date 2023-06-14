package de.adito.git.api.data;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enum of the possible options for the autoResolve mechanic
 *
 * @author m.kaspera, 23.06.2020
 */
public enum EAutoResolveOptions
{

  /**
   * Indicates that the Auto-Resolve should always be performed
   */
  ALWAYS("Always"),
  /**
   * Indicates that the Auto-Resolve should never be performed
   */
  NEVER("Never"),
  /**
   * Indicates that the user should be asked if an Auto-Resolve should be performed
   */
  ASK("Ask");

  private final String stringValue;

  EAutoResolveOptions(String pStringValue)
  {
    stringValue = pStringValue;
  }

  @Override
  public String toString()
  {
    return stringValue;
  }

  /**
   * Reads the value of EAutoResolveOptions from a String that should contain a boolean.
   * The value is ASK if the value is null, ALWAYS if the value is true and NEVER otherwise
   *
   * @param pBooleanValue Boolean encoded as String
   * @return EAutoResolveOptions determined by the algorithm sketched above
   */
  @NonNull
  public static EAutoResolveOptions getFromBoolean(@Nullable String pBooleanValue)
  {
    if (pBooleanValue == null)
      return ASK;
    else if (Boolean.parseBoolean(pBooleanValue))
      return ALWAYS;
    return NEVER;
  }

  /**
   * Reads the value of an EAutoResolveOptions from the given string.
   * The given value is compared to the string representations of the options, default value if none matches is NEVER
   *
   * @param pStringValue EAutoResolveOption encoded as string
   * @return EAutoResolveOption decoded from the string
   */
  @NonNull
  public static EAutoResolveOptions getFromStringValue(@Nullable String pStringValue)
  {
    if (pStringValue == null || ASK.stringValue.equals(pStringValue))
      return ASK;
    else if (ALWAYS.stringValue.equals(pStringValue))
      return ALWAYS;
    else return NEVER;

  }
}
