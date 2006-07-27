/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

/**
 * Factory for creating {@link BpelProcessBuilder} objects.
 */
public abstract class BpelProcessBuilderFactory {
  
  /**
   * Get a {@link BpelProcessBuilderFactory} object.
   * @return a usable {@link BpelProcessBuilderFactory} object
   * @throws BpelProcessBuilderFactoryException
   */
  public static BpelProcessBuilderFactory newProcessBuilderFactory() throws BpelProcessBuilderFactoryException {
    return new org.apache.ode.bpel.parser.BpelProcessBuilderFactoryImpl();
  }

  /**
   * Enable or disable strict parsing mode. In strict parsing mode the BPEL schema is
   * enforced, any parse problems will resultin a {@link BpelParseException}. In
   * non-strict mode, schema conformance is relaxed, and a best-effort will be made
   * to load the BPEL process description.
   *
   * @param strict if <code>true</code> strict parsing will be used
   */
  abstract public void setStrict(boolean strict);

  /**
   * Get the strict parsing flag.
   *
   * @return value of strict parse flag
   * @see #setStrict(boolean)
   */
  abstract public boolean getStrict();

  /**
   * Create a new {@link BpelProcessBuilder}.
   *
   * @return
   */
  abstract public BpelProcessBuilder newBpelProcessBuilder();
}
