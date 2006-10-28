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
package org.opentools.minerva.connector.jdbc;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;

/**
 * Connector CCI ConnectionFactory implementation for JDBC drivers.
 * It doesn't actually implement the CCI interfaces because JDBC already
 * has well-defined interfaces, but it is the equivalent.  Note that this
 * implementation will not work if JNDI Serialization is used instead of
 * a JNDI Reference.  The Reference should be set by the app server at
 * deployment.
 * @see javax.resource.Referenceable
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 4 $
 */
public class JDBCDataSource implements DataSource, Serializable, Referenceable {
    private static final long serialVersionUID = -7067067533116086114L;
    
    private transient ConnectionManager manager;
    private transient ManagedConnectionFactory factory;
    private transient PrintWriter logger;
    private Reference jndiReference;

    public JDBCDataSource(ConnectionManager manager, ManagedConnectionFactory factory) {
        this.manager = manager;
        this.factory = factory;
    }

    /**
     * JNDI reference is set during deployment
     */
    public void setReference(Reference ref) {
        jndiReference = ref;
    }

    /**
     * Used by JNDI
     */
    public Reference getReference() {
        return jndiReference;
    }

    public Connection getConnection() throws java.sql.SQLException {
        try {
            return (Connection)manager.allocateConnection(factory, null);
        } catch(ResourceException e) {
            throw new SQLException("Unable to get Connection: "+e);
        }
    }
    public Connection getConnection(String user, String password) throws java.sql.SQLException {
        try {
            return (Connection)manager.allocateConnection(factory, new JDBCConnectionRequestInfo(user, password));
        } catch(ResourceException e) {
            throw new SQLException("Unable to get Connection: "+e);
        }
    }
    public PrintWriter getLogWriter() throws java.sql.SQLException {
        return logger;
    }
    public int getLoginTimeout() throws java.sql.SQLException {
        return 0;
    }
    public void setLogWriter(PrintWriter writer) throws java.sql.SQLException {
        logger = writer;
    }
    public void setLoginTimeout(int timeout) throws java.sql.SQLException {
        throw new SQLException("Method setLoginTimeout() not implemented.");
    }
}
