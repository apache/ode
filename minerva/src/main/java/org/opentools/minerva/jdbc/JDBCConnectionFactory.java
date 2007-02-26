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

/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.opentools.minerva.pool.ObjectPool;
import org.opentools.minerva.pool.PoolObjectFactory;

/**
 * Object factory that creates java.sql.Connections.  This is meant for use
 * outside a J2EE/JTA environment - servlets alone, client/server, etc.  If
 * you're interested in creating transactional-aware connections, see
 * XAConnectionFactory, which complies with the JDBC 2.0 standard extension.
 * @see org.opentools.minerva.jdbc.xa.XAConnectionFactory
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class JDBCConnectionFactory extends PoolObjectFactory {
    private String url;
    private Properties props;
    private String userName;
    private String password;
    private PrintWriter log;
    private ObjectPool pool;

    /**
     * Creates a new factory.  You must configure it with JDBC properties
     * before you can use it.
     */
    public JDBCConnectionFactory() {
    }

    /**
     * Sets the JDBC URL used to create new connections.
     */
    public void setConnectURL(String url) {this.url = url;}

    /**
     * Gets the JDBC URL used to create new connections.
     */
    public String getConnectURL() {return url;}

    /**
     * Sets the JDBC Propeties used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setConnectProperties(Properties props) {this.props = props;}

    /**
     * Gets the JDBC Properties used to create new connections.
     */
    public Properties getConnectProperties() {return props;}

    /**
     * Sets the JDBC user name used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setUser(String userName) {this.userName = userName;}

    /**
     * Gets the JDBC user name used to create new connections.
     */
    public String getUser() {return userName;}

    /**
     * Sets the JDBC password used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setPassword(String password) {this.password = password;}

    /**
     * Gets the JDBC password used to create new connections.
     */
    public String getPassword() {return password;}

    /**
     * NOOP 
     * @deprecated
     */
    public void setPSCacheSize(int size) {
    }

    /**
     * Returns 0.
     * @deprecated
     */
    public int getPSCacheSize() {
        return 0;
    }

    /**
     * Validates that connection properties were set (at least a URL).
     */
    public void poolStarted(ObjectPool pool, PrintWriter log) {
        super.poolStarted(pool, log);
        if(url == null)
            throw new IllegalStateException("Must specify JDBC connection URL to "+getClass().getName());
        this.pool = pool;
    }

    /**
     * Cleans up.
     */
    public void poolClosing(ObjectPool pool) {
        super.poolClosing(pool);
        this.pool = null;
        log = null;
    }

    /**
     * Creates a new JDBC Connection.
     */
    public Object createObject(Object parameters) {
        try {
            if(userName != null && userName.length() > 0)
                return DriverManager.getConnection(url, userName, password);
            else if(props != null)
                return DriverManager.getConnection(url, props);
            else
                return DriverManager.getConnection(url);
        } catch(SQLException e) {
            if(log != null)
                e.printStackTrace(log);
        }
        return null;
    }

    /**
     * Wraps the connection with a ConnectionInPool.
     * @see org.opentools.minerva.jdbc.ConnectionInPool
     */
    public Object prepareObject(Object pooledObject) {
        Connection con = (Connection)pooledObject;
        ConnectionInPool wrapper = new ConnectionInPool(con);
        return wrapper;
    }

    /**
     * Returns the original connection from a ConnectionInPool.
     * @see org.opentools.minerva.jdbc.ConnectionInPool
     */
    public Object translateObject(Object clientObject) {
        return ((ConnectionInPool)clientObject).getUnderlyingConnection();
    }

    /**
     * Closes all outstanding work for the connection, rolls it back, and
     * returns the underlying connection to the pool.
     */
    public Object returnObject(Object clientObject) {
        ConnectionInPool wrapper = (ConnectionInPool)clientObject;
        Connection con = wrapper.getUnderlyingConnection();
        try {
            wrapper.invalidate();
        } catch(SQLException e) {
            pool.markObjectAsInvalid(clientObject);
        }
        return con;
    }

    /**
     * Closes a connection.
     */
    public void deleteObject(Object pooledObject) {
        Connection con = (Connection)pooledObject;
        try {
            con.rollback();
        } catch(SQLException e) {}

        try {
            con.close();
        } catch(SQLException e) {}
    }
}
