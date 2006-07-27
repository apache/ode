/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Copy;
import org.apache.ode.bom.api.From;
import org.apache.ode.bom.api.To;
import org.apache.ode.utils.NSContext;

/**
 * Assignmenet copy entry, i.e. what the assignment consits of.
 */
public class CopyImpl extends BpelObjectImpl implements Copy {
  private static final long serialVersionUID = -1L;
  private To _to;
  private From _from;
  private boolean keepSrcElement = false;

  public CopyImpl(NSContext ns) {
    super(ns);
  }

  public To getTo() {
    return _to;
  }

  public void setTo(To to) {
    _to = to;
  }

  public From getFrom() {
    return _from;
  }

  public void setFrom(From from) {
    _from = from;
  }

  public boolean isKeepSrcElement() {
    return keepSrcElement;
  }

  public void setKeepSrcElement(boolean keepSrcElement) {
    this.keepSrcElement = keepSrcElement;
  }
}
