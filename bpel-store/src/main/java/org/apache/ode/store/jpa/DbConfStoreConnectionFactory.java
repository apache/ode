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

package org.apache.ode.store.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.jpa.JpaTxMgrProvider;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.ConfStoreConnectionFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import java.util.HashMap;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class DbConfStoreConnectionFactory implements ConfStoreConnectionFactory {
    private static final Log __log = LogFactory.getLog(DbConfStoreConnectionFactory.class);

    private DataSource _ds;
    private EntityManagerFactory _emf;

    private TransactionManager _txMgr;

    @SuppressWarnings("unchecked")
    public DbConfStoreConnectionFactory(DataSource ds, boolean createDatamodel, String txFactoryClassName) {
        _ds = ds;
        initTxMgr(txFactoryClassName);

        HashMap<String, Object> propMap = new HashMap<String,Object>();
        propMap.put("openjpa.Log", "commons");
        propMap.put("openjpa.ManagedRuntime", new JpaTxMgrProvider(_txMgr));
        propMap.put("openjpa.ConnectionFactory", _ds);
        propMap.put("openjpa.ConnectionFactoryMode", "managed");
        propMap.put("openjpa.FlushBeforeQueries", "false");
        propMap.put("openjpa.FetchBatchSize", 1000);
        propMap.put("openjpa.jdbc.TransactionIsolation", "read-committed");

        if (createDatamodel) propMap.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=false)");

        _emf = Persistence.createEntityManagerFactory("ode-store", propMap);
    }

    @SuppressWarnings("unchecked")
    public ConfStoreConnection getConnection() {
        HashMap propMap2 = new HashMap();
        propMap2.put("openjpa.TransactionMode", "managed");
        return new ConfStoreConnectionJpa(_emf.createEntityManager(propMap2));
    }

    public void beginTransaction() {
        try {
            if(__log.isDebugEnabled()) __log.debug("begin transaction on " + _txMgr);
            _txMgr.begin();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void commitTransaction() {
        try {
            if(__log.isDebugEnabled()) __log.debug("commit transaction on " + _txMgr);
            _txMgr.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void rollbackTransaction() {
        try {
            if(__log.isDebugEnabled()) __log.debug("rollback transaction on " + _txMgr);
            _txMgr.rollback();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void initTxMgr(String txFactoryClassName) {
        __log.info("ProcessStore initializing transaction manager using " + txFactoryClassName);
        try {
            Class txFactClass = getClass().getClassLoader().loadClass(txFactoryClassName);
            Object txFact = txFactClass.newInstance();
            _txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
        } catch (Exception e) {
            __log.fatal("Couldn't initialize a transaction manager with factory: " + txFactoryClassName, e);
            throw new RuntimeException("Couldn't initialize a transaction manager with factory: " + txFactoryClassName, e);
        }
    }
}