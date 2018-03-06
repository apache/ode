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

package org.apache.ode.daohib;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.apache.ode.utils.DbIsolation;

import org.hibernate.HibernateException;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;



public class DataSourceConnectionProvider implements ConnectionProvider, Configurable {

  private Properties _props;
  
  public DataSourceConnectionProvider() {
  }
  
  public void configure(Map props) throws HibernateException {
    _props = new Properties();
    _props.putAll(props);
  }

  public Connection getConnection() throws SQLException {
    Connection c = SessionManager.getConnection(_props);
    DbIsolation.setIsolationLevel(c);
    return c;
  }

  public void closeConnection(Connection con) throws SQLException {
    con.close();
  }

  public void close() throws HibernateException {

  }

  public boolean supportsAggressiveRelease() {
    return true;
  }

  public boolean isUnwrappableAs(Class unwrapType) {
      return ConnectionProvider.class.equals(unwrapType) ||
              DataSourceConnectionProvider.class.isAssignableFrom(unwrapType);
  }

  public <T> T unwrap(Class<T> unwrapType) {
      if (ConnectionProvider.class.equals(unwrapType) ||
              DataSourceConnectionProvider.class.isAssignableFrom(unwrapType)) {
          return (T) this;
      } else {
          throw new UnknownUnwrapTypeException( unwrapType );
      }
  }
}
