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
package org.apache.ode.il.txutil;

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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.il.config.OdeConfigProperties;


/**
 * Does the dirty work of setting up / obtaining a TransactionManager based on the configuration in the {@link OdeConfigProperties} object.
 *
 */
public class TxManager {
    private static final Log __log = LogFactory.getLog(TxManager.class);

    private OdeConfigProperties _odeConfig;
    private boolean _debugTxn = false;


    public TxManager(OdeConfigProperties props) {
        if (props == null)
            throw new NullPointerException("Must provide a configuration.");

        _odeConfig = props;
        _debugTxn=Boolean.valueOf(_odeConfig.getProperty("tx.debug","false"));
    }


    public TransactionManager createTransactionManager() throws TransactionConfigException  {
        String txFactoryName = _odeConfig.getTxFactoryClass();
        __log.debug("Initializing transaction manager using " + txFactoryName);
        try {
            Class txFactClass = Class.forName(txFactoryName);
            Object txFact = txFactClass.newInstance();
            TransactionManager txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
            if (__log.isDebugEnabled() && _debugTxn)
                txMgr = new DebugTxMgr(txMgr);
            return txMgr;
        } catch (Exception e) {
            __log.fatal("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
            throw new TransactionConfigException("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
        }
    }


    // Transactional debugging stuff, to track down all these little annoying bugs.
    public static class DebugTxMgr implements TransactionManager {
        protected TransactionManager _tm;

        public DebugTxMgr(TransactionManager tm) {        
                _tm = tm;
        }

        public void begin() throws NotSupportedException, SystemException {
            __log.debug("Txm begin");
            _tm.begin();
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            __log.debug("Txm commit");
            for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) {
                __log.debug(traceElement.toString());
            }
            _tm.commit();
        }

        public int getStatus() throws SystemException {
            __log.debug("Txm status");
            return _tm.getStatus();
        }

        public Transaction getTransaction() throws SystemException {
            Transaction tx = _tm.getTransaction();
            __log.debug("Txm get tx " + tx);
            return tx == null ? null : new DebugTx(tx);
        }

        public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
            __log.debug("Txm resume");
            _tm.resume(transaction);
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            __log.debug("Txm rollback");
            _tm.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            __log.debug("Txm set rollback");
            _tm.setRollbackOnly();
        }

        public void setTransactionTimeout(int i) throws SystemException {
            __log.debug("Txm set tiemout " + i);
            _tm.setTransactionTimeout(i);
        }

        public Transaction suspend() throws SystemException {
            __log.debug("Txm suspend");
            return _tm.suspend();
        }
    }

    public static class DebugTx implements Transaction {
        private Transaction _tx;

        public DebugTx(Transaction tx) {
            _tx = tx;
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
            __log.debug("Tx commit");
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
            __log.debug("Tx rollback");
            _tx.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            __log.debug("Tx set rollback");
            _tx.setRollbackOnly();
        }
    }

}
