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
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.opentools.minerva.pool.ObjectPool;

/**
 * Manages shared connections using LocalTransactions.  All connections
 * for the same factory and transaction are handles to a single
 * ManagedConnection instance.
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
@SuppressWarnings("unchecked")
public class SharedLocalConnectionManager extends BaseConnectionManager implements Serializable {
    private static final long serialVersionUID = -2880654825235129911L;
    
    // Instance variables
    private transient WeakHashMap txConnections;

    public SharedLocalConnectionManager() {
        txConnections = new WeakHashMap();
    }

    /**
     * This implementation assumes single-threaded access per transaction
     * (either on thread per TX, or strictly serial access from different
     * threads per TX).  It does not create a local transaction if there
     * is not currently a global transaction.
     */
    public Object allocateConnection(ManagedConnectionFactory factory, ConnectionRequestInfo info) throws ResourceException {
        Transaction trans = null;
        Subject subj = null;
        ObjectPool pool = null;

        // Look up the current Subject & Transaction
        try {
            if(tm != null && tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                trans = tm.getTransaction();
            }
        } catch(SystemException e) {}
        if(sec != null) {
            subj = sec.getSubject(factory, pools.getPoolName(factory));
        }

        // Check for a pool
        pool = pools.getPool(factory, subj, info);

        // Check whether there's already a connection for this factory
        // for this transaction.
        Object txCache = null;
        if(trans != null) {
            txCache = txConnections.get(trans);
            if(txCache instanceof TxCacheKey) { // Simple case: one factory for this trans
                TxCacheKey key = (TxCacheKey)txCache;
                if(key.factory.equals(factory)) {
                    key.listener.addHandle();
                    return key.connection.getConnection(subj, info);
                }
            } else if(txCache instanceof List) { // Hard case: more factories for this trans
                List list = (List)txCache;
                for(int i=list.size()-1; i>= 0; i--) {
                    TxCacheKey key = (TxCacheKey)list.get(i);
                    if(key.factory.equals(factory)) {
                        key.listener.addHandle();
                        return key.connection.getConnection(subj, info);
                    }
                }
            }
        }

        // Get a connection (from pool or otherwise)
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
        
        if (con == null)
            throw new ResourceException("Pool exhausted, not blocking!");

        // Register the connection with the Tx Cache & the Transaction
        ConnectionListener listener = null;
        if(trans != null) {
            LocalTransaction ltx = con.getLocalTransaction();
            listener = new SharedLocalConnectionListener(pool, con, trans, ltx);
            TxCacheKey newKey = new TxCacheKey(factory, con, listener);
            if(txCache == null) {
                txConnections.put(trans, newKey);
            } else if(txCache instanceof TxCacheKey) {
                LinkedList list = new LinkedList();
                list.add(txCache);
                list.add(newKey);
                txConnections.put(trans, list);
            } else if(txCache instanceof List) {
                List list = (List)txCache;
                list.add(newKey);
            }
        } else {
            listener = new NoTransactionListener(pool, con);
        }

        // Add the connection/transaction listener
        try {
            listener.register();
            listener.addHandle();
        } catch(SystemException e) {
            throw new ApplicationServerInternalException("Unable to register Local TX with JTA TX: "+e.getMessage());
        } catch(RollbackException e) {
            throw new ApplicationServerInternalException("Unable to register Local TX with JTA TX: TX is rolled back.");
        }

        // Extract the Connection object
        Object handle = con.getConnection(subj, info);
        connectionHandleIssued(handle, listener);
        return handle;
    }

    /**
     * Enlist a previously checked-out connection in the current transaction
     */
    public void enlistExistingConnection(Object connection) throws ResourceException {
        Transaction trans = null;

        // Look up the current Transaction
        try {
            if(tm != null && tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                trans = tm.getTransaction();
            }
        } catch(SystemException e) {}

        if(trans != null) {
            ConnectionListener listener = (ConnectionListener)handles.get(connection);
            if(listener instanceof NoTransactionListener) {
                ObjectPool pool = listener.getPool();
                ManagedConnection con = listener.getManagedConnection();
                listener.clear(); // Get rid of old listener
                LocalTransaction local = con.getLocalTransaction();
                listener = new SharedLocalConnectionListener(pool, con, trans, local);
            } else if(listener instanceof SharedLocalConnectionListener) {
                ManagedConnection con = listener.getManagedConnection();
                LocalTransaction local = con.getLocalTransaction();
                ((SharedLocalConnectionListener)listener).updateTransaction(trans, local);
            } else {
                throw new ResourceException("Invalid connection "+connection+" for ConnectionManager "+this);
            }
            try {
                listener.enlist();
            } catch(Exception e) {
                throw new ResourceException("Unable to register listeners for connection: "+e, e);
            }
        } else {
            // Why do we care?  Old trans is over, work will be lost.
        }
    }

    /**
     * Dispose of cached connections.
     */
    public void shutDown() {
        txConnections.clear();
        txConnections = null;
        super.shutDown();
    }

    private static class TxCacheKey {
        public ManagedConnectionFactory factory;
        public ManagedConnection connection;
        public ConnectionListener listener;

        public TxCacheKey(ManagedConnectionFactory factory, ManagedConnection connection, ConnectionListener listener) {
            this.factory = factory;
            this.connection = connection;
            this.listener = listener;
        }

        public boolean equals(Object o) {
            TxCacheKey key = (TxCacheKey)o;
            return key.factory == factory && key.connection == connection;
        }

        public int hashCode() {
            return factory.hashCode() ^ connection.hashCode();
        }
    }
}
