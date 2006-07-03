/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daomem;

import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;
import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;

import java.util.HashMap;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemDAOStoreConnectionFactoryImpl implements DAOConnectionFactory {
  private static final Log __log = LogFactory.getLog(InMemDAOStoreConnectionFactoryImpl.class);

  private static final HashMap<String,InMemDomainStore> _stores =
    new HashMap<String,InMemDomainStore>();

  private TransactionManager _txm;

  public InMemDAOStoreConnectionFactoryImpl(TransactionManager txm) {
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
      _stores.put(clusterId, new InMemDomainStore(clusterId));
    }

    __log.info("Created deployment store for cluster " + clusterId);
  }

  public DomainStateConnection open(String clusterId)  {
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
