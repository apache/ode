package org.apache.ode.dao.jpa.ojpa;


import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
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
		
		Query q = em.createQuery("SELECT x FROM BPELDAOConnectionImpl x order by x._id asc");
		
		List<BpelDAOConnection> conns = (List<BpelDAOConnection>)q.getResultList();
		
		if ( conns.size() < 1 ) {
			return new BPELDAOConnectionImpl(new Long(1));
		}
		
		return conns.get(conns.size()-1);
	}
	
	public BpelDAOConnection getConnection(Long connID) {
		
		Query q = em.createQuery("SELECT x FROM BPELDAOConnectionImpl x WHERE x._id = ?1");
		q.setParameter(1, connID);
		
		BpelDAOConnection conn = (BpelDAOConnection)q.getSingleResult();
		
		if ( conn == null ) {
			conn = new BPELDAOConnectionImpl(connID);
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
