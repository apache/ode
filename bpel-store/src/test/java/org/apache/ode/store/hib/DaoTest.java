package org.apache.ode.store.hib;

import junit.framework.TestCase;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.ConfStoreConnectionFactory;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.hsqldb.jdbc.jdbcDataSource;

import javax.xml.namespace.QName;

public class DaoTest extends TestCase {
    jdbcDataSource hsqlds;

    ConfStoreConnectionFactory cf;

    public void setUp() throws Exception {
        hsqlds = new jdbcDataSource();
        hsqlds.setDatabase("jdbc:hsqldb:mem:test");
        hsqlds.setUser("sa");
        hsqlds.setPassword("");

        cf = new DbConfStoreConnectionFactory(hsqlds, true);
    }

    public void tearDown() throws Exception {
        hsqlds.getConnection().createStatement().execute("SHUTDOWN");
    }

    public void testEmpty() {
        ConfStoreConnection conn = cf.getConnection();
        conn.begin();
        assertEquals(0, conn.getDeploymentUnits().size());
        assertNull(conn.getDeploymentUnit("foobar"));
        conn.commit();
        conn.close();
    }

    public void testCreateDU() {
        ConfStoreConnection conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
            assertNotNull(du.getDeployDate());
        } finally {
            conn.commit();
            conn.close();
        }

        conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
        } finally {
            conn.commit();
        }

    }

    public void testRollback() {
        ConfStoreConnection conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
            assertNotNull(du);
            assertEquals("foo", du.getName());
            assertNotNull(du.getDeployDate());
        } finally {
            conn.rollback();
            conn.close();
        }

        conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
            assertNull(du);
        } finally {
            conn.commit();
        }

    }
    
    public void testGetDeploymentUnits() {
        ConfStoreConnection conn = cf.getConnection();
        conn.begin();
        try {
            conn.createDeploymentUnit("foo1");
            conn.createDeploymentUnit("foo2");
            conn.createDeploymentUnit("foo3");
            conn.createDeploymentUnit("foo4");
        } finally {
            conn.commit();
            conn.close();
        }
        conn = cf.getConnection();
        conn.begin();
        try {
            assertNotNull(conn.getDeploymentUnit("foo1"));
            assertNotNull(conn.getDeploymentUnit("foo2"));
            assertNotNull(conn.getDeploymentUnit("foo3"));
            assertNotNull(conn.getDeploymentUnit("foo4"));
            assertNull(conn.getDeploymentUnit("foo5"));
        } finally {
            conn.commit();
        }
    }
    
    
    public void testCreateProcess() {
        QName foobar = new QName("foo","bar");
        ConfStoreConnection conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
            ProcessConfDAO p = du.createProcess(foobar,foobar);
            assertEquals(foobar,p.getPID());
            assertEquals(foobar,p.getType());
            assertNotNull(p.getDeploymentUnit());
            assertEquals("foo1", p.getDeploymentUnit().getName());
        } finally {
            conn.commit();
            conn.close();
        }
        
        conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
            ProcessConfDAO p = du.getProcess(foobar);
            assertNotNull(p);
            assertNotNull(du.getProcesses());
            
            assertEquals(foobar,p.getPID());
            assertEquals(foobar,p.getType());

        } finally {
            conn.commit();
            conn.close();
        }
        
    }
    
    public void testProcessProperties() {
        QName foobar = new QName("foo","bar");
        ConfStoreConnection conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
            ProcessConfDAO p = du.createProcess(foobar,foobar);
            p.setProperty(foobar,"baz");
        } finally {
            conn.commit();
            conn.close();
        }
        
        conn = cf.getConnection();
        conn.begin();
        try {
            DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
            ProcessConfDAO p = du.getProcess(foobar);
            assertNotNull(p.getProperty(foobar));
            assertEquals("baz", p.getProperty(foobar));
            assertNotNull(p.getPropertyNames());
            assertTrue(p.getPropertyNames().contains(foobar));
        } finally {
            conn.commit();
            conn.close();
        }
        
        
    }
    
}
