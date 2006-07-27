/*
 * File:      $Id: SystemDaoImpl.java 1436 2006-05-31 03:59:18Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daomem;

import org.apache.ode.sfwk.bapi.dao.MessageExchangeDAO;
import org.apache.ode.sfwk.bapi.dao.SystemDAO;
import org.apache.ode.sfwk.deployment.som.SystemDescriptor;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.StreamUtils;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link SystemDAO} implementation.
 */
class SystemDaoImpl implements SystemDAO {
  private String _systemUUID;
  private String _systemName;
  private SystemDescriptor _descriptor;
  private final Map<String, MessageExchangeDAO> _messageExchangeDAOs =
    new ConcurrentHashMap<String, MessageExchangeDAO>();
  private boolean _active;
  private InMemDomainStore _domainStore;
  private short _deployState;
  private byte[] _sar;
  private boolean _deployed;

  SystemDaoImpl(InMemDomainStore domainStore, String systemUUID, String systemName) {
    _domainStore = domainStore;
    _systemUUID = systemUUID;
    _systemName = systemName;
  }

  public InputStream getSystemArchive() {
    if (_sar == null) {
      return null;
    }
    return new ByteArrayInputStream(_sar);
  }

  public boolean isDeployed() {
    return _deployed;
  }

  public void setDeployed(boolean deployed) {
    _deployed = deployed;
  }

  public void setSystemArchive(InputStream sarstream) {
    try {
      _sar = StreamUtils.read(sarstream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void delete() {
    _domainStore.removeSystem(_systemUUID);
  }

  public boolean isEnabled() {
    return _active;
  }

  public short getDeployState() {
    return _deployState;
  }

  public void setDeployState(short deployed) {
    _deployState = deployed;
  }

  public void setEnabled(boolean active) {
    _active = active;
  }

  public void setName(String systemName) {
    _systemName = systemName;
  }

  public Iterator<MessageExchangeDAO> getAllMessageExchanges() {
    return _messageExchangeDAOs.values().iterator();
  }

  public MessageExchangeDAO getMessageExchange(String instanceId) {
    return _messageExchangeDAOs.get(instanceId);
  }

  public void setSystemDescriptor(SystemDescriptor system) {
    _descriptor = system;
  }

  public SystemDescriptor getSystemDescriptor() {
    return _descriptor;
  }

  public String getSystemName() {
    return _systemName;
  }

  public String getSystemUUID() {
    return _systemUUID;
  }

  public MessageExchangeDAO newMessageExchange(String instanceId,
                                               Node sourceEndpoint,
                                               Node destinationEndpoint,
                                               String operationName,
                                               QName portType,
                                               String channelName) {
    MessageExchangeDAO mex = new MessageExchangeDaoImpl(instanceId,
            sourceEndpoint, destinationEndpoint, operationName, portType, channelName);
    _messageExchangeDAOs.put(instanceId, mex);
    return mex;
  }

  public void removeMessageExchange(String instanceId) {
    _messageExchangeDAOs.remove(instanceId);
  }

  public String toString() {
    StringBuilder buf = new StringBuilder("{");
    buf.append(ObjectPrinter.getShortClassName(this.getClass()));
    buf.append(" systemUUID=");
    buf.append(_systemUUID);
    buf.append(" name=");
    buf.append(_systemName);
    buf.append("}");
    return buf.toString();
  }
}
