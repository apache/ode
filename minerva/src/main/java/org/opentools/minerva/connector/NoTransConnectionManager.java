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
package org.opentools.minerva.connector;

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.opentools.minerva.pool.ObjectPool;

/**
 * Manages connections which don't use transactions at all.
 * There's no sharing - there's a one-to-one mapping of
 * ManagedConnections to client connections.
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public class NoTransConnectionManager extends BaseConnectionManager implements Serializable {
    private static final long serialVersionUID = -571222360616189505L;

    public NoTransConnectionManager() {
    }

    /**
     * Gets a ManagedConnection from the pool and creates a new client
     * connection for it.
     */
    public Object allocateConnection(ManagedConnectionFactory factory, ConnectionRequestInfo info) throws ResourceException {
        Subject subj = null;
        ObjectPool pool = null;

        // Look up the current Subject
        if(sec != null) {
            subj = sec.getSubject(factory, pools.getPoolName(factory));
        }

        // Check for a pool
        pool = pools.getPool(factory, subj, info);

        // Create a connection (from the pool or otherwise)
        ManagedConnection con = null;
        if(pool == null) {
            // Unable to pool - why wasn't it configured at deployment?
            // Create a new connection
            con = factory.createManagedConnection(subj, info);
        } else {
            // Check whether there's a pooled connection for this factory
            // and create one if necessary
            con = (ManagedConnection)pool.getObject(new ConnectionParameters(subj, info));
        }

        ConnectionListener listener = new NoTransactionListener(pool, con);

        // Add the connection/transaction listener
        try {
            listener.register();
            listener.addHandle();
        } catch(SystemException e) {
            throw new ApplicationServerInternalException("Unable to register no-transaction listener: "+e.getMessage());
        } catch(RollbackException e) {
            throw new ApplicationServerInternalException("Unable to register no-transaction listener: TX is rolled back.");
        }

        // Extract the Connection object
        Object handle = con.getConnection(subj, info);
        connectionHandleIssued(handle, listener);
        return handle;
    }

    /**
     * Enlist a previously checked-out connection in the current transaction.
     * Since this implementation ignores transactions, there's nothing to do
     * here.
     */
    public void enlistExistingConnection(Object connection) throws ResourceException {
    }
}
