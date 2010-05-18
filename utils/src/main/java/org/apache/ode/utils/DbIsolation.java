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

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility to set Ode-specific isolation level on DataSource connections.
 */
public class DbIsolation {
    private static final Log __log = LogFactory.getLog(DbIsolation.class);
    private static int _isolationLevel;

    // Read Ode-specific isolation level configuration 
    static {
        try {
            _isolationLevel = Integer.parseInt(System.getProperty("ode.connection.isolation", "2"));
        } catch (Throwable t) {
            __log.error("Error while reading 'ode.connnection.isolation' property", t);
        }
    }

    public static int getOdeIsolationLevel() {
        return _isolationLevel;
    }
    
    public static void setOdeIsolationLevel(int isolationLevel) {
        _isolationLevel = isolationLevel;
    }
    
    /**
     * Set Ode-specific isolation level on the connection, if needed.
     */
    public static void setIsolationLevel(Connection c) throws SQLException {
        try {
            if (_isolationLevel != 0 && c.getTransactionIsolation() != _isolationLevel) {
                if (__log.isDebugEnabled()) __log.debug("Set isolation level to "+_isolationLevel);
                c.setTransactionIsolation(_isolationLevel);
            }
        } catch (Exception e) {
            if (__log.isDebugEnabled()) 
                __log.debug("Error while setting isolation level to "+_isolationLevel, e);
        }
    }

}
