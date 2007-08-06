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
package org.apache.ode.utils.xml.capture;

import java.net.URI;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Base class for schema-specifc dependency trackers.
 */
public abstract class Tracker implements ContentHandler {
  private static final Log __log = LogFactory.getLog(Tracker.class);

  private Set<URI> references_ ;
  private URI base_;

  public void characters(char ch[], int start, int length) throws SAXException {
  }

  public void endDocument() throws SAXException {
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  public void startDocument() throws SAXException {
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
  }

  protected void addReference(URI ref) {
    if (references_.contains(ref)) {
      return;
    }

    if (__log.isDebugEnabled()) {
      __log.debug("added reference " + ref + " (base is "  + base_ + ")");
    }

    references_.add(ref);
  }

  protected void addReference(String schemaLoc) {
    if (schemaLoc == null) {
      return;
    }

    addReference(base_.resolve(schemaLoc));
  }

  protected URI getBase() {
    return base_;
  }

  public Set<URI> getReferences() {
    return references_;
  }

  void init(URI base,Set<URI>references) {
    base_ = base;
    references_ = references;
  }

}
