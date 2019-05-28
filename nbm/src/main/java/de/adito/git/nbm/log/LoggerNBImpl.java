package de.adito.git.nbm.log;

import de.adito.git.api.ILogger;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

import java.util.*;

/**
 * @author m.kaspera, 28.05.2019
 */
public class LoggerNBImpl implements ILogger
{

  private List<Level> allLogLevels = List.of(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);
  private Set<Level> activeLogLevels = new HashSet<>();
  private OutputWriter logger = null;

  public LoggerNBImpl()
  {
    setLogLevel(Level.INFO);
  }

  @Override
  public void println(String pMessage, Level pLogLevel)
  {
    if (logger == null)
      logger = IOProvider.getDefault().getIO("IDE Log", false).getOut();
    if (activeLogLevels.contains(pLogLevel))
      logger.println(String.format("[ %s ] %s | %s: %s", pLogLevel, new Date(System.currentTimeMillis()), "Git Plugin", pMessage));
  }

  @Override
  public void setLogLevel(Level pLogLevel)
  {
    activeLogLevels.clear();
    activeLogLevels.addAll(allLogLevels.subList(allLogLevels.indexOf(pLogLevel), allLogLevels.size()));
  }
}
