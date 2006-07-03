/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.parser;

import com.fs.pxe.sax.fsa.ParseError;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class BpelProcessBuilderImpl implements BpelProcessBuilder {
  private BpelParser _lastParser;
  private BpelParseErrorCollector _bpec;
  
  BpelProcessBuilderImpl() {
    _lastParser = new BpelParser();
    _bpec = new BpelParseErrorCollector("<<unknown>>");
  }

  public com.fs.pxe.bom.api.Process parse(InputSource bpelSource, String id)
    throws BpelParseException
  {
    _bpec = new BpelParseErrorCollector(id);
    _lastParser.setBpelParseErrorHandler(_bpec);
    try {
      return _lastParser.parse(bpelSource);
    } catch (SAXException se) {
      // Force these to go into the parse-error array.
      try {
        _bpec.parseError(ParseError.FATAL,"SAX", se.getMessage());
      } catch (Exception ex) {
        // ignore.
      }
      throw new BpelParseException(se.getMessage(),se);
    } catch (IOException ioe) {
      // Force these to go into the parse-error array.
      try {
        _bpec.parseError(ParseError.FATAL,"IO", ioe.getMessage());
      } catch (Exception ex) {
        // ignore.
      }
      throw new BpelParseException(ioe.getMessage(),ioe);
    }
  }

  public ParseError[] getParseErrors() {
    return _bpec.getErrors().toArray(new ParseError[] {});
  }

}
