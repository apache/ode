/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.daohib.SessionManager;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

/**
 * Hibernate-based {@link org.apache.ode.bpel.dao.BpelDAOConnectionFactory}
 * implementation.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {
  private static final Log __log = LogFactory
      .getLog(BpelDAOConnectionFactoryImpl.class);

  private SessionManager _sessionManager;

  /**
   * Constructor.
   */
  public BpelDAOConnectionFactoryImpl(SessionManager sessionManager) {
    _sessionManager = sessionManager;
  }

  public BpelDAOConnection getConnection() {
    try {
      return new BpelDAOConnectionImpl(_sessionManager);
    } catch (HibernateException e) {
      __log.error("DbError", e);
      throw e;
    }
  }

  /**
   * @see org.apache.ode.bpel.dao.BpelDAOConnectionFactory#init(java.util.Properties)
   */
  public void init(Properties properties) {
  }
}
