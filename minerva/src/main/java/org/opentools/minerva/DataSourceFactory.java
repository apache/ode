/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
