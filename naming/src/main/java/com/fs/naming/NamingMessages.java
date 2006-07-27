/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.naming;

import org.apache.ode.utils.msg.MessageBundle;

import javax.naming.Name;

/**
 * Internationalization for the <code>com.fs.naming</code> package.
 */
public class NamingMessages extends MessageBundle {

  /**
   * Communication error encountered in JNDI: {0}
   */
  public String msgCommunicationError(String message) {
    return this.format("Communication error encountered in JNDI: {0}", message);
  }

  /**
   * Unable to connect to JNDI provider at URL "{0}".
   */
  public String msgConnectErr(String urlS) {
    return this.format("Unable to connect to JNDI provider at URL \"{0}\".", urlS);
  }

  /**
   * The context "{0}" is not empty and cannot be removed.
   */
  public String msgContextNotEmpty(Name name) {
    return this.format("The context \"{0}\" is not empty and cannot be removed.", name);
  }

  /**
   * Unable to dereference object named "{0}".
   */
  public String msgDeRefError(Name name) {
    return this.format("Unable to dereference object named \"{0}\".", name);
  }

  /**
   * The name "{0}" is invalid.
   */
  public String msgInvalidName(Name name) {
    return this.format("The name \"{0}\" is invalid.", name);
  }

  /**
   * The JNDI provider URL "{0}" is not valid.
   */
  public String msgInvalidProviderURL(String providerURL) {
    return this.format("The JNDI provider URL \"{0}\" is not valid.", providerURL);
  }

  /**
   * The name "{0}" is already bound in context "{1}".
   */
  public String msgNameAlreadyBound(Name name) {
    return this.format("The name \"{0}\" is already bound in context \"{1}\".", name);
  }

  /**
   * No object named "{0}" found.
   */
  public String msgNameNotFound(Name name) {
    return this.format("No object named \"{0}\" found.", name);
  }

  /**
   * No object named "{0}" found; found up to "{1}".
   */
  public String msgNameNotFound(String simple, Name prefix) {
    return this.format("Noobject named \"{0}\" found; found up to \"{1}\".", simple, prefix);
  }

  /**
   * The value "{1}" is not bindable at name "{0}".
   */
  public String msgNotBindable(Name name, String name1) {
    return this.format("The value \"{1}\" is not bindable at name \"{0}\".", name);
  }

  /**
   * The object "{0}" is not a context.
   */
  public String msgNotContext(Name name) {
    return this.format("The object \"{0}\" is not a context.", name);
  }

  /**
   * "{0}" and "{1}" are not the same context.
   */
  public String msgNotSameContext(Name oldName, Name newName) {
    return this.format("\"{0}\" and \"{1}\" are not the same context.", oldName, newName);
  }

  /**
   * The provider URL is not set.
   */
  public String msgProviderUrlNotSet() {
    return this.format("The provider URL is not set.");
  }

  /**
   * The context "{0}" is read-only.
   */
  public String msgReadOnly(Name name) {
    return this.format("The context \"{0}\" is read-only.", name);
  }

}
