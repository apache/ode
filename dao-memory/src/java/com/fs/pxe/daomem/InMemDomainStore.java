/*
 * File:      $Id: InMemDomainStore.java 1460 2006-06-07 21:27:06Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daomem;

import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;
import com.fs.pxe.sfwk.bapi.dao.SystemDAO;
import com.fs.utils.ObjectPrinter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class InMemDomainStore {
  private static final Log __log = LogFactory.getLog(InMemDomainStore.class);

  private String _domainUUID;

  private final Map<String, SystemDAO> _systems = new ConcurrentHashMap<String, SystemDAO>();
  private final Map<String, SystemDAO> _systemsByName = new ConcurrentHashMap<String, SystemDAO>();
  
  InMemDomainStore(String clusterUUID) {
    _domainUUID = clusterUUID;
  }

  SystemDAO createSystemDeployment(String systemUUID, String systemName) {
    if (_systems.containsKey(systemUUID)) {
      String msg = "Duplicate system UUID: " + systemUUID;
      __log.error(msg);
      throw new RuntimeException(msg);
    }

    if (_systemsByName.containsKey(systemName)) {
      String msg = "Duplicate system UUID: " + systemUUID;
      __log.error(msg);
      throw new RuntimeException(msg);
    }

    SystemDAO dao = new SystemDaoImpl(this, systemUUID, systemName);
    _systems.put(systemUUID, dao);
    _systemsByName.put(systemName, dao);

    return dao;
  }

  Collection<SystemDAO> findAllSystems() {
    return _systems.values();
  }

  SystemDAO findSystem(String systemUUID) {
    return _systems.get(systemUUID);
  }

  SystemDAO findSystemByName(String systemName) {
    return _systemsByName.get(systemName);
  }

  void removeSystem(String systemUUID) {
    SystemDAO removed = _systems.remove(systemUUID);
    if (removed != null) {
      _systemsByName.remove(removed.getSystemName());
    }
  }

  DomainStateConnection getConnection(Transaction tx) 
  {
    if (tx == null) {
      throw new IllegalStateException("No transaction in thread!");
    }

    return new InMemDomainStateStoreConnectionImpl(this);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer("{");
    buf.append(ObjectPrinter.getShortClassName(this));
    buf.append("domainUUID=");
    buf.append(_domainUUID);
    buf.append("}");
    return buf.toString();
  }


}
