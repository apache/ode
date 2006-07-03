/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.impl.PropertyImpl;
import com.fs.pxe.sfwk.deployment.som.impl.ServiceImpl;

import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.*;

class ServiceContentHandler extends NestedContentHandler implements PropertyTarget
{
  private ServicesContentHandler _sch;
  private int _depth;
  private ServiceImpl _s;
  private ContentHandler _target;
  
  public ServiceContentHandler(ServicesContentHandler sch) {
    _sch = sch;
    _depth = 0;
  }
  
  public SystemDescriptorContentHandler getSystemDescriptorContentHandler() {
    return _sch.getSystemDescriptorContentHandler();
  }
  
  public ServiceImpl getService() {
    return _s;
  }

  Locator getLocator() {
    return _sch.getLocator();
  }
  
  public void addProperty(String n, String v) {
    com.fs.pxe.sfwk.deployment.som.Property p = new PropertyImpl();
    p.setName(n);
    p.setValue(v);
    _s.addProperty(p);
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    ++_depth;
    if (_depth == 1) {
      _s = _sch.getSystemDescriptorContentHandler().getDescriptor().newService();
      _s.setLocator(getLocator());
      if (atts.getValue("name") != null) {
        _s.setName(atts.getValue("name"));
      }
      if (atts.getValue("uuid") != null) {
        _s.setUuid(atts.getValue("uuid"));
      }
      if (atts.getValue("provider") != null) {
        URI u = null;
        try {
          u = new URI(atts.getValue("provider"));
        } catch (URISyntaxException use) {
          // TODO: Put in real error message.
          throw new SAXParseException("",_sch.getSystemDescriptorContentHandler().getLocator(),use);
        }
        _s.setProviderUri(u);
      }
    } else if (_depth == 2) {
      if (localName.equals("properties")) {
        _target = new PropertiesContentHandler(this);
      } else if (localName.equals("imports")) {
        _target = new ImportsContentHandler(this);
      } else if (localName.equals("exports")) {
        _target = new ExportsContentHandler(this);
      }
    } else {
      _target.startElement(namespaceURI,localName,qName,atts);
    }
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    if (_depth > 2) {
      _target.endElement(namespaceURI,localName,qName);
    }
    --_depth;
  }
}
