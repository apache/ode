/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import com.fs.pxe.sfwk.deployment.som.Property;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Default implementation of the <code>Property</code> interface.
 */
public class PropertyImpl extends ALocated implements Property, Constants {

  private String _name;
  private String _value;
  
  /**
   * Create a new instance.
   */
  public PropertyImpl() {}
  
  /**
   * @see Property#setName(String)
   */
  public void setName(String s) {
    _name = s;
  }

  /**
   * @see Property#getName()
   */
  public String getName() {
    return _name;
  }

  /**
   * @see Property#setValue(String)
   */
  public void setValue(String s) {
    _value = s;
  }

  /**
   * @see Property#getValue()
   */
  public String getValue() {
    return _value;
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
    if (_value != null) {
      atts.addAttribute("","value","value","PCDATA",_value);
    }
    ch.startElement(DESCRIPTOR_URI,"property","property",atts);
    ch.endElement(DESCRIPTOR_URI,"property","property");
  }
}
