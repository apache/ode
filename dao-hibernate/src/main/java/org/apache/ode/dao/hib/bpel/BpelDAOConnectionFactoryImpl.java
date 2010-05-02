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
package org.apache.ode.dao.hib.bpel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.hib.DataSourceConnectionProvider;
import org.apache.ode.dao.hib.HibernateTransactionManagerLookup;
import org.apache.ode.dao.hib.SessionManager;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;
import java.util.Enumeration;
import javax.transaction.TransactionManager;
import org.apache.ode.il.config.OdeConfigProperties;
import org.hibernate.cfg.Configuration;



/**
 * Hibernate-based {@link org.apache.ode.dao.bpel.BpelDAOConnectionFactory}
 * implementation.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {
    private static final Log __log = LogFactory.getLog(BpelDAOConnectionFactoryImpl.class);

    protected SessionManager _sessionManager;
    protected TransactionManager _txm;
    protected DataSource _ds;


    /**
     * Constructor.
     */
    public BpelDAOConnectionFactoryImpl() {
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
     * @see org.apache.ode.dao.bpel.BpelDAOConnectionFactory#init(java.util.Properties)
     */
    @SuppressWarnings("unchecked")
    public void init(Properties initialProps, TransactionManager mgr, Object env) {
      _txm=mgr;
      _ds=(DataSource)env;
      if (_txm == null){
          __log.error("Hibernate BpelDAOConnectionFactoryImpl requires a JTA Transaction Manager to be set.");
      }
      _sessionManager = setupSessionManager(initialProps, _txm, _ds);


    }

     public static SessionManager setupSessionManager(Properties initialProps, TransactionManager mgr, DataSource ds){
        return setupSessionManager(SessionManager.getDefaultConfiguration(), initialProps, mgr, ds);
     }

    public static SessionManager setupSessionManager(Configuration conf, Properties initialConfig, TransactionManager mgr, DataSource ds){
        // Don't want to pollute original properties
        Properties properties = new Properties();
        if (initialConfig != null) {
            for (Object prop : initialConfig.keySet()) {
                properties.put(prop, initialConfig.get(prop));
            }
        }

        // Note that we don't allow the following properties to be overriden by
        // the client.
        /*
        if (properties.containsKey(Environment.CONNECTION_PROVIDER))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.CONNECTION_PROVIDER);
        if (properties.containsKey(Environment.TRANSACTION_MANAGER_STRATEGY))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.TRANSACTION_MANAGER_STRATEGY);
        if (properties.containsKey(Environment.SESSION_FACTORY_NAME))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.SESSION_FACTORY_NAME);
        */
        if (ds!=null){
          properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
          // Guess Hibernate dialect if not specified in hibernate.properties
          if (properties.get(Environment.DIALECT) == null) {
              try {
                  properties.put(Environment.DIALECT, guessDialect(ds));
              } catch (Exception ex) {
                  String errmsg = "Unable to detect Hibernate dialect!";

                  if (__log.isDebugEnabled())
                      __log.debug(errmsg, ex);

                  __log.error(errmsg);
              }
          }
        }

        if (mgr!=null){
          properties.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
           /*
          * Since Hibernate 3.2.6, Hibernate JTATransaction requires User Transaction bound on JNDI. Let's work around
          * by implementing Hibernate JTATransactionFactory that hooks up to the JTATransactionManager(ODE uses geronimo
          * by default).
          */
          //properties.put(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JTATransactionFactory");
          properties.put(Environment.TRANSACTION_STRATEGY, "org.apache.ode.dao.hib.JotmTransactionFactory");
          properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
        }else{
          properties.put(Environment.TRANSACTION_STRATEGY,"org.hibernate.transaction.JDBCTransactionFactory");
          properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS,"thread");
        }

        // Isolation levels override
        if (System.getProperty("ode.connection.isolation") != null) {
            String level = System.getProperty("ode.connection.isolation", "2");
            properties.put(Environment.ISOLATION, level);
        }

        if (Boolean.valueOf(initialConfig.getProperty(OdeConfigProperties.PROP_DB_EMBEDDED_CREATE, "true"))) {
            properties.put(Environment.HBM2DDL_AUTO, "create-drop");
        }

        if (__log.isDebugEnabled()) {
            Enumeration names = properties.propertyNames();
            __log.debug("Properties passed to Hibernate:");
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                __log.debug(name + "=" + properties.getProperty(name));
            }
        }

        return new SessionManager(conf, properties, ds,mgr);

    }



    public void shutdown() {
        _sessionManager.shutdown();
    }

   private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";

   public static String guessDialect(DataSource dataSource) throws Exception {

        String dialect = null;
        // Open a connection and use that connection to figure out database
        // product name/version number in order to decide which Hibernate
        // dialect to use.
        Connection conn = dataSource.getConnection();
        try {
            Dialect d = DialectFactory.buildDialect(new Properties(), conn);
            dialect=d.getClass().getName();
        } finally {
            conn.close();
        }

        if (dialect == null) {
            __log.info("Cannot determine hibernate dialect for this database: using the default one.");
            dialect = DEFAULT_HIBERNATE_DIALECT;
        }

        return dialect;

    }
   
}
