/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.Property;
import com.fs.pxe.sfwk.deployment.som.impl.PortImpl;
import com.fs.pxe.sfwk.deployment.som.impl.PropertyImpl;
import com.fs.utils.NamespaceStack;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class PortContentHandler extends NestedContentHandler implements PropertyTarget {

  private int _depth;
  private PortsContentHandler _pch;
  private ContentHandler _target;
  private PortImpl _p;
  
  public PortContentHandler(PortsContentHandler pch) {
    _pch = pch;
    _depth = 0;
  }
  
  private Locator getLocator() {
    return _pch.getLocator();
  }
  
  public void addProperty(String name, String value) {
    Property p = new PropertyImpl();
    p.setName(name);
    p.setValue(value);
    _p.addProperty(p);
  }
  
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    ++_depth;
    if (_depth == 1) {
      _p = _pch.getServiceContentHandler().getService().newPort();
      _p.setLocator(getLocator());
      _pch.addPort(_p);
      if (atts.getValue("name") != null) {
        _p.setName(atts.getValue("name"));
      }
      if (atts.getValue("channel-ref") != null) {
        _p.setChannelRef(atts.getValue("channel-ref"));
      }
      if (atts.getValue("type") != null) {
        NamespaceStack nsc = _pch._sch.getSystemDescriptorContentHandler().getNamespaceStack();
        QName qn =nsc.dereferenceQName(atts.getValue("type"));
        _p.setType(qn);
      }
    } else if (_depth == 2) {
      _target = new PropertiesContentHandler(this);
    } else {
      _target.startElement(namespaceURI,localName,qName,atts);
    }
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    if (_depth > 1) {
      _target.endElement(namespaceURI,localName,qName);
    }
    --_depth;
  }
}
