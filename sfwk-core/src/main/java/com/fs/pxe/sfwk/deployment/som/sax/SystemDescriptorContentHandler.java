/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.impl.Constants;
import com.fs.pxe.sfwk.deployment.som.impl.SystemDescriptorImpl;
import com.fs.utils.NamespaceStack;
import com.fs.utils.msg.MessageBundle;

import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.*;

class SystemDescriptorContentHandler implements ContentHandler {

  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);
  
  private Locator _loc;
  private SystemDescriptorImpl _desc;
  private int _depth;
  private ContentHandler _target;
  private ErrorHandler _eh;
  
  private NamespaceStack _nss;
  
  public SystemDescriptorContentHandler(ErrorHandler eh) {
    _eh = eh;
  }
  
  public SystemDescriptorImpl getDescriptor() {
    return _desc;
  }
  
  NamespaceStack getNamespaceStack() {
    return _nss;
  }
  
  Locator getLocator() {
    return _loc;
  }
  
  ErrorHandler getErrorHandler() {
    return _eh;
  }
  
  public void setDocumentLocator(Locator locator) {
    _loc = locator;
  }

  public void startDocument() throws SAXException {
    _desc = new SystemDescriptorImpl();
    _depth = 0;
    _nss = new NamespaceStack();
  }

  public void endDocument() throws SAXException {
    // This method intentionally left blank.
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    _nss.declarePrefix(prefix,uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    // ignore; handled by popping contexts.
  }

  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    ++_depth;
    if (_depth == 1) {
      // basic sanity check to avoid issues.
      if (!localName.equals("system-descriptor")) {
        throw new SAXParseException(__msgs.incorrectRootElement("system-descriptor",localName),_loc);
      } else if (!namespaceURI.equals(Constants.DESCRIPTOR_URI)) {
        throw new SAXParseException(__msgs.incorrectNamespace(Constants.DESCRIPTOR_URI,
            namespaceURI),_loc);
      }
      if (atts.getValue("name") != null) {
        _desc.setName(atts.getValue("name"));
      }
      if (atts.getValue("wsdlUri") != null) {
        URI u = null;
        try {
          u = new URI(atts.getValue("wsdlUri"));
        } catch (URISyntaxException use) {
          throw new SAXParseException(use.getMessage(),_loc,use);
        }
        _desc.setWsdlUri(u);
      }
      if (atts.getValue("uuid") != null) {
        _desc.setUuid(atts.getValue("uuid"));
      }
      _desc.setLocator(_loc);
    } else if (_depth == 2) {
      if (localName.equals("services")) {
        _target = new ServicesContentHandler(this);
      } else if (localName.equals("channels")) {
        _target = new ChannelsContentHandler(this);
      }
    } else {
      _target.startElement(namespaceURI, localName,
          qName,atts);
    }
    _nss.pushNewContext();
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    if (_depth > 2) {
      _target.endElement(namespaceURI,localName,qName);
    }
    --_depth;
    _nss.pop();
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    // This method intentionally left blank, since we're not using content.
  }

  public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
    // This method intentionally left blank, since we're not using content.
  }


  public void processingInstruction(String target, String data)
      throws SAXException {
    // This method intentionally left blank, since we're not using PIs.
  }

  public void skippedEntity(String name) throws SAXException {
    // This method intentionally left blank, since we're not using PIs.
  }
}
