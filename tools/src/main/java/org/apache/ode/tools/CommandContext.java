/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools;


/**
 * <p>
 * A context for the execution of a {@link Command} that supplies uniform
 * logging and output functionality.
 * </p>
 * <p>
 * The logging infrastructure is purposefully simpler than, e.g., Log4J, in the
 * interest of supporting Apache Ant and other environments.
 * </p>
 */
public interface CommandContext {

  /**
   * Send a line of output to the equivalent of standard out.
   * @param s the content to send.
   */
  public void outln(String s);

  /**
   * Send output to the equivalent of standard out.  Note that this is not required
   * to be pretty if it's not on a console, i.e., multiple invocations might not
   * appear on a single line.
   * @param s the content to send.
   */
  public void out(String s);
  
  /**
   * Send a line of output to the equivalent of standard error.
   * @param s the content to send.
   */
  public void errln(String s);
  
  /**
   * Log an error.
   * @param s a descriptive message.
   */
  public void error(String s);

  /**
   * Log an error that resulted from a {@link Throwable}.
   * @param s a descriptive message.
   * @param t the cause.
   */
  public void error(String s, Throwable t);

  /**
   * Log a warning that resulted from a {@link Throwable}
   * @param s
   * @param t
   */
  public void warn(String s, Throwable t);

  /**
   * Log a warning.
   * @param s a descriptive message.
   */
  public void warn(String s);

  /**
   * Log an informative message.
   * @param s a descriptive message.
   */
  public void info(String s);

  /**
   * Log an informative message that resulted from a {@link Throwable}.
   * @param s a descriptive message.
   * @param t the cause.
   */
  public void info(String s, Throwable t);

  /**
   * Log a debug-level message that resulted from a {@link Throwable}.
   * @param s a descriptive message.
   * @param t the cause.
   */

  public void debug(String s, Throwable t);

  /**
   * Log an debug-level message.
   * @param s a descriptive message.
   */
  public void debug(String s);
  
}
