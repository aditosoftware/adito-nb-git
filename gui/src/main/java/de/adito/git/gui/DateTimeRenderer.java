package de.adito.git.gui;

import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.time.format.*;
import java.util.Locale;

/**
 * Renderer to provide Date -> String Conversions
 *
 * @author w.glanzer, 14.12.2018
 */
public class DateTimeRenderer
{

  private static final Locale _REAL_USER_LOCALE = System.getProperty("user.country") != null ?
      new Locale(System.getProperty("user.country")) :
      Locale.getDefault();
  private static final DateTimeFormatter _FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
      .withLocale(_REAL_USER_LOCALE)
      .withZone(ZoneId.systemDefault());

  private DateTimeRenderer()
  {
  }

  @Nullable
  public static String asString(@Nullable Instant pDate)
  {
    if (pDate == null)
      return null;

    return _FORMATTER.format(pDate);
  }

}
