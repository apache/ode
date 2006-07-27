/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import org.apache.ode.utils.ObjectPrinter;

import java.util.HashSet;
import java.util.Set;


/**
 * Compiled represnetation of a BPEL activity.
 */
public abstract class OActivity extends OAgent {
  static final long serialVersionUID = -1L  ;
  
  public OExpression joinCondition;
  public final Set<OLink>sourceLinks = new HashSet<OLink>();
  public final Set<OLink>targetLinks = new HashSet<OLink>();
  public String name;

  public String getType() {
    return ObjectPrinter.getShortClassName(getClass());
  }

  public OActivity(OProcess owner) {
    super(owner);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(super.toString());
    if (name != null) {
      buf.append('-');
      buf.append(name);
    }

    return buf.toString();
  }

}
