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
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.opentools.minerva.jdbc.ConnectionInPool;
import org.opentools.minerva.pool.PoolEvent;
import org.opentools.minerva.pool.PoolEventListener;

/**
 * ManagedConnection implementation for JDBC connections.  Rolls back on
 * cleanup, closes on destroy.  This represents one physical connection to
 * the DB, and it can be shared, but uses LocalTransactions only (no
 * XAResource).
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 245 $
 */
public class JDBCManagedConnection extends BaseManagedConnection {
    private Connection con;
    private String url;

    public JDBCManagedConnection(Connection con, String user, String url) {
        super(user);
        this.con = con;
        this.url = url;
    }

    public Object getConnection(Subject sub, ConnectionRequestInfo info) throws ResourceException {
        final ConnectionInPool wrapper = new ConnectionInPool(con, ConnectionInPool.PS_CACHE_UNLIMITED);
        wrapper.addPoolEventListener(new PoolEventListener() {
            public void objectClosed(PoolEvent evt) {
                ConnectionEvent ce = new ConnectionEvent(JDBCManagedConnection.this, ConnectionEvent.CONNECTION_CLOSED);
                ce.setConnectionHandle(wrapper);
                fireConnectionEvent(ce);
                wrapper.removePoolEventListener(this);
            }
            public void objectError(PoolEvent evt) {
                ConnectionEvent ce = new ConnectionEvent(JDBCManagedConnection.this, ConnectionEvent.CONNECTION_ERROR_OCCURRED);
                ce.setConnectionHandle(wrapper);
                fireConnectionEvent(ce);
                wrapper.removePoolEventListener(this);
            }
            public void objectUsed(PoolEvent evt) {}
        });
        return wrapper;
    }

    public XAResource getXAResource() throws ResourceException {
        throw new ResourceException("getXAResource not supported");
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        return new JDBCLocalTransaction(con);
    }

    public void destroy() throws ResourceException {
        super.destroy();
        try {
            con.close();
        } catch(SQLException e) {
            throw new ResourceException("Unable to close DB connection: "+e);
        }
        con = null;
    }

    public void cleanup() throws ResourceException {
        try {
            con.rollback();
        } catch(SQLException e) {
            throw new ResourceException("Unable to rollback DB connection: "+e);
        }
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        /**@todo: Implement this javax.resource.spi.ManagedConnection method*/
        throw new java.lang.UnsupportedOperationException("Method getMetaData() not yet implemented.");
    }

    String getURL() {
        return url;
    }
}
