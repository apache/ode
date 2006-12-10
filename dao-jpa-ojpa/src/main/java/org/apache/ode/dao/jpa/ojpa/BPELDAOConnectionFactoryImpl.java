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
	private BPELDAOConnectionImpl conn;
	
	public BPELDAOConnectionFactoryImpl(EntityManager em) {
		this.em = em;
	}
	
	public BpelDAOConnection getConnection() {
		
		if ( conn == null ) {
			List<BpelDAOConnection> conns = null;
			
			Query q = em.createQuery("SELECT x FROM BPELDAOConnectionImpl x order by x._id asc");
			
			try {
				conns = (List<BpelDAOConnection>)q.getResultList();
				if ( conns.size() < 1 ) {
					conn = new BPELDAOConnectionImpl(new Long(1),em);
				} else {
					conn = (BPELDAOConnectionImpl)conns.get(conns.size()-1);
					conn.setEntityManger(em);
				}
				
			} catch (NoResultException e) {
				conn = new BPELDAOConnectionImpl(new Long(1),em);
			}
		}
		
		return conn;
	}
	
	public BpelDAOConnection getConnection(Long connID) {
		if ( conn != null && conn.getID().equals(connID) ) {
			return conn;
		}
		
		BPELDAOConnectionImpl tmpConn = null;
		
		Query q = em.createQuery("SELECT x FROM BPELDAOConnectionImpl x WHERE x._id = ?1");
		q.setParameter(1, connID);
		
		try {
			tmpConn = (BPELDAOConnectionImpl)q.getSingleResult();
			tmpConn.setEntityManger(em);
		} catch (NoResultException e){}
		
		if ( tmpConn == null ) {
			conn = new BPELDAOConnectionImpl(connID,em);
			tmpConn = conn;
		}
		
		return tmpConn;
	}
	
	public Long getConnectionId(BpelDAOConnection conn) {
		BPELDAOConnectionImpl oConn = (BPELDAOConnectionImpl)conn;
		return oConn.getID();
	}

	public void init(Properties properties) {
	}

}
