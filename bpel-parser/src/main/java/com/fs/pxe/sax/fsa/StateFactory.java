/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa;

import com.fs.sax.evt.StartElement;


/**
 * Responsible for creating instances of a <code>State</code> with specific context.
 */
public interface StateFactory {
  
  /**
   * Create an instance of a {@link State} with the specified context.
   * @param se the <code>startElement</code> SAX Event that caused the creation.
   * @param pc the {@link ParseContext} for the current parse.
   * @return the configured instance.
   * @throws ParseException if the configuration is invalid.
   */
  public State newInstance(StartElement se, ParseContext pc) throws ParseException;
}
