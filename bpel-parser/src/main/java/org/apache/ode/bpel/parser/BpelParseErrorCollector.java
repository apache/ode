/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.sax.fsa.ParseError;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.evt.SaxEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class BpelParseErrorCollector implements BpelParseErrorHandler {

  private ArrayList<ParseError> _errors;
  
  private short _abort = ParseError.ERROR;
  private String _baseUri;

  public BpelParseErrorCollector(String id) {
    _errors = new ArrayList<ParseError>();
    _baseUri = id;
  }
  
  public void setAbortThreshold(short s) {
    _abort = s;
  }
  
  public void parseError(ParseError bpe) throws ParseException {
    _errors.add(bpe);
    if (bpe.getSeverity() >= _abort) {
      throw new ParseException(bpe);
    }
  }
  
  public String getBaseUri() {
    return _baseUri;
  }

  private void parseError(short severity, String uri, int line, int col, String key, String msg) throws ParseException {
    parseError(new ParseErrorImpl(severity,uri,line,col,key,msg));
  }
  
  public void parseError(short severity, BpelObject bo, String key, String msg) throws ParseException {
    parseError(severity,_baseUri,bo.getLineNo(),-1,key,msg);   
  }

  public void parseError(short severity, SaxEvent se, String key, String msg) throws ParseException {
    if (se.getLocation() != null) {
      Locator l = se.getLocation();
      parseError(severity,l.getSystemId(),l.getLineNumber(),l.getColumnNumber(),key, "INTERNAL PARSE ERROR: " + msg);
    } else {
      parseError(severity,_baseUri,-1,-1,key,"INTERNAL PARSE ERROR: " + msg);
    }
  }

  public void parseError(short severity, String key, String msg) throws ParseException {
    parseError(severity,_baseUri,-1,-1,key,msg);
  }

  public void setBaseUri(String uri) {
    _baseUri = uri;
 }

  public List<ParseError> getErrors() {
    return Collections.unmodifiableList(_errors);
  }
  
  public void warning(SAXParseException exception) throws SAXException {
    parseError(new ParseErrorImpl(ParseError.WARNING, _baseUri, exception.getLineNumber(), exception.getColumnNumber(), "SAX_WARNING", exception.getLocalizedMessage()));
  }

  public void error(SAXParseException exception) throws SAXException {
    parseError(new ParseErrorImpl(ParseError.ERROR,_baseUri, exception.getLineNumber(), exception.getColumnNumber(), "SAX_ERROR",exception.getLocalizedMessage()));
  }

  public void fatalError(SAXParseException exception) throws SAXException {
    parseError(new ParseErrorImpl(ParseError.FATAL, _baseUri, exception.getLineNumber(), exception.getColumnNumber(), "SAX_FATAL",exception.getLocalizedMessage()));
  }
}
