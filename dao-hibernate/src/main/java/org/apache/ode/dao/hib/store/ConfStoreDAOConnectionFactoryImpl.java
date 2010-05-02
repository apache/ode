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
package org.apache.ode.dao.hib.store;

import org.apache.ode.dao.hib.store.hobj.VersionTrackerDAOImpl;
import org.apache.ode.dao.hib.store.hobj.DeploymentUnitDaoImpl;
import org.apache.ode.dao.hib.store.hobj.ProcessConfDaoImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.utils.GUID;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.transaction.TransactionManager;
import org.apache.ode.dao.hib.SessionManager;
import static org.apache.ode.dao.hib.bpel.BpelDAOConnectionFactoryImpl.setupSessionManager;

public class ConfStoreDAOConnectionFactoryImpl implements ConfStoreDAOConnectionFactory {

  private static final Log __log = LogFactory.getLog(ConfStoreDAOConnectionFactoryImpl.class);
  private static final String _guid = new GUID().toString();
  private static final Map<String, DataSource> _dataSources = new ConcurrentHashMap<String, DataSource>();
  protected SessionManager _sessionManager;
  private TransactionManager _txm;
  private DataSource _ds;

  public void init(Properties initialProps, TransactionManager mgr, Object env) {
    _txm = mgr;
    _ds = (DataSource) env;
    _sessionManager = setupSessionManager(initialProps, _txm, _ds);

  }

  public void shutdown() {
    _sessionManager.shutdown();
  }

  public ConfStoreDAOConnectionImpl getConnection() {
    final ThreadLocal<ConfStoreDAOConnectionImpl> currentConnection = ConfStoreDAOConnectionImpl.getThreadLocal();
    ConfStoreDAOConnectionImpl conn = (ConfStoreDAOConnectionImpl) currentConnection.get();
    if (conn != null && !conn.isClosed()) {
      return conn;
    } else {
      conn = new ConfStoreDAOConnectionImpl(_sessionManager);
      currentConnection.set(conn);
      return conn;
    }
  }

  static Configuration getDefaultConfiguration() throws MappingException {
    return new Configuration().addClass(ProcessConfDaoImpl.class).addClass(DeploymentUnitDaoImpl.class).addClass(VersionTrackerDAOImpl.class);
  }
}
