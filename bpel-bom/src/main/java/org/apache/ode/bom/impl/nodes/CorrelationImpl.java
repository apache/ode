/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Correlation;

/**
 * Model of a BPEL correlation (on an invoke/receive/reply).
 */
public class CorrelationImpl extends BpelObjectImpl implements Correlation {

  private static final long serialVersionUID = -1L;

  private String _correlationSetName;
  private short _initiate;
  private short _pattern = Correlation.CORRPATTERN_IN;
  
  public CorrelationImpl() {
  }

  public short getInitiate() {
    return _initiate;
  }

  public void setInitiate(short initiate) {
    _initiate = initiate;
  }

  public short getPattern() {
    return _pattern;
  }

  public void setPattern(short pattern) {
    _pattern = pattern;
  }

  public String getCorrelationSet() {
    return _correlationSetName;
  }

  public void setCorrelationSet(String correlationSetName) {
    _correlationSetName = correlationSetName;
  }

}
