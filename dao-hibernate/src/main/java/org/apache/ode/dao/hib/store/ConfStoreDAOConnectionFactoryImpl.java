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

import static org.apache.ode.dao.hib.bpel.BpelDAOConnectionFactoryImpl.setupSessionManager;

import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.hib.SessionManager;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;

public class ConfStoreDAOConnectionFactoryImpl implements ConfStoreDAOConnectionFactory {
	
     private static final Log __log = LogFactory.getLog(ConfStoreDAOConnectionFactoryImpl.class);

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

}
