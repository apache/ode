package org.apache.ode.dao.jpa.test;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.dao.jpa.ojpa.*;
import org.apache.openjpa.persistence.ArgumentException;

import junit.framework.TestCase;

public class InsertObjectTest extends TestCase {
	
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
	
	public void testStart() throws Exception {
		
		BPELDAOConnectionFactoryImpl factory = new BPELDAOConnectionFactoryImpl();
		BpelDAOConnection conn = factory.getConnection();
		
		
		try {
		em.getTransaction().begin();
		em.persist(conn);
				
		em.flush();
		em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		
	}

	@Override
	protected void tearDown() throws Exception {
		em.close();
	}
	
	private void createMessageExchange(BpelDAOConnection conn ) {
		MessageExchangeDAO me = conn.createMessageExchange('0');
	}
	

}
