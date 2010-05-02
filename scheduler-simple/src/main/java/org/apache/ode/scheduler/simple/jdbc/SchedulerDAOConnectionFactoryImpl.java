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
package org.apache.ode.scheduler.simple.jdbc;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import org.apache.ode.dao.scheduler.SchedulerDAOConnection;
import org.apache.ode.dao.scheduler.SchedulerDAOConnectionFactory;


public class SchedulerDAOConnectionFactoryImpl implements SchedulerDAOConnectionFactory {
  static ThreadLocal<SchedulerDAOConnection> _connections = new ThreadLocal<SchedulerDAOConnection>();
  DataSource _ds;
  TransactionManager _txm;
  AtomicBoolean _active = new AtomicBoolean(true);

  public void init(Properties odeConfig, TransactionManager mgr, Object env) {
    _ds = (DataSource) env;
    _txm = mgr;
  }

  public SchedulerDAOConnection getConnection() {
    if (_connections.get()==null || _connections.get().isClosed() ){
      _connections.set(new SchedulerDAOConnectionImpl(_active,_ds,_txm));
    }
    return _connections.get();
    
  }

  public void shutdown() {
    _active.set(false);
  }
}
