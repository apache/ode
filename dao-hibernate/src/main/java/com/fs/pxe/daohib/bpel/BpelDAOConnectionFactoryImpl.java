/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.dao.BpelDAOConnection;
import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.daohib.SessionManager;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

/**
 * Hibernate-based {@link com.fs.pxe.bpel.dao.BpelDAOConnectionFactory}
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
   * @see com.fs.pxe.bpel.dao.BpelDAOConnectionFactory#init(java.util.Properties)
   */
  public void init(Properties properties) {
  }
}
