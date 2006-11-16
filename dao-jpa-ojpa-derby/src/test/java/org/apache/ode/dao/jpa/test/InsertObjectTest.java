package org.apache.ode.dao.jpa.test;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.ode.bpel.dao.BpelDAOConnection;
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
		
		if ( em.isOpen() ) {
			System.out.println("connection open");
		}
		

		
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
	}
	
	public void testStart() throws Exception {
		System.out.println("start test");
		String tmp = new String("some data");
		
		BPELDAOConnectionFactoryImpl factory = new BPELDAOConnectionFactoryImpl();
		BpelDAOConnection conn = factory.getConnection();
		
		
//	    Collection<MessageTest> tmp2 = ft.getFaultMessages();
//		ft.addMessage("Test Message");
//		ft.addProp("test1", "test prop 1");
	    

		//Collection<TestMessage> tmp = ft.g
		try {
		em.getTransaction().begin();
		em.persist(conn);
				
		em.flush();
		em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	protected void tearDown() throws Exception {
		em.close();
	}
	

}
