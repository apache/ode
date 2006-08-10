/*
 * File:      $Id: IdGen.java 843 2006-02-17 21:51:55Z mriou $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique keys for process instances and scopes.
 * 
 */
class IdGen {
  private static AtomicLong PROC_ID = new AtomicLong(0);
  private static AtomicLong SCOPE_ID = new AtomicLong(0);
  private static AtomicLong CSET_ID = new AtomicLong(0);

  public static Long newProcessId() {
    return PROC_ID.getAndIncrement();
  }

  public static Long newScopeId() {
    return SCOPE_ID.getAndIncrement();
  }

  public static Long newCorrelationSetId() {
    return CSET_ID.getAndIncrement();
  }

}
