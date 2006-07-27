/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

import org.apache.ode.sax.fsa.ParseError;

import org.xml.sax.InputSource;


/**
 * <p>
 * Factory for creating the various BPEL Object Model objects.  Use an instance of
 * {@link org.apache.ode.bpel.parser.BpelProcessBuilderFactory} to create an instance of
 * this interface.
 * </p>
 */
public interface BpelProcessBuilder {

  /**
   * The XML namespace of the schema for the specification as contributed to OASIS
   * in 2003.
   */
  public static final String BPEL4WS_NS =
    "http://schemas.xmlsoap.org/ws/2003/03/business-process/";
 
  /**
   * The XML namespace for schema of WS-BPEL 2.0, i.e., the first OASIS-sanctioned
   * version of the specification.
   */
  public static final String WSBPEL2_0_NS =
    "http://schemas.xmlsoap.org/ws/2004/03/business-process/";

  /**
   * Parse a BPEL process definition.
   * @param bpelSource input source
   * @param id source id 
   * @return BOM representation of BPEL process
   * @throws BpelParseException in case of parse error
   */
  org.apache.ode.bom.api.Process parse(InputSource bpelSource, String id) throws BpelParseException;

  /**
   * Get the errors from the last {@link #parse(org.xml.sax.InputSource,String)}
   * call.
   *
   * @return array of errors
   */
  ParseError[] getParseErrors();

}
