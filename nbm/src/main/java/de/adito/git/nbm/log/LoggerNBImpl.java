package de.adito.git.nbm.log;

import com.google.inject.Inject;
import de.adito.git.api.ILogger;
import de.adito.git.api.prefs.IPrefStore;
import de.adito.git.gui.Constants;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

import java.util.*;

/**
 * @author m.kaspera, 28.05.2019
 */
public class LoggerNBImpl implements ILogger
{

  private static final List<Level> ALL_LOG_LEVELS = List.of(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);
  private static final Set<Level> ACTIVE_LOG_LEVELS = new HashSet<>();
  private final IPrefStore prefStore;
  private OutputWriter logger = null;

  @Inject
  public LoggerNBImpl(IPrefStore pPrefStore)
  {
    prefStore = pPrefStore;
    if (ACTIVE_LOG_LEVELS.isEmpty())
    {
      String storedLogLevel = prefStore.get(Constants.LOG_LEVEL_SETTINGS_KEY);
      if (storedLogLevel == null)
        setLogLevel(Level.INFO);
      else
      {
        setLogLevel(Level.fromString(storedLogLevel));
      }
    }
  }

  @Override
  public void println(String pMessage, Level pLogLevel)
  {
    if (logger == null)
      logger = IOProvider.getDefault().getIO("IDE Log", false).getOut();
    if (ACTIVE_LOG_LEVELS.contains(pLogLevel))
      logger.println(String.format("[ %s ] %s | %s: %s", pLogLevel, new Date(System.currentTimeMillis()), "Git Plugin", pMessage));
  }

  @Override
  public void setLogLevel(Level pLogLevel)
  {
    prefStore.put(Constants.LOG_LEVEL_SETTINGS_KEY, pLogLevel.toString());
    synchronized (ACTIVE_LOG_LEVELS)
    {
      ACTIVE_LOG_LEVELS.clear();
      ACTIVE_LOG_LEVELS.addAll(ALL_LOG_LEVELS.subList(ALL_LOG_LEVELS.indexOf(pLogLevel), ALL_LOG_LEVELS.size()));
    }
  }

  @Override
  public Level getLogLevel()
  {
    Level minimumLevel = null;
    synchronized (ACTIVE_LOG_LEVELS)
    {
      for (Level level : ACTIVE_LOG_LEVELS)
      {
        if (minimumLevel == null || minimumLevel.ordinal() > level.ordinal())
        {
          minimumLevel = level;
        }
      }
    }
    return minimumLevel;
  }
}
