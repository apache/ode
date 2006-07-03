/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.impl;

import com.fs.pxe.sfwk.deployment.SystemDescriptorSerUtility;
import com.fs.pxe.sfwk.deployment.som.Channel;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;

import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Default implementation of the <code>SystemDescriptor</code> interface.
 */
public class SystemDescriptorImpl extends ALocated
  implements SystemDescriptor, Constants
{
	private static final long serialVersionUID = -2376268357112050061L;

  private String _uuid;
  private ChannelImpl[] _channels;
  private String _name;
  private ServiceImpl[] _services;
  private URI _wsdl;
  
  /**
   * Create a new instance.
   */
  public SystemDescriptorImpl() {
    _channels = new ChannelImpl[0];
    _services = new ServiceImpl[0];
  }
  
  /**
   * @see SystemDescriptor#addChannel(Channel)
   */
  public void addChannel(Channel c) {
    if (!(c instanceof ChannelImpl)) {
      throw new IllegalArgumentException();
    }
    ChannelImpl[] nu = new ChannelImpl[_channels.length + 1];
    System.arraycopy(_channels,0,nu,0,_channels.length);
    nu[_channels.length] = (ChannelImpl) c;
    _channels = nu;
  }

  /**
   * @see SystemDescriptor#getChannels()
   */  
  public Channel[] getChannels() {
    return _channels;
  }

  /**
   * @see SystemDescriptor#addService(Service)
   */  
  public void addService(Service s) {
    if (!(s instanceof ServiceImpl)) {
      throw new IllegalArgumentException();
    }
    ServiceImpl[] nu = new ServiceImpl[_services.length + 1];
    System.arraycopy(_services,0,nu,0,_services.length);
    nu[_services.length] = (ServiceImpl) s;
    _services = nu;
  }

  /**
   * @see SystemDescriptor#getServices()
   */  
  public Service[] getServices() {
    return _services;
  }
  
  /**
   * @see SystemDescriptor#setName(String)
   */
  public void setName(String s) {
    _name = s;
  }

  /**
   * @see SystemDescriptor#getName()
   */
  public String getName() {
    return _name;
  }

  /**
   * @see SystemDescriptor#setWsdlUri(URI)
   */
  public void setWsdlUri(URI u) {
    _wsdl = u;
  }

  /**
   * @see SystemDescriptor#getWsdlUri()
   */
  public URI getWsdlUri() {
    return _wsdl;
  }

  /**
   * @see SystemDescriptor#setUuid(String)
   */
  public void setUuid(String uuid) {
    _uuid = uuid;
  }

  /**
   * @see SystemDescriptor#getUuid()
   */
  public String getUuid() {
    return _uuid;
  }
  
  /**
   * Create a new <code>ServiceImpl</code> instance with a back-reference to
   * this instance.
   * @return the new instance.
   */
  public ServiceImpl newService() {
    return new ServiceImpl(this);
  }
  
  /**
   * Create a new <code>ChannelImpl</code> instance with a back-reference to this
   * instance.
   * @return the new instance.
   */
  public ChannelImpl newChannel() {
    return new ChannelImpl(this);
  }
    
  public String toString() {
    return SystemDescriptorSerUtility.fromSystemDescriptor(this);
  }
  
  /**
   * @see com.fs.pxe.sfwk.deployment.som.Marshallable#toSaxEvents(ContentHandler)
   */
  public void toSaxEvents(ContentHandler ch) throws SAXException {
    try {
      ch.startDocument();
      AttributesImpl atts = new AttributesImpl();
      if (_name != null) atts.addAttribute("","name","name","PCDATA",_name);
      if (_uuid != null) atts.addAttribute("","uuid","uuid","PCDATA",_uuid.toString());
      if (_wsdl != null) atts.addAttribute("","wsdlUri","wsdlUri","PCDATA",_wsdl.toString());
      ch.startPrefixMapping("",DESCRIPTOR_URI);
      ch.startElement(DESCRIPTOR_URI,"system-descriptor","",atts);
      ch.startElement(DESCRIPTOR_URI,"channels","",new AttributesImpl());
      for (int i=0; i < _channels.length; ++i) {
        _channels[i].toSaxEvents(ch);
      }
      ch.endElement(DESCRIPTOR_URI,"channels","");      
      ch.startElement(DESCRIPTOR_URI,"services","",new AttributesImpl());
      for (int i=0;i<_services.length;++i) {
        _services[i].toSaxEvents(ch);
      }
      ch.endElement(DESCRIPTOR_URI,"services","");
      ch.endElement(DESCRIPTOR_URI,"system-descriptor","");
      ch.endPrefixMapping("");
    } finally {
      ch.endDocument();
    }
  }
  
}
