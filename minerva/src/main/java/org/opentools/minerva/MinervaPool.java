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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.opentools.minerva.connector.BaseConnectionManager;
import org.opentools.minerva.connector.NoTransConnectionManager;
import org.opentools.minerva.connector.SharedLocalConnectionManager;
import org.opentools.minerva.connector.jdbc.JDBCDataSource;
import org.opentools.minerva.connector.jdbc.JDBCManagedConnectionFactory;
import org.opentools.minerva.pool.PoolParameters;

public class MinervaPool {
  
  private static final Map<String,MinervaPool> __pools = Collections.synchronizedMap(new HashMap<String, MinervaPool>());
  
  private PoolParameters _params = new PoolParameters();
  
  JDBCManagedConnectionFactory _mcf = new JDBCManagedConnectionFactory();
  BaseConnectionManager _connManager;

  private String _id;

  private TransactionManager _tm;

  private PoolType _type = PoolType.MANAGED;
  private static int __numpools = 0;
  
  public MinervaPool() {
    synchronized(MinervaPool.class) {
      _id = "Minerva# " + ++__numpools; 
    }

    _params.blocking = false;
    _params.gcEnabled = true;
    _params.idleTimeoutEnabled = true;
    _params.invalidateOnError = true;
    _params.minSize = 0;
    _params.maxSize = 1;
  }

  public PoolParameters getPoolParams() {
    return _params;
  }

  public JDBCManagedConnectionFactory getConnectionFactory() {
    return _mcf;
  }
  
  public void setTransactionManager(TransactionManager tm) {
    _tm = tm;
  }
  
  public synchronized void start() throws Exception {
    if (_connManager != null)
      return;
    
    // First, lets make sure the connection works:

    // Try the managed connection factory to make sure it works.
    _mcf.createManagedConnection(null,null).destroy();
    
    switch (_type) {
    case UNMANAGED:
      _connManager = new NoTransConnectionManager();
      break;
    case MANAGED:
      _connManager = new SharedLocalConnectionManager();
      break;
    }
    _connManager.setTransactionManager(_tm);
    _connManager.createPerFactoryPool(_mcf, _params);
    __pools.put(_id, this);
  }
  
  public synchronized void stop() {
    __pools.remove(_id);
    _connManager.shutDown();
    _connManager = null;
  }
  String getId() {
    return _id;
  }
  
  static MinervaPool get(String id) {
    return __pools.get(id);
  }

  public PoolType getType() {
    return _type ;
  }
  
  public void setType(PoolType type) {
    _type = type;
  }
  
  public Reference createDataSourceReference() {
    Reference ref = new Reference(javax.sql.DataSource.class.getName(),
        MinervaJDBCDataSourceFactory.class.getName(), null);
    ref.add(new StringRefAddr(MinervaJDBCDataSourceFactory.REF_ID, _id));    // TODO Auto-generated method stub
    return ref;
  }

  public enum PoolType {
    MANAGED,
    UNMANAGED
  }

  public DataSource createDataSource() {
    JDBCDataSource ds = new JDBCDataSource(_connManager,_mcf);
    ds.setReference(createDataSourceReference());
    return ds;
  }

}
