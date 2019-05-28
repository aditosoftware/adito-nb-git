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
    ERROR
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

}
