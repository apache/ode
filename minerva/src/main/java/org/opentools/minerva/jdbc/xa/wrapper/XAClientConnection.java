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

import java.sql.Connection;
import java.sql.SQLException;

import org.opentools.minerva.jdbc.ConnectionInPool;
import org.opentools.minerva.pool.PoolEvent;

/**
 * Wrapper for database connections used by an XAConnection. When close is called, it does not close the underlying connection, just
 * informs the XAConnection that close was called. The connection will not be closed (or returned to the pool) until the
 * transactional details are taken care of. This instance only lives as long as one client is using it - though we probably want to
 * consider reusing it to save object allocations.
 * 
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAClientConnection extends ConnectionInPool {

    private XAConnectionImpl xaCon;

    /**
     * Creates a new connection wrapper.
     * 
     * @param xaCon
     *            The handler for all the transactional details.
     * @param con
     *            The "real" database connection to wrap.
     */
    public XAClientConnection(XAConnectionImpl xaCon, Connection con) {
        super(con);
        this.xaCon = xaCon;
    }

    /**
     * Updates the last used time for this connection to the current time. This is not used by the current implementation.
     */
    public void setLastUsed() {
        xaCon.firePoolEvent(new PoolEvent(xaCon, PoolEvent.OBJECT_USED));
    }

    /**
     * Indicates that an error occured on this connection.
     */
    public void setError(SQLException e) {
        xaCon.setConnectionError(e);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        enter();
        try {
            if (((XAResourceImpl) xaCon.getXAResource()).isTransaction() && autoCommit)
                throw new SQLException(
                        "Cannot set AutoCommit for a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");

            try {
                _con.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                setError(e);
                throw e;
            }
        } finally {
            exit();
        }

    }

    public void commit() throws SQLException {
        enter();
        try {
            if (((XAResourceImpl) xaCon.getXAResource()).isTransaction())
                throw new SQLException(
                        "Cannot commit a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");

            _con.commit();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void rollback() throws SQLException {
        enter();
        try {
            if (((XAResourceImpl) xaCon.getXAResource()).isTransaction())
                throw new SQLException(
                        "Cannot rollback a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");
        } finally {
            exit();
        }
    }

    public void close() throws SQLException {
        super.close();
        xaCon.clientConnectionClosed();
        xaCon = null;
    }
   
}
