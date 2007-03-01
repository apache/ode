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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.transaction.*;
import javax.transaction.xa.XAResource;

import org.opentools.minerva.pool.ObjectPool;
import org.opentools.minerva.pool.PoolParameters;

/**
 * Abstract base class for ConnectionManager implementations.  This
 * handles connection pools, and provides listener implementations
 * for LocalTransactions, XAResources, and no transactions.
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
@SuppressWarnings("unchecked")
public abstract class BaseConnectionManager implements ConnectionManager {
    public final static String POOL_CONFIGURATION_KEY="PoolConfiguration";
    public final static String POOL_CONFIG_VALUE_PER_FACTORY="per-factory";
    public final static String POOL_CONFIG_VALUE_PER_USER="per-user";
    public final static boolean DEFAULT_POOL_PER_FACTORY=true;
    protected transient PoolManager pools;
    protected transient Map handles;
    protected transient TransactionManager tm;
    protected transient ServerSecurityManager sec;
    protected transient PrintWriter logger;

    public BaseConnectionManager() {
        pools = new PoolManager();
        handles = new HashMap();
    }

    /**
     * Sets the TransactionManager to use for this ConnectionManager.
     */
    public void setTransactionManager(TransactionManager tm) {
        this.tm = tm;
    }

    /**
     * Gets the TransactionManager used by this ConnectionManager.
     */
    public TransactionManager getTransactionManager() {
        return tm;
    }

    /**
     * Sets the security implementation to use for this ConnectionManager.
     */
    public void setSecurityManager(ServerSecurityManager sec) {
        this.sec = sec;
    }

    /**
     * Gets the security implementation used by this ConnectionManager.
     */
    public ServerSecurityManager getSecurityManager() {
        return sec;
    }

    /**
     * Sets the logger to use for this ConnectionManager.
     */
    public void setLogWriter(PrintWriter logger) {
        this.logger = logger;
    }

    /**
     * Gets the logger used by this ConnectionManager.
     */
    public PrintWriter getLogWriter() {
        return logger;
    }

    /**
     * Creates a new connection pool for the specified factory.  All
     * connections for the factory are pooled together, regardless
     * of security or connection parameters.  A default name is
     * generated for the pool.
     */
    public void createPerFactoryPool(ManagedConnectionFactory factory, PoolParameters params) {
        createPerFactoryPool(factory, params, factory.getClass().getName()+"-"+(pools.getFactoryCount()+1));
    }

    /**
     * Creates a new connection pool for the specified factory.  All
     * connections for the factory are pooled together, regardless
     * of security or connection parameters.  The provided name is
     * used for the pool.
     */
    public void createPerFactoryPool(ManagedConnectionFactory factory, PoolParameters params, String name) {
        pools.addPerFactoryPool(factory, new ManagedConnectionPoolFactory(factory), params, name);
    }

    /**
     * Creates a new connection pool for the specified factory.  A separate
     * pool is created for each factory/user combination, where a user is
     * represented either by a Subject or by a ConnectionRequestInfo object.
     * A default name is generated for the pool.
     */
    public void createPerUserPool(ManagedConnectionFactory factory, PoolParameters params) {
        createPerUserPool(factory, params, factory.getClass().getName()+"-"+(pools.getFactoryCount()+1));
    }

    /**
     * Creates a new connection pool for the specified factory.  A separate
     * pool is created for each factory/user combination, where a user is
     * represented either by a Subject or by a ConnectionRequestInfo object.
     * The provided name is used for the pool.
     */
    public void createPerUserPool(ManagedConnectionFactory factory, PoolParameters params, String name) {
        pools.addPerUserPool(factory, new ManagedConnectionPoolFactory(factory), params, name);
    }

    /**
     * Closes down this ConnectionManager.
     */
    public void shutDown() {
        pools.clear();
        handles.clear();
        pools = null;
        handles = null;
        tm = null;
        sec = null;
        if (logger != null)
            logger.println(getClass().getName()+" shut down.");
        logger = null;
    }

    /**
     * Notes that a connection was given out.  Subclasses can override, but
     * should call this in their implementation.
     */
    protected void connectionHandleIssued(Object handle, Object listener) {
        handles.put(handle, listener);
    }

    /**
     * Notes that a connection was closed.  Subclasses can override, but
     * should call this in their implementation.
     */
    protected void connectionHandleClosed(Object handle) {
        handles.remove(handle);
    }

    /**
     * Base class for listeners - handles share connection counting
     * and listener registration.
     */
    protected abstract class ConnectionListener implements ConnectionEventListener {
        private int handleCount = 0;
        protected ObjectPool pool;
        protected ManagedConnection con;

        protected ConnectionListener(ObjectPool pool, ManagedConnection con) {
            this.pool = pool;
            this.con = con;
            if(con == null)
                throw new IllegalArgumentException("How can you manage a null connection?");
        }

        /**
         * Initializes the connections with the transaction.
         */
        public abstract void enlist() throws SystemException, RollbackException;

        /**
         * Registers this listener on the appropriate objects
         * and initializes the connections with the transaction.
         * Generally calls enlist in addition to other work.
         */
        public abstract void register() throws SystemException, RollbackException;

        /**
         * Gets the ObjectPool for this listener.
         */
        public ObjectPool getPool() {
            return pool;
        }

        /**
         * Gets the ManagedConnection for this listener.
         */
        public ManagedConnection getManagedConnection() {
            return con;
        }

        /**
         * Notes that a new client handle has been generated.  This
         * is particularly important for shared-connection
         * implementations.
         */
        // Not synchronized because all shared connections are in the
        // same TX, thus the same or sequential threads.
        public void addHandle() {
            ++handleCount;
        }

        /**
         * Notes that a client handle has been closed.  Generally
         * the connection is not closed or returned to the pool until
         * all the handles are closed (or the transaction ends).
         */
        protected int removeHandle() {
            return --handleCount;
        }

        /**
         * Gets the number of outstanding connection handles.
         */
        protected int getHandleCount() {
            return handleCount;
        }

        /**
         * Cleanup this listener.
         */
        public void clear() {
            try { // Con may be destroyed
                con.removeConnectionEventListener(this);
            } catch(Exception e) {}
            pool = null;
            con = null;
        }
    }

    /**
     * A listener to use when there is no transaction.  The connection
     * is destroyed or returned to the pool when there's an error or
     * the client closes all handles to it.
     */
    // Probably won't be shared, since you usually share based on TX,
    // but who knows?
    protected class NoTransactionListener extends ConnectionListener {

        public NoTransactionListener(ObjectPool pool, ManagedConnection con) {
            super(pool, con);
        }

        /**
         * Nothing to do in this implementation.
         */
        public void enlist() throws SystemException, RollbackException {
        }

        /**
         * Adds this as a connection event listener.
         */
        public void register() throws SystemException, RollbackException {
            con.addConnectionEventListener(this);
        }

        /**
         * In the event of an error, it is assumed to be fatal and
         * the connection is destroyed and this listener is cleared.
         */
        public void connectionErrorOccurred(ConnectionEvent evt) {
            closeConnection(true);
            clear();
        }

        /**
         * Close when the last handle is closed.
         */
        public void connectionClosed(ConnectionEvent evt) {
            connectionHandleClosed(evt.getConnectionHandle());
            if(removeHandle() == 0) {
                // Last handle to shared MC has been closed
                closeConnection(false);
                clear();
            }
        }
        public void localTransactionStarted(ConnectionEvent evt) {
        }
        public void localTransactionCommitted(ConnectionEvent evt) {
        }
        public void localTransactionRolledback(ConnectionEvent evt) {
        }

        private void closeConnection(boolean error) {
            if(pool != null) {
                if(error) {
                    pool.markObjectAsInvalid(con);
                }
                pool.releaseObject(con);
            } else {
                try {
                    con.destroy();
                } catch(ResourceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Listener to use for shared access to connections using
     * LocalTransactions.  The connection is destroyed or returned to
     * the pool when the associated global transaction ends or an
     * error occurs.  The connection is not returned immediately on
     * close because some connections using LocalTransaction may not
     * be able to maintain separate state for separate transactions.
     */
    protected class SharedLocalConnectionListener extends ConnectionListener implements Synchronization, ConnectionEventListener {
        private Transaction trans;
        private LocalTransaction local;

        public SharedLocalConnectionListener(ObjectPool pool, ManagedConnection con, Transaction trans, LocalTransaction local) {
            super(pool, con);
            this.trans = trans;
            this.local = local;
            if(con == null)
                throw new IllegalArgumentException("How can you manage a null connection?");
            if(trans == null)
                throw new IllegalArgumentException("Not allowed to use a listener when there's no transaction!");
        }

        /**
         * Updates this listener to use a new Transaction.
         */
        public void updateTransaction(Transaction trans, LocalTransaction local) {
            this.trans = trans;
            this.local = local;
        }

        /**
         * Adds this as a transaction listener.
         */
        public void enlist() throws SystemException, RollbackException {
            try {
                local.begin();
            } catch(ResourceException e) {
                SystemException se = new SystemException("Unable to start LocalTransaction: "+e.getMessage());
                throw se;
            }
            trans.registerSynchronization(this);
        }

        /**
         * Adds this as a connection listener and transaction listener.
         */
        public void register() throws SystemException, RollbackException {
            enlist();
            con.addConnectionEventListener(this);
        }

        /**
         * Commit/Rollback the local transaction when the global
         * transaction ends.  Return the connection to the pool
         * (or destroy it if there's no pool).
         */
        public void afterCompletion(int status) {
            if(local == null) {
                // ConnectionError - this object has already been cleared
                // but there's no way to unregister the Synchronization
                return;
            }

            try {
                if(status == Status.STATUS_COMMITTED) {
                    local.commit();
                } else {
                    local.rollback();
                }
            } catch(ResourceException e) {
                // Dispose of the connection
                killConnection();
                throw new RuntimeException("Unable to complete LocalTransaction: "+e.getMessage(), e);
            }

            if(getHandleCount() <= 0) {
                if(pool != null) {
                    pool.releaseObject(con);
                } else {
                    try {
                        con.destroy();
                    } catch(ResourceException e) {
                        e.printStackTrace();
                    }
                }
                clear();
            } else {
                trans = null;
                local = null;
            }
        }

        public void beforeCompletion() {
        }

        /**
         * In the event of an error, it is assumed to be fatal and
         * the connection is destroyed and this listener is cleared.
         */
        public void connectionErrorOccurred(ConnectionEvent evt) {
            try {
                local.rollback();
            } catch(Exception e) {}

            // FIXME: Will close() be called?
            killConnection();
            clear();
        }

        /**
         * Only return connection to the pool if this is the last handle to
         * the current ManagedConnection, and there's no TX.  This could
         * be the case, for example, with a UserTransaction where the TX
         * was committed before the connection was closed.
         */
        public void connectionClosed(ConnectionEvent evt) {
            connectionHandleClosed(evt.getConnectionHandle());
            if(removeHandle() <= 0) {  // If this was the last handle...
                if(trans == null) {    // And the transaction is over...
                    if(pool != null) { // Put back in the pool...
                        pool.releaseObject(con);
                    } else {
                        try {          // Or get rid of entirely.
                            con.destroy();
                        } catch(ResourceException e) {
                            e.printStackTrace();
                        }
                    }
                    clear();
                }
            }
        }
        public void localTransactionStarted(ConnectionEvent evt) {
        }
        public void localTransactionCommitted(ConnectionEvent evt) {
        }
        public void localTransactionRolledback(ConnectionEvent evt) {
        }

        private void killConnection() {
            if(pool != null) {
                pool.markObjectAsInvalid(con);
                pool.releaseObject(con);
            } else {
                try {
                    con.destroy();
                } catch(ResourceException e) {
                    e.printStackTrace();
                }
            }
        }

        public void clear() {
            super.clear();
            trans = null;
            local = null;
        }
    }

    /**
     * Listener for connections using XAResources.  The connection
     * is returned to the pool when the last client handle is closed.
     * If there's no pool, the connection is destroyed when the
     * transaction ends.  This handles both shared and non-shared
     * connections, as long as the subclass calls addHandle
     * properly.
     */
    protected class XAListener extends ConnectionListener implements Synchronization, ConnectionEventListener {
        private XAResource xar;
        private Transaction trans;

        public XAListener(ObjectPool pool, ManagedConnection con, Transaction trans, XAResource xar) {
            super(pool, con);
            this.trans = trans;
            this.xar = xar;
            if(con == null)
                throw new IllegalArgumentException("How can you manage a null connection?");
            if(xar == null || trans == null)
                throw new IllegalArgumentException("Not allowed to use a listener when there's no transaction!");
        }

        public void enlist() throws SystemException, RollbackException {
            trans.enlistResource(xar);
            if(pool == null) {
                // Don't need this unless the connection is not pooled.
                trans.registerSynchronization(this);
            }
        }

        public void register() throws SystemException, RollbackException {
            enlist();
            con.addConnectionEventListener(this);
        }

        public void updateTransaction(Transaction trans, XAResource xaRes) {
            this.trans = trans;
            this.xar = xaRes;
        }

        /**
         * Only called when the connection is not pooled,
         * so we go ahead and destroy it.  This may be called after
         * this object is cleared because there's no way to remove
         * a Synchronization in case of a connection error.
         */
        public void afterCompletion(int status) {
            if(getHandleCount() > 0) {
                trans = null;
                xar = null;
            } else {
                if(pool == null && con != null) {
                    try {
                        con.destroy();
                    } catch(ResourceException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void beforeCompletion() {
        }

        /**
         * In the event of an error, it is assumed to be fatal and
         * the connection is destroyed and this listener is cleared.
         */
        public void connectionErrorOccurred(ConnectionEvent evt) {
            // FIXME: Should we delist here?  Will close() be called?
            try {
                trans.delistResource(xar, XAResource.TMFAIL);
            } catch(SystemException e) {
                e.printStackTrace();
            }
            if(pool != null) {
                pool.markObjectAsInvalid(con);
                pool.releaseObject(con);
            } else {
                try {
                    con.destroy();
                } catch(ResourceException e) {
                    e.printStackTrace();
                }
            }

            clear();
        }

        /**
         * On the last client handle close, the connection
         * is delisted and returned to the pool.  If there's no
         * pool, we'll wait until the transaction is over.
         */
        public void connectionClosed(ConnectionEvent evt) {
            connectionHandleClosed(evt.getConnectionHandle());
            if(removeHandle() > 0) {
                return;
            }

            // XAResource is delisted
            if(trans != null) {
                // Delist if closing before transaction was ended
                try {
                    trans.delistResource(xar, XAResource.TMSUCCESS);
                } catch(SystemException e) {
                    e.printStackTrace();
                }
            }

            // Clean up if pooled or if transaction is over
            if(pool != null) {
                pool.releaseObject(con);
                clear();
            } else if (trans == null) {
                try {
                    con.destroy();
                } catch(ResourceException e) {
                    e.printStackTrace();
                }
                clear();
            } else {
                // No pool and active transaction - Connection will be
                // destroyed when transaction is over
            }
        }

        public void localTransactionStarted(ConnectionEvent evt) {
        }
        public void localTransactionCommitted(ConnectionEvent evt) {
        }
        public void localTransactionRolledback(ConnectionEvent evt) {
        }

        public void clear() {
            super.clear();
            trans = null;
            xar = null;
        }
    }
}
