/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

import org.apache.ode.sax.fsa.ParseContext;

import org.xml.sax.ErrorHandler;

/**
 * <p>
 * Simple interface for handling either SAX-related (i.e., validation and
 * well-formedness) errors and BPEL-specific errors at parse time.  The most
 * common use case for the interface will be a collector that holds errors until
 * later.
 * </p>
 */
public interface BpelParseErrorHandler extends ErrorHandler, ParseContext {
    
}
