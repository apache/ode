/*
 * File:      $Id: InMemDomainStateStoreConnectionFactoryImpl.java 807 2006-02-14 03:50:30Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daomem;

import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;
import com.fs.pxe.sfwk.bapi.dao.DomainStateConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemDomainStateStoreConnectionFactoryImpl implements DomainStateConnectionFactory {
  private static final Log __log = LogFactory.getLog(InMemDomainStateStoreConnectionFactoryImpl.class);

  private static final Map<String,InMemDomainStore> _stores = new ConcurrentHashMap<String,InMemDomainStore>();

  private TransactionManager _txm;

  public InMemDomainStateStoreConnectionFactoryImpl(TransactionManager txm) {
    _txm = txm;
  }

  /**
   * Clear the in-memory store.
   */
  public static void clear() {
    _stores.clear();
  }

  public void createDomainStateStore(String clusterId) {
    if (_stores.get(clusterId) == null) {
      InMemDomainStore storeInMem = new InMemDomainStore(clusterId);
      _stores.put(clusterId, storeInMem);
    }

    __log.info("Created deployment store for cluster " + clusterId);
  }

  public DomainStateConnection open(String clusterId) {
    InMemDomainStore storeInMem = _stores.get(clusterId);

    if (storeInMem == null) {
      throw new IllegalArgumentException("no such domain: " + clusterId);
    }

    try {
      return storeInMem.getConnection(_txm.getTransaction());
    } catch (SystemException se) {
      throw new RuntimeException("Tx Error", se);
    }
  }
}
