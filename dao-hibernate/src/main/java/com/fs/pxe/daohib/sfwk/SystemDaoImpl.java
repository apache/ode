/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk;

import com.fs.pxe.daohib.sfwk.hobj.HSfwkMessageExchange;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.daohib.sfwk.hobj.HSystem;
import com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO;
import com.fs.pxe.sfwk.bapi.dao.SystemDAO;
import com.fs.pxe.sfwk.deployment.SystemDescriptorSerUtility;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.utils.QNameUtils;
import org.hibernate.Session;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

/**
 * Hibernate-based {@link SystemDAO} implementation.
 */
final class SystemDaoImpl implements SystemDAO {
  HSystem _system;
  Session _sess;

  /** Constructor. */
  public SystemDaoImpl(Session sess, HSystem system) {
    _system = system;
    _sess = sess;
  }

  @SuppressWarnings("unchecked")
  public Iterator<MessageExchangeDAO> getAllMessageExchanges() {
    return Collections.EMPTY_LIST.iterator();
  }

  public void setSystemArchive(InputStream sarstream) {
    try {
      if (_system.getSystemArchive() != null)
        _sess.delete(_system.getSystemArchive());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      for (int b = sarstream.read(); b >= 0; b = sarstream.read()) {
            outputStream.write((byte) b);
      }
      byte[] barr = outputStream.toByteArray();
      if (barr.length > 0) {
        HLargeData ld = new HLargeData(barr);
        _system.setSystemArchive(ld);
        _sess.save(ld);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.SystemDAO#getSystemArchive()
   */
  public InputStream getSystemArchive() {
    if (_system.getSystemArchive() == null) return null;
    if (_system.getSystemArchive().getBinary() == null) return null;
    return new ByteArrayInputStream(_system.getSystemArchive().getBinary());
  }

  public MessageExchangeDAO getMessageExchange(String instanceId) {
    HSfwkMessageExchange mex;
    mex = (HSfwkMessageExchange)_sess.get(HSfwkMessageExchange.class, instanceId);

    if(mex == null){
      return null;
    }
    return new MessageExchangeDaoImpl(_sess, mex);
  }

  public void setSystemDescriptor(SystemDescriptor system) {
    if (system == null) {
      _system.setSystemDeploymentDescriptor(null);
    }
    else {
      if (_system.getSystemDeploymentDescriptor() != null)
        _sess.delete(_system.getSystemDeploymentDescriptor());
      String str = SystemDescriptorSerUtility.fromSystemDescriptor(system);
      if (str != null && str.length() > 0) {
        HLargeData ld = new HLargeData(SystemDescriptorSerUtility.fromSystemDescriptor(system));
        _system.setSystemDeploymentDescriptor(ld);
        _sess.save(ld);
      }
    }
  }

  public SystemDescriptor getSystemDescriptor() {
    String str = _system.getSystemDeploymentDescriptor().getText();
    if (str == null) {
      return null;
    }
    return SystemDescriptorSerUtility.toSystemDescriptor(str);
  }

  public String getSystemName() {
    return _system.getSystemName();
  }

  public String getSystemUUID() {
    return _system.getSystemUUID();
  }

  public MessageExchangeDAO newMessageExchange(String instanceId, Node sourceEpr, Node destEpr, String operationName,
                                               QName portType, String channelName) {
    HSfwkMessageExchange ex = new HSfwkMessageExchange();
    ex.setOperationName(operationName);
    ex.setPortType(QNameUtils.fromQName(portType));
    ex.setChannelName(channelName);
    ex.setInstanceId(instanceId);

    ex.setSystem(_system);

    _sess.save(ex);
//   The following causes table-level locks in db.
//    _system.getMessageExchanges().add(ex);

    MessageExchangeDaoImpl dao = new MessageExchangeDaoImpl(_sess, ex);
    dao.setSourceEndpoint(sourceEpr);
    dao.setDestinationEndpoint(destEpr);
    return dao;
  }

  public void removeMessageExchange(String instanceId) {
    HSfwkMessageExchange ex = (HSfwkMessageExchange)_sess.get(HSfwkMessageExchange.class, instanceId);
//  The following causes table-level locks in db.
//    _system.getMessageExchanges().remove(ex);
    _sess.delete(ex);
  }

  public void delete() {
    _sess.delete(_system);
    _system.getDomain().getSystems().remove(_system);
  }

  public boolean isEnabled() {
    return _system.getActive();
  }

  public void setEnabled(boolean active) {
    _system.setActive(active);
  }

  public void setName(String systemName) {
    _system.setSystemName(systemName);
  }

  public boolean isDeployed() {
    return _system.getDeployed();
  }

  public void setDeployed(boolean deployed) {
    _system.setDeployed(deployed);
  }

}
