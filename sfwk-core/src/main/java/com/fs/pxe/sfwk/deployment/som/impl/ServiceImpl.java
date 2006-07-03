/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import com.fs.pxe.sfwk.deployment.som.Port;
import com.fs.pxe.sfwk.deployment.som.Property;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Default implementation of the <code>Service</code> interface.
 */
public class ServiceImpl extends ALocated implements Service, Constants {

  private String _name;
  private URI _uri;
  private String _uuid;
  private Property[] _properties;
  private Port[] _exports;
  private Port[] _imports;
  private SystemDescriptorImpl _s;
  
  /**
   * Create a new instance with a back-reference to the <code>SystemDescriptorImpl</code>
   * instance that owns it.
   * @param s the <code>SystemDescriptorImpl</code> that owns this instance.
   */
  ServiceImpl(SystemDescriptorImpl s) {
    _exports = new Port[0];
    _imports = new Port[0];
    _properties = new Property[0];
    _s = s;
  }
  
  /**
   * @see Service#setName(String)
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @see Service#getName()
   */

  public String getName() {
    return _name;
  }

  /**
   * @see Service#setProviderUri(URI)
   */
  public void setProviderUri(URI u) {
    _uri = u;
  }

  /**
   * @see Service#getProviderUri()
   */

  public URI getProviderUri() {
    return _uri;
  }

  public void setUuid(String uuid) {
    _uuid = uuid;
  }

  /**
   * @see Service#getUuid()
   */
  public String getUuid() {
    return _uuid;
  }

  /**
   * @see Service#addProperty(Property)
   */
  public void addProperty(Property p) {
    Property[] nu = new Property[_properties.length + 1];
    System.arraycopy(_properties,0,nu,0,_properties.length);
    nu[_properties.length] = p;
    _properties = nu;
  }

  /**
   * @see Service#getProperties()
   */
  public Property[] getProperties() {
    return _properties;
  }

  /**
   * @see Service#addImportedPort(Port)
   */  
  public void addImportedPort(Port p) {
    Port[] nu = new Port[_imports.length + 1];
    System.arraycopy(_imports,0,nu,0,_imports.length);
    nu[_imports.length] = p;
    _imports = nu;
  }

  /**
   * @see Service#getImportedPorts()
   */
  public Port[] getImportedPorts() {
    return _imports;
  }

  /**
   * @see Service#addExportedPort(Port)
   */
  public void addExportedPort(Port p) {
    Port[] nu = new Port[_exports.length + 1];
    System.arraycopy(_exports,0,nu,0,_exports.length);
    nu[_exports.length] = p;
    _exports = nu;
  }

  /**
   * @see Service#getExportedPorts()
   */
  public Port[] getExportedPorts() {
    return _exports;
  }
  
  /**
   * Create a new <code>PortImpl</code> with a back-reference to this <code>ServiceImpl</code>
   * instance.
   * @return the new instance.
   */
  public PortImpl newPort() {
    return new PortImpl(this);
  }
  
  /**
   * @return the <code>SystemDescriptor</code> that owns this <code>Service</code>
   */
  public SystemDescriptor getSystemDescriptor() {
    return _s;
  }
  
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
    if (_uri != null) {
      atts.addAttribute("","provider","provider","PCDATA",_uri.toString());
    }
    if (_uuid != null) {
      atts.addAttribute("","uuid","uuid","PCDATA",_uuid.toString());
    }
    ch.startElement(DESCRIPTOR_URI,"service","service",atts);
    if (_properties.length > 0) {
      ch.startElement(DESCRIPTOR_URI,"properties","properties",new AttributesImpl());
      for (int i=0; i < _properties.length; ++i) {
        ((PropertyImpl) _properties[i]).toSaxEvents(ch);
      }
      ch.endElement(DESCRIPTOR_URI,"properties","properties");
    }
    if (_imports.length > 0) {
      ch.startElement(DESCRIPTOR_URI,"imports","imports",new AttributesImpl());
      for (int i=0; i < _imports.length; ++i) {
        ((PortImpl) _imports[i]).toSaxEvents(ch);
      }
      ch.endElement(DESCRIPTOR_URI,"imports","imports");
    }
    if (_exports.length > 0) {
      ch.startElement(DESCRIPTOR_URI,"exports","exports",new AttributesImpl());
      for (int i=0; i < _exports.length; ++i) {
        ((PortImpl) _exports[i]).toSaxEvents(ch);
      }
      ch.endElement(DESCRIPTOR_URI,"exports","exports");      
    }
    ch.endElement(DESCRIPTOR_URI,"service","service");
  }
}
