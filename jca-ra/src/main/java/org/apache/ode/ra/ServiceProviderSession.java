/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

/**
 * Base interface accessible to all sessions returned from
 * {@link OdeConnection#createServiceProviderSession(String, Class)}.
 */
public interface ServiceProviderSession {

  /**
   * Close the session.
   */
  void close();
}
