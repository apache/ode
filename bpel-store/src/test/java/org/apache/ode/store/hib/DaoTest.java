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
package org.apache.ode.store.hib;

import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.il.dbutil.DatabaseConfigException;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.ConfStoreConnectionFactory;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;

public class DaoTest extends TestCase {
    protected BpelDAOConnection daoConn;
    protected TransactionManager txm;
    private DataSource ds;
    private Database db;
    ConfStoreConnectionFactory cf;

    protected DataSource getDataSource() throws DatabaseConfigException {
        if (ds == null) {
            Properties props = new Properties();
            props.setProperty(OdeConfigProperties.PROP_DAOCF, System.getProperty(OdeConfigProperties.PROP_DAOCF, OdeConfigProperties.DEFAULT_DAOCF_CLASS));
            OdeConfigProperties odeProps = new OdeConfigProperties(props,"");
            db = Database.create(odeProps);
            db.setTransactionManager(txm);
            db.start();
            this.ds = db.getDataSource();
        }
        return ds;
    }

    public void setUp() throws Exception {
        EmbeddedGeronimoFactory factory = new EmbeddedGeronimoFactory();
        txm = factory.getTransactionManager();
        ds = getDataSource();
        org.springframework.mock.jndi.SimpleNamingContextBuilder.emptyActivatedContextBuilder().bind("java:comp/UserTransaction", txm);
        txm.begin();

        cf = new DbConfStoreConnectionFactory(ds, new Properties(), true, OdeConfigProperties.DEFAULT_TX_FACTORY_CLASS_NAME);
    }

    public void tearDown() throws Exception {
        db.shutdown();
    }

    public void testEmpty() {
        cf.beginTransaction();
        ConfStoreConnection conn = cf.getConnection();
        assertEquals(0, conn.getDeploymentUnits().size());
        assertNull(conn.getDeploymentUnit("foobar"));
        cf.commitTransaction();
    }

    public void testCreateDU() {
        cf.beginTransaction();
        ConfStoreConnection conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
            assertNotNull(du.getDeployDate());
        } finally {
            cf.commitTransaction();
        }

        cf.beginTransaction();
        conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
        } finally {
            cf.commitTransaction();
        }

    }

    public void testRollback() {
        cf.beginTransaction();
        ConfStoreConnection conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
            assertNotNull(du.getDeployDate());
        } finally {
            cf.rollbackTransaction();
        }

        cf.beginTransaction();
        conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
            assertNull(du);
        } finally {
            cf.commitTransaction();
        }
    }

    public void testGetDeploymentUnits() {
        cf.beginTransaction();
        ConfStoreConnection conn = cf.getConnection();
        try {
            conn.createDeploymentUnit("foo1");
            conn.createDeploymentUnit("foo2");
            conn.createDeploymentUnit("foo3");
            conn.createDeploymentUnit("foo4");
        } finally {
            cf.commitTransaction();
        }

        cf.beginTransaction();
        conn = cf.getConnection();
        try {
            assertNotNull(conn.getDeploymentUnit("foo1"));
            assertNotNull(conn.getDeploymentUnit("foo2"));
            assertNotNull(conn.getDeploymentUnit("foo3"));
            assertNotNull(conn.getDeploymentUnit("foo4"));
            assertNull(conn.getDeploymentUnit("foo5"));
        } finally {
            cf.commitTransaction();
        }
    }

    public void testCreateProcess() {
        QName foobar = new QName("foo","bar");
        cf.beginTransaction();
        ConfStoreConnection conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
            ProcessConfDAO p = du.createProcess(foobar,foobar,1);
            assertEquals(foobar,p.getPID());
            assertEquals(foobar,p.getType());
            assertNotNull(p.getDeploymentUnit());
            assertEquals("foo1", p.getDeploymentUnit().getName());
        } finally {
            cf.commitTransaction();
        }

        cf.beginTransaction();
        conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
            ProcessConfDAO p = du.getProcess(foobar);
            assertNotNull(p);
            assertNotNull(du.getProcesses());

            assertEquals(foobar,p.getPID());
            assertEquals(foobar,p.getType());
        } finally {
            cf.commitTransaction();
        }
    }

    public void testProcessProperties() {
        QName foobar = new QName("foo","bar");
        cf.beginTransaction();
        ConfStoreConnection conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
            ProcessConfDAO p = du.createProcess(foobar,foobar,1);
            p.setProperty(foobar,"baz");
        } finally {
            cf.commitTransaction();
        }

        cf.beginTransaction();
        conn = cf.getConnection();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
            ProcessConfDAO p = du.getProcess(foobar);
            assertNotNull(p.getProperty(foobar));
            assertEquals("baz", p.getProperty(foobar));
            assertNotNull(p.getPropertyNames());
            assertTrue(p.getPropertyNames().contains(foobar));
        } finally {
            cf.commitTransaction();
        }
    }
}