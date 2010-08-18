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
package org.apache.ode.store;

import junit.framework.TestCase;

import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.dao.store.DeploymentUnitDAO;
import org.apache.ode.dao.store.ProcessConfDAO;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.il.txutil.TxManager;
import java.util.Properties;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

public class DaoTest extends TestCase {

    ConfStoreDAOConnectionFactory _cf;
    
    Database _db;
    
    TransactionManager _txm;

    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(OdeConfigProperties.PROP_DAOCF_STORE,System.getProperty(OdeConfigProperties.PROP_DAOCF_STORE,OdeConfigProperties.DEFAULT_DAOCF_STORE_CLASS));
        OdeConfigProperties odeProps = new OdeConfigProperties(props, "");
        TxManager tx = new TxManager(odeProps);
        _txm = tx.createTransactionManager();
        _db = new Database(odeProps);
        _db.setTransactionManager(_txm);
        _db.start();
        _cf = _db.createDaoStoreCF();
    }

    public void tearDown() throws Exception {
        _cf.shutdown();
        _db.shutdown();
    }

    public void testEmpty() throws Exception {
        ConfStoreDAOConnection conn = _cf.getConnection();
        _txm.begin();
        assertEquals(0, conn.getDeploymentUnits().size());
        assertNull(conn.getDeploymentUnit("foobar"));
        _txm.commit();
        conn.close();
    }

    public void testCreateDU() throws Exception{
        ConfStoreDAOConnection conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
            assertNotNull(du.getDeployDate());
        } finally {
            _txm.commit();
            conn.close();
        }

        conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
        } finally {
            _txm.commit();
        }

    }

    public void testRollback() throws Exception {
        ConfStoreDAOConnection conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
            assertNotNull(du.getDeployDate());
        } finally {
            _txm.rollback();
            conn.close();
        }

        conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
            assertNull(du);
        } finally {
            _txm.commit();
        }
    }
    
    public void testGetDeploymentUnits() throws Exception {
        ConfStoreDAOConnection conn = _cf.getConnection();
        _txm.begin();
        try {
            conn.createDeploymentUnit("foo1");
            conn.createDeploymentUnit("foo2");
            conn.createDeploymentUnit("foo3");
            conn.createDeploymentUnit("foo4");
        } finally {
            _txm.commit();
            conn.close();
        }

        conn = _cf.getConnection();
        _txm.begin();
        try {
            assertNotNull(conn.getDeploymentUnit("foo1"));
            assertNotNull(conn.getDeploymentUnit("foo2"));
            assertNotNull(conn.getDeploymentUnit("foo3"));
            assertNotNull(conn.getDeploymentUnit("foo4"));
            assertNull(conn.getDeploymentUnit("foo5"));
        } finally {
            _txm.commit();
        }
    }
    
    public void testCreateProcess() throws Exception {
        QName foobar = new QName("foo","bar");
        ConfStoreDAOConnection conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
            ProcessConfDAO p = du.createProcess(foobar,foobar,1);
            assertEquals(foobar,p.getPID());
            assertEquals(foobar,p.getType());
            assertNotNull(p.getDeploymentUnit());
            assertEquals("foo1", p.getDeploymentUnit().getName());
        } finally {
            _txm.commit();
            conn.close();
        }
        
        conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
            ProcessConfDAO p = du.getProcess(foobar);
            assertNotNull(p);
            assertNotNull(du.getProcesses());
            
            assertEquals(foobar,p.getPID());
            assertEquals(foobar,p.getType());
        } finally {
            _txm.commit();
            conn.close();
        }
    }
    
    public void testProcessProperties() throws Exception {
        QName foobar = new QName("foo","bar");
        ConfStoreDAOConnection conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
            ProcessConfDAO p = du.createProcess(foobar,foobar,1);
            p.setProperty(foobar,"baz");
        } finally {
            _txm.commit();
            conn.close();
        }
        
        conn = _cf.getConnection();
        _txm.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
            ProcessConfDAO p = du.getProcess(foobar);
            assertNotNull(p.getProperty(foobar));
            assertEquals("baz", p.getProperty(foobar));
            assertNotNull(p.getPropertyNames());
            assertTrue(p.getPropertyNames().contains(foobar));
        } finally {
            _txm.commit();
            conn.close();
        }
    }    
}