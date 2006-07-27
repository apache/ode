/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.dao;

public class ScopeStateEnum {
  /** DOCUMENTME */
  public static final ScopeStateEnum ACTIVE = new ScopeStateEnum("ACTIVE");

  /** DOCUMENTME */
  public static final ScopeStateEnum FAULTED = new ScopeStateEnum("FAULTED");

  /** DOCUMENTME */
  public static final ScopeStateEnum FAULTHANDLER = new ScopeStateEnum("FAULTHANDLER");

  /** DOCUMENTME */
  public static final ScopeStateEnum COMPLETED = new ScopeStateEnum("COMPLETED");

  /** DOCUMENTME */
  public static final ScopeStateEnum COMPENSATING = new ScopeStateEnum("COMPENSATING");

  /** DOCUMENTME */
  public static final ScopeStateEnum COMPENSATED = new ScopeStateEnum("COMPENSATED");
  private final String myName; // for debug only

  public ScopeStateEnum(String name) {
    myName = name;
  }

  public boolean equals(Object o) {
    return ((ScopeStateEnum)o).myName.equals(myName);
  }

  public int hashCode() {
    return myName.hashCode();
  }

  public String toString() {
    return myName;
  }
}
