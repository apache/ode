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
package org.apache.ode.dao.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.openjpa.ee.ManagedRuntime;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BPELDAOConnectionFactoryImpl implements BpelDAOConnectionFactoryJDBC {
    static final Log __log = LogFactory.getLog(BPELDAOConnectionFactoryImpl.class);

    private EntityManagerFactory _emf;
    private TransactionManager _tm;
    private DataSource _ds;
    private Object _dbdictionary;

    static ThreadLocal<BPELDAOConnectionImpl> _connections = new ThreadLocal<BPELDAOConnectionImpl>();

    public BPELDAOConnectionFactoryImpl() {
    }

    public BpelDAOConnection getConnection() {
        try {
            _tm.getTransaction().registerSynchronization(new Synchronization() {
                // OpenJPA allows cross-transaction entity managers, which we don't want
                public void afterCompletion(int i) {
                    if (_connections.get() != null)
                        _connections.get().getEntityManager().close();
                    _connections.set(null);
                }
                public void beforeCompletion() { }
            });
        } catch (RollbackException e) {
            throw new RuntimeException("Coulnd't register synchronizer!");
        } catch (SystemException e) {
            throw new RuntimeException("Coulnd't register synchronizer!");
        }
        if (_connections.get() != null) {
            return _connections.get();
        } else {
            HashMap propMap2 = new HashMap();
            propMap2.put("openjpa.TransactionMode", "managed");
            EntityManager em = _emf.createEntityManager(propMap2);
            BPELDAOConnectionImpl conn = new BPELDAOConnectionImpl(em);
            _connections.set(conn);
            return conn;
        }
    }

    public void init(Properties properties) {
        HashMap<String, Object> propMap = new HashMap<String,Object>();

//        propMap.put("openjpa.Log", "DefaultLevel=TRACE");
        propMap.put("openjpa.Log", "log4j");
//        propMap.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");

        propMap.put("openjpa.ManagedRuntime", new TxMgrProvider());
        propMap.put("openjpa.ConnectionFactory", _ds);
        propMap.put("openjpa.ConnectionFactoryMode", "managed");
        propMap.put("openjpa.FlushBeforeQueries", "false");

        if (_dbdictionary != null)
            propMap.put("openjpa.jdbc.DBDictionary", _dbdictionary);

        if (properties != null)
            for (Map.Entry me : properties.entrySet())
                propMap.put((String)me.getKey(),me.getValue());

        _emf = Persistence.createEntityManagerFactory("ode-dao", propMap);
    }

    public void setTransactionManager(TransactionManager tm) {
        _tm = tm;
    }

    public void setDataSource(DataSource datasource) {
        _ds = datasource;

    }

    public void setDBDictionary(String dbd) {
        _dbdictionary = dbd;
    }

    public void setTransactionManager(Object tm) {
        _tm = (TransactionManager) tm;

    }

    public void setUnmanagedDataSource(DataSource ds) {
    }

    public void shutdown() {
        _emf.close();
    }


    private class TxMgrProvider implements ManagedRuntime {
        public TxMgrProvider() {
        }

        public TransactionManager getTransactionManager() throws Exception {
            return _tm;
        }
        public void setRollbackOnly(Throwable cause) throws Exception {
            // there is no generic support for setting the rollback cause
            getTransactionManager().getTransaction().setRollbackOnly();
        }
        public Throwable getRollbackCause() throws Exception {
            // there is no generic support for setting the rollback cause
            return null;
        }
    }

    private class DebugTxMgr implements TransactionManager {
        private TransactionManager _tm;

        public DebugTxMgr(TransactionManager tm) {
            _tm = tm;
        }

        public void begin() throws NotSupportedException, SystemException {
            _tm.begin();
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            _tm.commit();
        }

        public int getStatus() throws SystemException {
            return _tm.getStatus();
        }

        public Transaction getTransaction() throws SystemException {
            Transaction tx = _tm.getTransaction();
            __log.debug("JPA get transaction" + tx);
            return new DebugTx(tx);
        }

        public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
            _tm.resume(transaction);
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            _tm.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            _tm.setRollbackOnly();
        }

        public void setTransactionTimeout(int i) throws SystemException {
            _tm.setTransactionTimeout(i);
        }

        public Transaction suspend() throws SystemException {
            return _tm.suspend();
        }
    }

    private class DebugTx implements Transaction {
        private Transaction _tx;

        public DebugTx(Transaction tx) {
            _tx = tx;
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
            _tx.commit();
        }

        public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
            return _tx.delistResource(xaResource, i);
        }

        public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
            return _tx.enlistResource(xaResource);
        }

        public int getStatus() throws SystemException {
            return _tx.getStatus();
        }

        public void registerSynchronization(Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
            __log.debug("Synchronization registration on " + synchronization.getClass().getName());
            _tx.registerSynchronization(synchronization);
        }

        public void rollback() throws IllegalStateException, SystemException {
            _tx.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            _tx.setRollbackOnly();
        }
    }
}
