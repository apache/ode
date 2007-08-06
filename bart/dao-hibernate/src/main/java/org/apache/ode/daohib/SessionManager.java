/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.daohib;

import org.apache.ode.daohib.bpel.hobj.*;
import org.apache.ode.utils.uuid.UUID;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages hibernate sessions, and their association with 
 * a transaction thread.  Uses a ThreadLocal strategy for 
 * managing sessions.
 */
public class SessionManager {
  private static final String PROP_GUID = "ode.hibernate.guid";
  
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
            .addClass(HProcessInstance.class)
            .addClass(HCorrelator.class)
            .addClass(HCorrelatorMessage.class)
            .addClass(HCorrelationProperty.class)
            .addClass(HCorrelatorSelector.class)
            .addClass(HMessageExchange.class)
            .addClass(HMessage.class)
            .addClass(HPartnerLink.class)
            .addClass(HScope.class)
            .addClass(HCorrelationSet.class)
            .addClass(HXmlData.class)
            .addClass(HVariableProperty.class)
            .addClass(HBpelEvent.class)
	        .addClass(HFaultData.class)
	    .addClass(HActivityRecovery.class)
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
