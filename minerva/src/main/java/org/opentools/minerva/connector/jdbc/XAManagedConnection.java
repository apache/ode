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

import java.sql.Connection;
import java.sql.SQLException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.sql.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

/**
 * ManagedConnection implementation for XADataSource connections.  Does nothing
 * on cleanup, closes on destroy.  This represents one physical connection to
 * the DB.  It cannot be shared, and uses XAResources only (no
 * LocalTransactions).
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 449 $
 */
public class XAManagedConnection extends BaseManagedConnection {
    private XAConnection con;
    private XADataSource source;

    public XAManagedConnection(XADataSource source, XAConnection con, String user) {
        super(user);
        this.con = con;
        this.source = source;
    }

    public LocalTransaction getLocalTransaction() throws javax.resource.ResourceException {
        throw new ResourceException("getLocalTransaction not supported");
    }

    public XAResource getXAResource() throws javax.resource.ResourceException {
        try {
            return con.getXAResource();
        } catch(SQLException e) {
            ResourceException re = new ResourceException("Unable to get XAResource: "+e, e);
            throw re;
        }
    }

    /**
     * This implementation does not support re-authentication.  It also does
     * not support connection sharing.
     */
    public Object getConnection(Subject sub, ConnectionRequestInfo info) throws javax.resource.ResourceException {
        try {
            final Connection wrapper = con.getConnection();
            con.addConnectionEventListener(new ConnectionEventListener() {
                public void connectionClosed(javax.sql.ConnectionEvent evt) {
                    javax.resource.spi.ConnectionEvent ce = new javax.resource.spi.ConnectionEvent(XAManagedConnection.this, javax.resource.spi.ConnectionEvent.CONNECTION_CLOSED);
                    ce.setConnectionHandle(wrapper);
                    fireConnectionEvent(ce);
                    con.removeConnectionEventListener(this);
                }
                public void connectionErrorOccurred(javax.sql.ConnectionEvent evt) {
                    javax.resource.spi.ConnectionEvent ce = new javax.resource.spi.ConnectionEvent(XAManagedConnection.this, javax.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED);
                    ce.setConnectionHandle(wrapper);
                    fireConnectionEvent(ce);
                    con.removeConnectionEventListener(this);
                }
            });
            return wrapper;
        } catch(SQLException e) {
            ResourceException re = new ResourceException("Unable to get XAResource: "+e, e);
            throw re;
        }
    }

    public void destroy() throws ResourceException {
        super.destroy();
        try {
            con.close();
        } catch(SQLException e) {
            ResourceException re = new ResourceException("Unable to close DB connection: "+e, e);
            throw re;
        }
        con = null;
        source = null;
    }

    public void cleanup() throws ResourceException {
    }

    public ManagedConnectionMetaData getMetaData() throws javax.resource.ResourceException {
        /**@todo: implement this org.opentools.minerva.connector.jdbc.BaseManagedConnection abstract method*/
        throw new java.lang.UnsupportedOperationException("Method getMetaData() not yet implemented.");
    }

    XADataSource getDataSource() {
        return source;
    }
}