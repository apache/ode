/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import com.fs.pxe.sfwk.deployment.som.Channel;
import com.fs.pxe.sfwk.deployment.som.Port;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Default implementation of the <code>Channel</code> interface.
 */
public class ChannelImpl extends ALocated implements Channel, Constants {
  
  private String _name;
  private String _uuid;
  private SystemDescriptorImpl _s;
  
  /**
   * Create a new instance with a back-reference to the <code>SystemDescriptorImpl</code>
   * that owns it.
   * @param s the <code>SystemDescriptorImpl</code> that owns this <code>ChannelImpl</code>
   */
  ChannelImpl(SystemDescriptorImpl s) {
    _s = s;
  }


  /**
   * @see Channel#getName()
   */
  public String getName() {
    return _name;
  }

  /**
   * @see Channel#getUuid()
   */
  public String getUuid() {
    return _uuid;
  }
  
  /**
   * @see Channel#setName(String)
   */
  public void setName(String s) {
    _name = s;
  }
  
  public void setUuid(String uuid) {
    _uuid = uuid;
  }
  
  /**
   * @return the <code>SystemDescriptor</code> that owns this <code>Channel</code>
   */
  public SystemDescriptor getSystemDescriptor() {
    return _s;
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
    if (_uuid != null) {
      atts.addAttribute("","uuid","uuid","PCDATA",_uuid.toString());
    }
    ch.startElement(DESCRIPTOR_URI,"channel","channel",atts);
    ch.endElement(DESCRIPTOR_URI,"channel","channel");
  }
  
  /*
   * Convenience method to look up a Service based on a ServicePort.
   * TODO use or remove.
   */
  private Service findServiceForPort(Port p) {
    Service[] ss = _s.getServices();
    for (int i=0; i < ss.length; ++i) {
      Port[] pp = ss[i].getImportedPorts();
      for (int j=0;j < pp.length; ++j) {
        if (pp[j] == p) {
          return ss[i];
        }
      }
      pp = ss[i].getExportedPorts();
      for (int j=0;j < pp.length; ++j) {
        if (pp[j] == p) {
          return ss[i];
        }
      }      
    }
    return null;
  }
}
