/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime.channels;

/**
 * Channel used for parent-to-child scope communication. 
 * @jacob.kind
 */
public interface Termination  {

  /**
   * Stop processing immediately.
   */
  void terminate();

}
