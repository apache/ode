package org.apache.ode.dao.jpa.test;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.dao.jpa.ojpa.BPELDAOConnectionFactoryImpl;

import junit.framework.TestCase;

public class SelectObjectTest extends TestCase {
	
	private EntityManager em;
	private static final String TEST_NS = "http://org.apache.ode.jpa.test";
	private String[] correlationKeys = { "key1", "key2" };
	private CorrelationKey key1 = new CorrelationKey(1,correlationKeys);


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
		BpelDAOConnection conn = factory.getConnection();
		
		ProcessDAO p = conn.getProcess(new QName(TEST_NS,"testPID1"));
		Collection<ProcessInstanceDAO> insts = p.findInstance(key1);
		
		for ( ProcessInstanceDAO inst : insts ) {
			Long id = inst.getInstanceId();
		}
		
		int instCount = p.getNumInstances();
		
		
		
		conn.close();
		

	}

	@Override
	protected void tearDown() throws Exception {
		em.close();
	}
	
}
