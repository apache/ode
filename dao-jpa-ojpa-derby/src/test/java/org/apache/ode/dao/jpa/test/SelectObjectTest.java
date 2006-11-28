package org.apache.ode.dao.jpa.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.dao.jpa.ojpa.BPELDAOConnectionFactoryImpl;

import junit.framework.TestCase;

public class SelectObjectTest extends TestCase {
	
	private EntityManager em;

	@Override
	protected void setUp() throws Exception {
		
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("ode-unit-test");
			em = emf.createEntityManager();
		
		} catch ( Exception e ) {
			e.printStackTrace();
			fail();
		}
		
		
	}
	
	public void testGetObject() throws Exception {
		
		BPELDAOConnectionFactoryImpl factory = new BPELDAOConnectionFactoryImpl(em);
		BpelDAOConnection conn1 = factory.getConnection();
		Long connId1 = factory.getConnectionId(conn1);
		BpelDAOConnection conn2 = factory.getConnection(connId1 + 5);
		Long connId2 = factory.getConnectionId(conn2);
		
		if ( conn1 != null && conn2 != null	 ) {
			System.out.println("got conn1:" + connId1);
			System.out.println("got conn2:" + connId2);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		em.close();
	}
	
}
