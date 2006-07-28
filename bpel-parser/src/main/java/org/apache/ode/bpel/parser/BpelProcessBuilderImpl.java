/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.parser;

import org.apache.ode.sax.fsa.ParseError;

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

  public org.apache.ode.bom.api.Process parse(InputSource bpelSource, String id)
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
