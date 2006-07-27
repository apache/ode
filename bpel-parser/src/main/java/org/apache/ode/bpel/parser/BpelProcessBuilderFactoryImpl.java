/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

public class BpelProcessBuilderFactoryImpl extends BpelProcessBuilderFactory {
  private boolean _strict;

  public BpelProcessBuilder newBpelProcessBuilder() {
    return new BpelProcessBuilderImpl();
  }

  public void setStrict(boolean strict) {
    _strict = strict;
  }

  public boolean getStrict() {
    return _strict;
  }
}
