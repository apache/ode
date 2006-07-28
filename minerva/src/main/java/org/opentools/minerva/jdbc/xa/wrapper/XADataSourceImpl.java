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
package org.opentools.minerva.jdbc.xa.wrapper;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;
import javax.sql.*;

/**
 * Transactional DataSource wrapper for JDBC 1.0 drivers.  This is very
 * lightweight - it just passes requests through to an underlying driver, and
 * wraps the results with an XAConnection.  The XAConnection and corresponding
 * XAResource are responsible for closing the connection when appropriate.
 * Note that the underlying driver may perform pooling, but need not.  This
 * class does not add any pooling capabilities.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XADataSourceImpl implements XADataSource {
    private String url;
    private String user;
    private String password;
    private Properties properties;
    private int loginTimeout;
    private PrintWriter logWriter;

    /**
     * Empty constructure for beans, reflection, etc.
     */
    public XADataSourceImpl() {
    }

    /**
     * Specifies the URL and properties to connect to the underlying driver.
     * If the properties are null, they will not be used.
     */
    public XADataSourceImpl(String url, Properties properties) {
        this.url = url;
        this.properties = properties;
    }

    /**
     * Gets the JDBC URL used to open an underlying connection.
     */
    public String getURL() {return url;}

    /**
     * Sets the JDBC URL used to open an underlying connection.
     */
    public void setURL(String url) {this.url = url;}

    /**
     * Gets the JDBC user name used to open an underlying connection.
     */
    public String getUser() {return user;}

    /**
     * Sets the JDBC user name used to open an underlying connection.
     * This is optional - use it only if your underlying driver requires it.
     */
    public void setUser(String user) {this.user = user;}

    /**
     * Gets the JDBC password used to open an underlying connection.
     */
    public String getPassword() {return password;}

    /**
     * Sets the JDBC password used to open an underlying connection.
     * This is optional - use it only if your underlying driver requires it.
     */
    public void setPassword(String password) {this.password = password;}

    /**
     * Gets the JDBC properties used to open an underlying connection.
     */
    public Properties getProperties() {return properties;}

    /**
     * Sets the JDBC properties used to open an underlying connection.
     * This is optional - use it only if your underlying driver requires it.
     */
    public void setProperties(Properties properties) {this.properties = properties;}

    /**
     * Gets the log writer used to record when XAConnections are opened.
     */
    public PrintWriter getLogWriter() throws SQLException {return logWriter;}

    /**
     * Sets a log writer used to record when XAConnections are opened.
     */
    public void setLogWriter(PrintWriter writer) throws SQLException {logWriter = writer;}

    /**
     * This is not used by the current implementation, since the effect would
     * differ depending on the underlying driver.
     */
    public int getLoginTimeout() throws SQLException {return loginTimeout;}

    /**
     * This is not used by the current implementation, since the effect would
     * differ depending on the underlying driver.
     */
    public void setLoginTimeout(int timeout) throws SQLException {loginTimeout = timeout;}

    /**
     * Gets an XAConnection.  This first gets a java.sql.Connection from the
     * underlying driver, and then wraps it in an XAConnection and XAResource.
     * This uses the URL, user, password, and properties (or as many as you
     * have specified) to make the connection.
     */
    public XAConnection getXAConnection() throws SQLException {
        Connection con;
        if(user != null && user.length() > 0)
            con = DriverManager.getConnection(url, user, password);
        else if(properties != null)
            con = DriverManager.getConnection(url, properties);
        else
            con = DriverManager.getConnection(url);

        try {
            con.setAutoCommit(false);
        } catch(SQLException e) {
            if(logWriter != null)
                logWriter.println("XADataSource unable to disable auto-commit on "+con.getClass().getName());
        }

        XAResourceImpl res = new XAResourceImpl(con);
        XAConnectionImpl xacon = new XAConnectionImpl(con, res);
        res.setXAConnection(xacon);

        if(logWriter != null)
            logWriter.println(getClass().getName()+" created new Connection ("+con.getClass().getName()+") with XAResource "+res.getClass().getName()+" and XAConnection "+xacon.getClass().getName()+".");

        return xacon;
    }

    /**
     * Gets an XAConnection.  This first gets a java.sql.Connection from the
     * underlying driver, and then wraps it in an XAConnection and XAResource.
     * This first sets the default user name and password to the values you
     * specify.  Then it uses those, the URL, and properties (or as many as you
     * have specified) to make the connection.
     */
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        this.user = user;
        this.password = password;
        return getXAConnection();
    }
}
