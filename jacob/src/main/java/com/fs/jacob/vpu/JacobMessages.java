/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.jacob.vpu;

import com.fs.utils.msg.MessageBundle;

/**
 * Messages for the Jacob VPU.
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class JacobMessages extends MessageBundle {

  /**
   * Error indicating that client-code (i.e. not the VPU kernel) threw an
   * unexpected exception.
   * 
   * @param methodName
   *          name of offending method
   * @param className
   *          name of offending class
   * 
   * Method "{0}" in class "{1}" threw an unexpected exception.
   */
  public String msgClientMethodException(String methodName, String className) {
    return this.format("Method \"{0}\" in class \"{1}\" threw an unexpected exception.",
        methodName, className);
  }

  // TODO
  public String msgContDeHydrationErr(String channel, String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * Error indicating that a re-hydration of a saved continuation object could
   * not be completed.
   * 
   * @param channel
   *          channel with the dangling continuation
   * @param mlClassName
   *          name of de-hydrated {@link com.fs.jacob.ML} object
   * 
   */
  public String msgContHydrationErr(String channel, String mlClassName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal error indicating that a required client method was not accessible
   * due to security protections errors. This may be caused by a change to the
   * client class definitions.
   * 
   * Method "{0}" in class "{1}" is not accessible.
   */
  public String msgMethodNotAccessible(String methodName, String className) {
    return this.format("Method \"{0}\" in class \"{1}\" is not accessible.", methodName,
        className);
  }

}
