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

package org.apache.ode.bpel.scheduler.quartz;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

class DataSourceConnectionProvider implements ConnectionProvider {
  private DataSource _ds;
  private int _isolationLevel;

  DataSourceConnectionProvider(DataSource ds) {
    _ds = ds;
    _isolationLevel = Integer.parseInt(System.getProperty("ode.connection.isolation", "0"));
  }
  
  public Connection getConnection() throws SQLException {
    Connection c = _ds.getConnection();
    if (_isolationLevel != 0) {
        c.setTransactionIsolation(_isolationLevel);
    }
    return c;
  }

  public void shutdown() throws SQLException {

  }

}
