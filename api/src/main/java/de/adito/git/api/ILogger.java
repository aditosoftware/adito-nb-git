package de.adito.git.api;

/**
 * Interface that defines which meethods a logger should have
 *
 * @author m.kaspera, 28.05.2019
 */
public interface ILogger
{

  /**
   * The different Log levels, from fine grained to course:
   * TRACE
   * DEBUG
   * INFO
   * WARN
   * ERROR
   */
  enum Level
  {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    /**
     * creates the matching logLevel from the passed string representation
     *
     * @param pLevel Level that was converted to a string
     * @return Level whose string representation matches the provided string, or ERROR as default
     */
    public static Level fromString(String pLevel)
    {
      switch (pLevel)
      {
        case "TRACE":
          return TRACE;
        case "DEBUG":
          return DEBUG;
        case "INFO":
          return INFO;
        case "WARN":
          return WARN;
        default:
          return ERROR;
      }
    }
  }

  /**
   * logs the message if the logLevel is higher or equal than the one set
   *
   * @param pMessage  message to write
   * @param pLogLevel how important the message is
   */
  void println(String pMessage, Level pLogLevel);

  /**
   * sets from which logLevel on messages should be logged (pLogLevel is inclusive)
   *
   * @param pLogLevel minimum logLevel
   */
  void setLogLevel(Level pLogLevel);

  /**
   * retrieves the currently used logLevel
   *
   * @return current logLevel
   */
  Level getLogLevel();

}
