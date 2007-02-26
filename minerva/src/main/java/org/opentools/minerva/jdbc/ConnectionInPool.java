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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opentools.minerva.pool.PoolEvent;
import org.opentools.minerva.pool.PoolEventListener;
import org.opentools.minerva.pool.PooledObject;

/**
 * Wrapper for database connections in a pool. Handles closing appropriately. The connection is returned to the pool rather than
 * truly closing, any outstanding statements are closed, and the connection is rolled back. This class is also used by statements,
 * etc. to update the last used time for the connection.
 * 
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @author Maciej Szefler m s z e f l e r ( at ) g m a i l . c o m 
 */
public class ConnectionInPool implements PooledObject, ConnectionWrapper {
    private final static String CLOSED = "Connection has been closed!";

    private static final String CONCURRENT = "Concurrent access to connection not permitted!";

    private static final Class[] RESULT_SET_CLASS = { ResultSet.class };

    /** The actual (underlying) connection. */
    protected volatile Connection _con;

    /** Has the object been invalidated? */
    private volatile boolean _invalid = false;

    /** Listeners. */
    private CopyOnWriteArrayList<PoolEventListener> _listeners = new CopyOnWriteArrayList<PoolEventListener>();

    /** Statements/result sets created by this connection wrapper. */
    private Set<ManagedObject> _managedObjects = new HashSet<ManagedObject>();

    /** Work lock: detect concurrent use of connection. */
    private AtomicBoolean _spinlock = new AtomicBoolean(false);

    /**
     * Creates a new connection wrapper.
     * 
     * @param con the "real" database connection to wrap.
     */
    public ConnectionInPool(Connection con) {
        this._con = con;
    }

    /**
     * Adds a listener for pool events.
     */
    public void addPoolEventListener(PoolEventListener listener) {
        _listeners.add(listener);
    }

