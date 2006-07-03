/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.opentools.minerva;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.transaction.TransactionManager;

import org.opentools.minerva.connector.SharedLocalConnectionManager;
import org.opentools.minerva.connector.jdbc.JDBCDataSource;
import org.opentools.minerva.connector.jdbc.JDBCManagedConnectionFactory;
import org.opentools.minerva.pool.PoolParameters;


/**
 * Uses JCA style
 */
public class DataSourceFactory implements ObjectFactory {
  
  /**
   * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
   *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
   */
  public Object getObjectInstance(Object obj, Name n, Context nameCtx,
                                  Hashtable environment)
                           throws Exception {
    SharedLocalConnectionManager connManager = new SharedLocalConnectionManager();
    JDBCManagedConnectionFactory jdbcCF = new JDBCManagedConnectionFactory();

    PoolParameters pool = new PoolParameters();
    TransactionManager tm = null;

    try {
      Reference ref = (Reference)obj;
      Enumeration addrs = ref.getAll();

      while (addrs.hasMoreElements()) {
        RefAddr addr = (RefAddr)addrs.nextElement();
        String name = addr.getType();
        String value = (String)addr.getContent();

        if (name.equals("driverClassName")) {
          jdbcCF.setDriver(value);
        } else if (name.equals("url")) {
          jdbcCF.setConnectionURL(value);
        } else if (name.equals("username")) {
          jdbcCF.setUserName(value);
        } else if (name.equals("password")) {
          jdbcCF.setPassword(value);
        } else if (name.equals("min")) {
          pool.minSize = Integer.parseInt(value);
        } else if (name.equals("max")) {
          pool.maxSize = Integer.parseInt(value);
        } else if(name.equals("TransactionManager")){
        	InitialContext ctx = new InitialContext();
        	tm = (TransactionManager)ctx.lookup(value);
        }
      }

      if(tm == null)
      	throw new IllegalStateException("Requires transaction manager: set required property 'TransactionManager'");
      connManager.createPerFactoryPool(jdbcCF, pool);
      connManager.setTransactionManager(tm);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new JDBCDataSource(connManager, jdbcCF);
  }
}
