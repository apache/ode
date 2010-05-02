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
package org.apache.ode.il.dbutil;

import java.io.File;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.h2.jdbcx.JdbcDataSource;

public class H2Database implements EmbeddedDatabase {

  private static final Log __log = LogFactory.getLog(H2Database.class);
  private DatabaseConnectionManager _connectionManager;
  private String _dbUrl = null;
  private DataSource _dataSource = null;

  public void init(File workRoot, OdeConfigProperties props, TransactionManager txm) {
    String db = props.getDbEmbeddedName();
    if (workRoot == null) {
      _dbUrl = "jdbc:h2:mem:" + db + ";DB_CLOSE_DELAY=-1";
      JdbcDataSource hds = new JdbcDataSource();
      hds.setURL(_dbUrl);
      hds.setUser("sa");
      _dataSource = hds;
    } else {
      _dbUrl = "jdbc:h2:" + workRoot + File.separator + db;
      if (!props.isDbEmbeddedCreate()) {
        _dbUrl += ";IFEXISTS=TRUE";
      }
      String clazz = org.h2.Driver.class.getName();
      _connectionManager = new DatabaseConnectionManager(txm, props);
      try {
        _connectionManager.init(_dbUrl, clazz, "sa", null);
      } catch (DatabaseConfigException ex) {
        __log.error("Unable to initialize connection pool", ex);
      }
      _dataSource = _connectionManager.getDataSource();
    }
    __log.debug("Using Embedded Database: " + _dbUrl);
  }

  public void shutdown() {
    if (_connectionManager != null) {
      try {
        _connectionManager.shutdown();
      } catch (DatabaseConfigException ex) {
        __log.error("unable to shutdown connection pool", ex);
      }
    }
    try {
      Connection conn = getDataSource().getConnection();
      conn.createStatement().execute("SHUTDOWN");
    } catch (Throwable ex) {
      __log.debug("Error shutting down H2.", ex);
    }
  }

  public DataSource getDataSource() {
    return _dataSource;
  }
}
