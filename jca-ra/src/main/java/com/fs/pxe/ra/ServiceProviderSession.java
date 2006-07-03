/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

/**
 * Base interface accessible to all sessions returned from
 * {@link PxeConnection#createServiceProviderSession(String, Class)}.
 */
public interface ServiceProviderSession {

  /**
   * Close the session.
   */
  void close();
}
