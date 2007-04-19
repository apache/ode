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
package org.apache.ode.utils;

import org.apache.commons.logging.Log;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class LoggingDataSourceWrapper implements DataSource {

    private DataSource _wrapped;
    private Log _log;

    public LoggingDataSourceWrapper(DataSource wrapped, Log log) {
        _wrapped = wrapped;
        _log = log;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = new LoggingConnectionWrapper(_wrapped.getConnection(), _log);
        if (shouldPrint()) print("getConnection (tx=" + conn.getTransactionIsolation() + ")");
        return conn;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn =  new LoggingConnectionWrapper(_wrapped.getConnection(username, password), _log);
        if (shouldPrint()) print("getConnection (tx=" + conn.getTransactionIsolation() + ")");
        return conn;
    }

    public int getLoginTimeout() throws SQLException {
        return _wrapped.getLoginTimeout();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return _wrapped.getLogWriter();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        _wrapped.setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        _wrapped.setLogWriter(out);
    }

    private boolean shouldPrint() {
        if (_log != null)
            return _log.isDebugEnabled();
        else return true;
    }

    private void print(String str) {
        if (_log != null)
            _log.debug(str);
        else System.out.println(str);
    }

}
