/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk;

import com.fs.pxe.daohib.sfwk.hobj.HDomain;
import com.fs.pxe.daohib.sfwk.hobj.HSfwkMessageExchange;
import com.fs.pxe.daohib.sfwk.hobj.HSystem;
import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;
import com.fs.pxe.sfwk.bapi.dao.SystemDAO;

import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate-based implementation of the {@link DomainStateConnection} interface.
 * Objects of this class are returned from {@link DAOConnectionFactoryImpl}.
 */
class DomainStateConnectionImpl implements DomainStateConnection {

  private Session _sess;
  private HDomain _domain;

  /** Constructor. */
	public DomainStateConnectionImpl(Session sess, HDomain domain) {
		_sess = sess;
    _domain = domain;
	}
  
	public SystemDAO createSystemDeployment(String systemId, String systemName) {
		HSystem system = new HSystem();
    system.setSystemUUID(systemId);
    system.setSystemName(systemName);
    system.setDomain(_domain);
    system.setMessageExchanges(new HashSet<HSfwkMessageExchange>());
    try {
			_sess.save(system);
		} catch (HibernateException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
    _domain.getSystems().add(system);
    return new SystemDaoImpl(_sess, system);
	}

	public Collection<SystemDAO> findAllSystems() {
    List<SystemDAO> systems = new ArrayList<SystemDAO>();

    for(Iterator iter = _domain.getSystems().iterator(); iter.hasNext(); ) {
    	systems.add(new SystemDaoImpl(_sess, (HSystem)iter.next()));
    }

    return systems;
	}

	public SystemDAO findSystem(String systemID) {
    HSystem system = (HSystem)_sess.get(HSystem.class, systemID.toString());
    return system != null
            ? new SystemDaoImpl(_sess, system)
            : null;
  }

	public SystemDAO findDeployedSystemByName(String systemName) {
    Query query = _sess.getNamedQuery("HSystem.DeployedByName");
    query.setParameter(0,systemName);
    HSystem hsys = (HSystem) query.uniqueResult();
    return hsys == null ? null : new SystemDaoImpl(_sess, hsys);
	}
	
  
  public void close() {
  }
}
