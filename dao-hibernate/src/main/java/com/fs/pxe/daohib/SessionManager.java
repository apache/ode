/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib;

import com.fs.pxe.daohib.bpel.hobj.*;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.utils.uuid.UUID;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Manages hibernate sessions, and their association with 
 * a transaction thread.  Uses a ThreadLocal strategy for 
 * managing sessions.
 */
public class SessionManager {
  private static final String PROP_GUID = "pxe.hibernate.guid";
  
  private static final Map<String, TransactionManager> _txManagers =
    Collections.synchronizedMap(new HashMap<String, TransactionManager>());
  private static final Map<String, DataSource> _dataSources = 
    Collections.synchronizedMap(new HashMap<String,DataSource>());

  private final String _uuid = new UUID().toString();
  private final TransactionManager _txManager;
  private final SessionFactory _sessionFactory;

  /** Inaccessible constructor. */
  public SessionManager(Properties env, DataSource ds, TransactionManager tx) throws HibernateException {
    if(tx == null)
      throw new IllegalArgumentException("Null transaction manager");

    _txManager = tx;
    _txManagers.put(_uuid,tx);
    _dataSources.put(_uuid,ds);
    
    _sessionFactory = getDefaultConfiguration()
            .setProperties(env)
            .setProperty(PROP_GUID, _uuid)
            .buildSessionFactory();
  }

  TransactionManager getTransactionManager() {
    return _txManager;
  }

  /**
   * Get the current Hibernate Session.
   */
  public Session getSession() {
    return _sessionFactory.getCurrentSession();
  }
  
  /**
   * Returns a hibernate configuration with hibernate DAO objects added as resources.
   * @return
   * @throws MappingException
   */
  public static final Configuration getDefaultConfiguration() throws MappingException {
    return new Configuration()
            .addClass(HProcess.class)
            .addClass(HProcessProperty.class)
            .addClass(HProcessInstance.class)
            .addClass(HCorrelator.class)
            .addClass(HCorrelatorSelector.class)
            .addClass(HCorrelatorMessage.class)
            .addClass(HCorrelatorMessageKey.class)
            .addClass(HCorrelationProperty.class)
            .addClass(HMessageExchange.class)
            .addClass(HMessage.class)
            .addClass(HPartnerLink.class)
            .addClass(HScope.class)
            .addClass(HCorrelationSet.class)
            .addClass(HXmlData.class)
            .addClass(HVariableProperty.class)
            .addClass(HBpelEvent.class)
	    .addClass(HFaultData.class)
            .addClass(HLargeData.class);
  }

  public static TransactionManager getTransactionManager(Properties props) {
    String guid = props.getProperty(PROP_GUID);
    return _txManagers.get(guid);
  }

  public static Connection getConnection(Properties props) throws SQLException {
    String guid = props.getProperty(PROP_GUID);
    return _dataSources.get(guid).getConnection();
  }
}
