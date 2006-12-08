package org.apache.ode.dao.jpa.ojpa;


import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.dao.jpa.BPELDAOConnectionImpl;

public class BPELDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

	private EntityManager em;
	
	public BPELDAOConnectionFactoryImpl(EntityManager em) {
		this.em = em;
	}
	
	public BpelDAOConnection getConnection() {
		
		List<BpelDAOConnection> conns = null;
		
		Query q = em.createQuery("SELECT x FROM BPELDAOConnectionImpl x order by x._id asc");
		
		try {
			conns = (List<BpelDAOConnection>)q.getResultList();
			
		} catch (NoResultException e) {
			return new BPELDAOConnectionImpl(new Long(1),em);
		}
		
		if ( conns.size() < 1 ) {
			return new BPELDAOConnectionImpl(new Long(1),em);
		}
		
		BPELDAOConnectionImpl conn = (BPELDAOConnectionImpl)conns.get(conns.size()-1);
		conn.setEntityManger(em);
		
		return conn;
	}
	
	public BpelDAOConnection getConnection(Long connID) {
		BPELDAOConnectionImpl conn = null;
		
		Query q = em.createQuery("SELECT x FROM BPELDAOConnectionImpl x WHERE x._id = ?1");
		q.setParameter(1, connID);
		
		try {
			conn = (BPELDAOConnectionImpl)q.getSingleResult();
			conn.setEntityManger(em);
		} catch (NoResultException e){}
		
		if ( conn == null ) {
			conn = new BPELDAOConnectionImpl(connID,em);
		}
		
		return conn;
	}
	
	public Long getConnectionId(BpelDAOConnection conn) {
		BPELDAOConnectionImpl oConn = (BPELDAOConnectionImpl)conn;
		return oConn.getID();
	}

	public void init(Properties properties) {
	}

}
