/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daomem;

import org.apache.ode.sfwk.bapi.dao.DomainStateConnection;
import org.apache.ode.sfwk.bapi.dao.SystemDAO;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class InMemDomainStateStoreConnectionImpl implements DomainStateConnection {
  private static final Log __log = LogFactory.getLog(InMemDomainStateStoreConnectionImpl.class);
  private static int CNT = 0;

  private final int _id = ++CNT;
  private InMemDomainStore _storeInMem;

  InMemDomainStateStoreConnectionImpl(InMemDomainStore storeInMem) {
    _storeInMem = storeInMem;
  }

  public synchronized void close()  {
    if (_storeInMem == null) {
      return;
    }

    _storeInMem = null;
    
    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": closed");
    }
  }

  protected void finalize() {
    try {
      this.close();
    } catch (Exception ex) {
      // ignore
    }
  }

  public SystemDAO createSystemDeployment(String systemUUID, String systemName) {
    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": createSystemDeployment (systemUUID="
          + systemUUID + ")");
    }

    SystemDAO system = _storeInMem.createSystemDeployment(systemUUID, systemName);

    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": createSystemDeployment (systemUUID="
          + systemUUID + ",...) = " + system);
    }

    return system;
  }

  public Collection<SystemDAO> findAllSystems() {
    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": findAllSystems()");
    }

    Collection<SystemDAO> systems = _storeInMem.findAllSystems();

    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": findAllSystems()");
    }

    return systems;
  }

  public SystemDAO findSystem(String systemUUID) {
    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": findSystem(" + systemUUID + ")");
    }

    SystemDAO system = _storeInMem.findSystem(systemUUID);

    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": findSystem(" + systemUUID + ") = " + system);
    }

    return system;
  }

  public SystemDAO findDeployedSystemByName(String systemName) {
    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": findSystemByName(" + systemName + ")");
    }

    SystemDAO system = _storeInMem.findSystemByName(systemName);

    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": findSystemByName(" + systemName + ") = " + system);
    }

    return system;
  }

  public void removeSystem(String systemUUID) {
    if (__log.isDebugEnabled()) {
      __log.debug("conn#" + _id + ": removeSystemDeployment (systemUUID=" + systemUUID + ")");
    }

    _storeInMem.removeSystem(systemUUID);
  }

}
