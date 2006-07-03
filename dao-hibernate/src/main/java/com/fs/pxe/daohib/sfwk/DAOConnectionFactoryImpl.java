/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk;

import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.sfwk.hobj.HDomain;
import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;
import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * A {@link DAOConnectionFactory} implementation based on
 * the Hibernate database abstraction layer.
 */
public class DAOConnectionFactoryImpl implements DAOConnectionFactory {
  private static final Log __log = LogFactory.getLog(DAOConnectionFactoryImpl.class);

  private static final String QRY_DOMAIN = "from " + HDomain.class.getName() + " as d where d.domainId = ?";

  private SessionManager _sessionManger;


  /** Constructor. */
	public DAOConnectionFactoryImpl(SessionManager sessionManager) {
    _sessionManger = sessionManager;
  }

	public void createDomainStateStore(String domainId) {
		Session sess = null;
    try{
      sess = _sessionManger.getSession();
      Query qry = sess.createQuery(QRY_DOMAIN);
      qry.setString(0,domainId);
      List results = qry.list();
      if(results.size() == 0){
        HDomain domain = new HDomain();
        domain.setDomainId(domainId);
        sess.save(domain);
        sess.flush();
      }
    }catch(HibernateException e) {
      __log.error("DbError: Cannot create domain." ,e);
      throw e;
    }
	}
	/**
	 * @see com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory#open(java.lang.String)
	 */
	public DomainStateConnection open(String domainId) {
    try{
      Session sess = _sessionManger.getSession();
      HDomain domain = (HDomain)sess.load(HDomain.class, domainId);
    	return new DomainStateConnectionImpl(sess, domain);
    }catch(HibernateException e){
      __log.error("DbError" ,e);
    	throw e;
    }
	}
}
