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

  private static final DateTimeFormatter _FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
      .withLocale(Locale.ENGLISH)
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
