/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import com.fs.pxe.sfwk.deployment.som.Port;
import com.fs.pxe.sfwk.deployment.som.Property;
import com.fs.pxe.sfwk.deployment.som.Service;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Default implementation of the <code>ServicePort</code> interface.
 */
public class PortImpl extends ALocated implements Port, Constants {

  private QName _type;
  private Property[] _properties;
  private String _name;
  private String _channelRef;
  private ServiceImpl _s;
  
  /**
   * Create a new instance with a back-reference to the <code>ServiceImpl</code>
   * instance that owns it.
   * @param s the <code>ServiceImpl</code> instance that owns this <code>ServicePort</code>
   */
  PortImpl(ServiceImpl s) {
   _s = s;
   _properties = new Property[0];
  }
  
  /**
   * @see Port#setName(String)
   */
  public void setName(String name) {
    _name = name;
  }
  
  /**
   * @see Port#getName()
   */
  public String getName() {
    return _name;
  }
  
  /**
   * @see Port#setType(QName)
   */
  public void setType(QName type) {
    _type = type;
  }

  /**
   * @see Port#getType()
   */
  public QName getType() {
    return _type;
  }

  public String getChannelRef() {
    return _channelRef;
  }

  public void setChannelRef(String name) {
    _channelRef = name;
  }
  
  /**
   * @see Port#addProperty(Property)
   */
  public void addProperty(Property p) {
    Property[] nu = new Property[_properties.length + 1];
    System.arraycopy(_properties,0,nu,0,_properties.length);
    nu[_properties.length] = p;
    _properties = nu;    
  }

  /**
   * @return the <code>Service</code> instance that owns this <code>ServicePort</code>
   */
  public Service getService() {
    return _s;
  }
  
  /**
   * @see Port#getProperties()
   */
  public Property[] getProperties() {
    return _properties;
  }
  
  /**
   * Validate this object and its referenced objects.
   * @param eh an <code>ErrorHandler</code> to report violations
   * @throws SAXException if the <code>ErrorHandler</code> throws one.
   */
  public void validate(ErrorHandler eh) throws SAXException {
    
  }
  
  /**
   * @see com.fs.pxe.sfwk.deployment.som.Marshallable#toSaxEvents(ContentHandler)
   */
  public void toSaxEvents(ContentHandler ch) throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    if (_name != null) {
      atts.addAttribute("","name","name","PCDATA",_name);
    }
    String prefix = (_type.getPrefix() == null || _type.getPrefix().length() == 0)
      ? "ns":_type.getPrefix();
    String uri = _type.getNamespaceURI();
    if (uri == null) {
      uri="";
    }
    if (_type != null) {
      atts.addAttribute("","type","type","PCDATA",prefix + ':' +  _type.getLocalPart());
      ch.startPrefixMapping(prefix,uri);      
    }
    if (_channelRef != null) {
      atts.addAttribute("","channel-ref","channel-ref","PCDATA",_channelRef);
    }
    ch.startElement(DESCRIPTOR_URI,"port","port",atts);
    if (_properties.length > 0) {
      ch.startElement(DESCRIPTOR_URI,"properties","properties",new AttributesImpl());
      for (int i=0; i < _properties.length; ++i) {
        ((PropertyImpl)_properties[i]).toSaxEvents(ch);
      }
      ch.endElement(DESCRIPTOR_URI,"properties","properties");
    }
    ch.endElement(DESCRIPTOR_URI,"port","port");
    if (_type != null) {
      ch.endPrefixMapping(prefix);
    }
  }
}
