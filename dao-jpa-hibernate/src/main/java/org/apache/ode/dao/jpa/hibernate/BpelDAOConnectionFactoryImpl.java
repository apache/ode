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
package org.apache.ode.dao.jpa.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.apache.ode.dao.jpa.JpaConnection;
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.GUID;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectFactory;
import org.hibernate.ejb.EntityManagerImpl;

/**

 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

    static final Log __log = LogFactory.getLog(BpelDAOConnectionFactoryImpl.class);
    static Map _defaultProperties = new HashMap();
    static JpaOperator _operator = new JpaOperatorImpl();
    EntityManagerFactory _emf;
    TransactionManager _txm;
    DataSource _ds;

    static {
        _defaultProperties.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
    }

    public void init(Properties odeConfig, TransactionManager txm, Object env) {
        this._txm = txm;
        this._ds = (DataSource) env;
        Map emfProperties = buildConfig(OdeConfigProperties.PROP_DAOCF + ".", odeConfig, _txm, _ds);
        _emf = Persistence.createEntityManagerFactory("ode-bpel", emfProperties);

    }

    public BpelDAOConnection getConnection() {
        final ThreadLocal<BpelDAOConnectionImpl> currentConnection = BpelDAOConnectionImpl.getThreadLocal();

        BpelDAOConnectionImpl conn = (BpelDAOConnectionImpl) currentConnection.get();
        if (conn != null && isOpen(conn)) {
            return conn;
        } else {
            EntityManager em = _emf.createEntityManager();
            conn = new BpelDAOConnectionImpl(em, _txm, _operator);
            currentConnection.set(conn);
            return conn;
        }
    }

    public void shutdown() {
        _emf.close();
    }

    static Map buildConfig(String prefix, Properties odeConfig, TransactionManager txm, DataSource ds) {
        Map props = new HashMap(_defaultProperties);

        String guid = new GUID().toString();
        if (ds != null) {
            props.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
            HibernateUtil.registerDatasource(guid, ds);
            props.put(Environment.DIALECT, guessDialect(ds));
        }
        if (txm != null) {
            props.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
            props.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
            HibernateUtil.registerTransactionManager(guid, txm);
            props.put("javax.persistence.transactionType", "JTA");
        } else {
            props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        }


        if (ds != null || txm != null) {
            props.put(HibernateUtil.PROP_GUID, guid);
        }

        if (Boolean.valueOf(odeConfig.getProperty(OdeConfigProperties.PROP_DB_EMBEDDED_CREATE, "true"))) {
            props.put(Environment.HBM2DDL_AUTO, "create-drop");
        }

        // Isolation levels override; when you use a ConnectionProvider, this has no effect
        //String level = System.getProperty("ode.connection.isolation", "2");
        //props.put(Environment.ISOLATION, level);

        addEntries(prefix, odeConfig, props);

        return props;
    }

    public static void addEntries(String prefix, Properties odeConfig, Map props) {
        if (odeConfig != null) {
            for (Map.Entry me : odeConfig.entrySet()) {
                String key = (String) me.getKey();
                if (key.startsWith(prefix)) {
                    String jpaKey = key.substring(prefix.length() - 1);
                    String val = (String) me.getValue();
                    if (val == null || val.trim().length() == 0) {
                        props.remove(jpaKey);
                    } else {
                        props.put(jpaKey, me.getValue());
                    }
                }
            }
        }
    }

    /*
     * For some reason Hibernate does not mark an EntityManager as being closed when
     * the EntityManagerFactory that created it is closed. This method performs a
     * deep introspection to determine if the EntityManager is still viable.
     */
    public static boolean isOpen(JpaConnection conn) {
        EntityManager mgr = conn.getEntityManager();
        if (mgr == null) {
            return false;
        } else if (mgr instanceof EntityManagerImpl) {
            EntityManagerImpl mgrImpl = (EntityManagerImpl) mgr;
            return !mgrImpl.getSession().getSessionFactory().isClosed();
        } else {
            return !conn.isClosed();
        }
    }
    private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";
    
    public static String guessDialect(DataSource dataSource) {

        String dialect = null;
        // Open a connection and use that connection to figure out database
        // product name/version number in order to decide which Hibernate
        // dialect to use.
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            Dialect d = DialectFactory.buildDialect(new Properties(), conn);
            dialect = d.getClass().getName();
        } catch (SQLException se) {
            __log.error(se);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                __log.error(ex);
            }
        }

        if (dialect == null) {
            __log.info("Cannot determine hibernate dialect for this database: using the default one.");
            dialect = DEFAULT_HIBERNATE_DIALECT;
        }

        return dialect;

    }    

}

