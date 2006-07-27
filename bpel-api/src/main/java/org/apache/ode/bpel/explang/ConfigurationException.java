/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.explang;

/**
 * Exception indicating thrown from the
 * {@link ExpressionLanguageRuntime#initialize(org.apache.ode.bpel.o.OExpressionLanguage)}
 * method indicating that the expression language processor could not be configured.
 */
public class ConfigurationException extends Exception {

  public ConfigurationException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