    /**
     * Removes a listener for pool events.
     */
    public void removePoolEventListener(PoolEventListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Gets a reference to the "real" connection. This should only be used if you need to cast that to a specific type to call a
     * proprietary method - you will defeat all the pooling if you use the underlying connection directly.
     */
    public Connection getUnderlyingConnection() {
        return _con;
    }

    /**
     * Updates the last used time for this connection to the current time.
     */
    public void setLastUsed() {
        firePoolEvent(new PoolEvent(this, PoolEvent.OBJECT_USED));
    }

    /**
     * Indicates that an error occured on this connection.
     */
    public void setError(SQLException e) {
        firePoolEvent(new PoolEvent(this, PoolEvent.OBJECT_ERROR));
    }

    /**
     * Indicates that an error occured on this connection.
     */
    public void setCatastrophicError(SQLException e) {
        PoolEvent pe = new PoolEvent(this, PoolEvent.OBJECT_ERROR);
        pe.setCatastrophic();
        firePoolEvent(pe);
    }


    /**
     * Prepares a connection to be returned to the pool. All outstanding statements are closed, and if AutoCommit is off, the
     * connection is rolled back. No further SQL calls are possible once this is called. This method may be invoked outside the
     * normal application thread (typically by the transaction manager synchronizer).
     */
    public void invalidate() throws SQLException {
        if (_invalid)
            return;

        _invalid = true; // this will prevent any further invocations.

        try {
            // Grab the lock, no waiting, if there is some rogue thread holding on to this
            // connection, we cannot return to the pool.
            if (!_spinlock.compareAndSet(false, true)) {
                // throwing the exception will cause this connection to be purged from the pool.
                throw new SQLException(CONCURRENT);
            }

            // We have the lock, we're safe, can clean up properly: close all the statements/results sets
            for (ManagedObject s : _managedObjects)
                s.invalidate();

            if (!_con.getAutoCommit()) {
                _con.rollback();
            }
        } finally {
            _con = null;
        }
    }

    /**
     * Dispatches an event to the listeners.
     */
    protected void firePoolEvent(PoolEvent evt) {
        for (PoolEventListener pel : _listeners) {
            if (evt.getType() == PoolEvent.OBJECT_CLOSED)
                pel.objectClosed(evt);
            else if (evt.getType() == PoolEvent.OBJECT_ERROR)
                pel.objectError(evt);
            else
                pel.objectUsed(evt);
        }
    }

    // ---- Implementation of java.sql.Connection ----
    public Statement createStatement() throws SQLException {
        enter();
        try {
            return proxy(_con.createStatement(), Statement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareCall(sql), CallableStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        enter();
        try {
            return _con.nativeSQL(sql);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        enter();
        try {
            _con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public boolean getAutoCommit() throws SQLException {
        enter();
        try {
            return _con.getAutoCommit();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void commit() throws SQLException {
        enter();
        try {
            _con.commit();
        } catch (SQLException e) {
            setCatastrophicError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void rollback() throws SQLException {
        enter();
        try {
            _con.rollback();
        } catch (SQLException e) {
            setCatastrophicError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void close() throws SQLException {
        // IMPORTANT: do not call enter()/exit() in this method, doing so will cause deadlock
        // due to invalidate() being called via callback.

        if (_con == null || _invalid)
            throw new SQLException(CLOSED);

        firePoolEvent(new PoolEvent(this, PoolEvent.OBJECT_CLOSED));

        _con = null;
        _listeners = null;
    }

    public boolean isClosed() throws SQLException {
        Connection con = _con; // avoid NPE if _con changes between null test and isClosed() call.

        if (con == null)
            return true;
        try {
            return con.isClosed();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        enter();
        try {
            return _con.getMetaData();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        enter();
        try {
            _con.setReadOnly(readOnly);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public boolean isReadOnly() throws SQLException {
        enter();
        try {
            return _con.isReadOnly();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void setCatalog(String catalog) throws SQLException {
        enter();
        try {
            _con.setCatalog(catalog);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public String getCatalog() throws SQLException {
        enter();
        try {
            return _con.getCatalog();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void setTransactionIsolation(int level) throws SQLException {
        enter();
        try {
            _con.setTransactionIsolation(level);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public int getTransactionIsolation() throws SQLException {
        enter();
        try {
            return _con.getTransactionIsolation();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        enter();
        try {
            return _con.getWarnings();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void clearWarnings() throws SQLException {
        enter();
        try {
            _con.clearWarnings();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        enter();
        try {
            return proxy(_con.createStatement(resultSetType, resultSetConcurrency), Statement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareStatement(sql, resultSetType, resultSetConcurrency), PreparedStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareCall(sql, resultSetType, resultSetConcurrency), CallableStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        enter();
        try {
            return _con.getTypeMap();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        enter();
        try {
            _con.setTypeMap(map);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public int getHoldability() throws SQLException {
        enter();
        try {
            return _con.getHoldability();

        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void setHoldability(int holdability) throws SQLException {
        enter();
        try {
            _con.setHoldability(holdability);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public Savepoint setSavepoint() throws SQLException {
        enter();
        try {
            return _con.setSavepoint();
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        enter();
        try {
            _con.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        enter();
        try {
            _con.rollback(savepoint);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        enter();
        try {
            return proxy(_con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), Statement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }

    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        enter();
        try {
            return proxy(_con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), CallableStatement.class);

        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareStatement(sql, autoGeneratedKeys), PreparedStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }

    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        enter();
        try {
            return proxy(_con.prepareStatement(sql, resultSetType, resultSetConcurrency), PreparedStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareStatement(sql, columnIndexes), PreparedStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        enter();
        try {
            return _con.setSavepoint(name);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareStatement(sql, columnNames), PreparedStatement.class);
        } catch (SQLException e) {
            setError(e);
            throw e;
        } finally {
            exit();
        }
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String)
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        enter();
        try {
            return proxy(_con.prepareStatement(sql), PreparedStatement.class);
        } finally {
            exit();
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Build a managed proxy for a JDBC Statement-type object.
     */
    private <ST extends Statement> ST proxy(ST st, Class<ST> cls) throws SQLException {
        StatementInvocationHandler sih = new StatementInvocationHandler(st);
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { cls }, sih);
        _managedObjects.add(sih);
        return (ST) proxy;
    }

    private ResultSet proxy(ResultSet rs) throws SQLException {
        ResultSetInvocationHandler sih = new ResultSetInvocationHandler(rs);
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), RESULT_SET_CLASS, sih);
        _managedObjects.add(sih);
        return (ResultSet) proxy;
    }

    protected void enter() throws SQLException {
        if (_spinlock.compareAndSet(false, true)) {
            // Got lock, check if we are ok to proceed
            if (_con == null || _invalid) {
                // remember, we need to release the lock.
                _spinlock.set(false);
                throw new SQLException(CLOSED);
            }
        } else {
            // couldn't get lock. indicates concurrent access attempt.
            throw new SQLException(CONCURRENT);
        }
    }

    protected void exit() {
        if (!_spinlock.compareAndSet(true, false))
            throw new IllegalStateException("INTERNAL ERROR: Releasing unheld lock!");
    }

    private abstract class ManagedObject implements InvocationHandler {
        protected Object _target;

        protected ManagedObject(Object target) {
            _target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            enter();
            try {
                if (_target == null)
                    throw new SQLException(CLOSED);

                Object ret = method.invoke(_target, args);

                if (method.getName().equals("close")) {
                    _target = null;
                    _managedObjects.remove(this);
                }
                    

                if (ret instanceof ResultSet)
                    return proxy((ResultSet) ret);
                if (ret instanceof PreparedStatement)
                    return proxy((PreparedStatement) ret, PreparedStatement.class);
                if (ret instanceof CallableStatement)
                    return proxy((CallableStatement) ret, CallableStatement.class);
                if (ret instanceof Statement)
                    return proxy((Statement) ret, Statement.class);
                return ret;
            } catch (InvocationTargetException ite) {
                Throwable ex = ite.getTargetException();
                if (ex instanceof SQLException)
                    setError((SQLException) ex);
                throw ex;
            } finally {
                exit();
            }
        }

        abstract void invalidate();

    }

    private class StatementInvocationHandler extends ManagedObject {

        public StatementInvocationHandler(Statement target) {
            super(target);
        }

        public void invalidate() {
            try {
                ((Statement) _target).close();

            } catch (Exception ex) {
                ;
                ; // not much we can do here.
            }
        }

    }

    private class ResultSetInvocationHandler extends ManagedObject {

        public ResultSetInvocationHandler(ResultSet target) {
            super(target);
        }

        public void invalidate() {
            try {
                ((ResultSet) _target).close();
            } catch (Exception ex) {
                ;
                ; // not much we can do here.
            }
        }
    }
}
