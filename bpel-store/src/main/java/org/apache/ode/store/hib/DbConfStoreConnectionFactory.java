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
package org.apache.ode.store.hib;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.daohib.HibertenateJtaPlatform;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.store.ConfStoreConnectionFactory;
import org.apache.ode.store.Messages;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.msg.MessageBundle;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.dialect.Dialect;

public class DbConfStoreConnectionFactory implements ConfStoreConnectionFactory {
    private static final Logger __log = LoggerFactory.getLogger(DbConfStoreConnectionFactory.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private static final String _guid = new GUID().toString();

    private static final Map<String, DataSource> _dataSources = new ConcurrentHashMap<String, DataSource>();

    private TransactionManager _txMgr;

    private final DataSource _ds;

    final SessionFactory _sessionFactory;

    public DbConfStoreConnectionFactory(DataSource ds, Properties initialProps, boolean createDatamodel, String txFactoryClassName) {
        _ds = ds;

        // Don't want to pollute original properties
        Properties properties = new Properties();
        for (Object prop : initialProps.keySet()) {
            properties.put(prop, initialProps.get(prop));
        }

        __log.debug("using data source: " + ds);
        _dataSources.put(_guid, ds);

        if (createDatamodel) {
            properties.put(Environment.HBM2DDL_AUTO, "create-drop");
        }


        // Note that we don't allow the following properties to be overriden by the client.
        if (properties.containsKey(Environment.CONNECTION_PROVIDER))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.CONNECTION_PROVIDER);
        if (properties.containsKey(Environment.JTA_PLATFORM))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.JTA_PLATFORM);
        if (properties.containsKey(Environment.SESSION_FACTORY_NAME))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.SESSION_FACTORY_NAME);
        if (properties.containsKey(Environment.TRANSACTION_STRATEGY))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.TRANSACTION_STRATEGY);
        if (properties.containsKey(Environment.CURRENT_SESSION_CONTEXT_CLASS))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.CURRENT_SESSION_CONTEXT_CLASS);

        properties.put(SessionManager.PROP_GUID, _guid);
        properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
        properties.put(Environment.JTA_PLATFORM, HibertenateJtaPlatform.class.getName());

        // Need to use CMTTransactionFactory instead of JTATransactionFactory in Hibernate 4
        // Refer: https://jira.spring.io/browse/SPR-9480?focusedCommentId=81419&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-81419
        properties.put(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.CMTTransactionFactory");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");

        if(__log.isDebugEnabled()) __log.debug("Store connection properties: " + properties );

        initTxMgr(txFactoryClassName);
        SessionManager.registerTransactionManager(_guid, _txMgr);

        Configuration configuration = getDefaultConfiguration().setProperties(properties);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        _sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public ConfStoreConnectionHib getConnection() {
        return new ConfStoreConnectionHib(_sessionFactory.getCurrentSession());
    }

    @SuppressWarnings("unchecked")
    private void initTxMgr(String txFactoryClassName) {
        __log.info("ProcessStore initializing transaction manager using " + txFactoryClassName);
        try {
            Class<?> txFactClass = getClass().getClassLoader().loadClass(txFactoryClassName);
            Object txFact = txFactClass.newInstance();
            _txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
        } catch (Exception e) {
            __log.error("Couldn't initialize a transaction manager with factory: " + txFactoryClassName, e);
            throw new RuntimeException("Couldn't initialize a transaction manager with factory: " + txFactoryClassName, e);
        }
    }

    public void beginTransaction() {
        try {
            _txMgr.begin();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void commitTransaction() {
        try {
            _txMgr.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void rollbackTransaction() {
        try {
            _txMgr.rollback();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Configuration getDefaultConfiguration() throws MappingException {
        return new Configuration().addClass(ProcessConfDaoImpl.class).addClass(DeploymentUnitDaoImpl.class)
                .addClass(VersionTrackerDAOImpl.class);
    }

    public static class DataSourceConnectionProvider implements ConnectionProvider, Configurable {
        private String _guid;

        public DataSourceConnectionProvider() {
        }

        public void configure(Map props) throws HibernateException {
            _guid = (String) props.get(SessionManager.PROP_GUID);
        }

        public Connection getConnection() throws SQLException {
            return _dataSources.get(_guid).getConnection();
        }

        public void closeConnection(Connection arg0) throws SQLException {
            arg0.close();
        }

        public void close() throws HibernateException {
        }

        public boolean supportsAggressiveRelease() {
            return true;
        }

        public boolean isUnwrappableAs(Class unwrapType) {
            return ConnectionProvider.class.equals(unwrapType) ||
                    DataSourceConnectionProvider.class.isAssignableFrom(unwrapType);
        }

        public <T> T unwrap(Class<T> unwrapType) {
            if (ConnectionProvider.class.equals(unwrapType) ||
                    DataSourceConnectionProvider.class.isAssignableFrom(unwrapType)) {
                return (T) this;
            } else {
                throw new UnknownUnwrapTypeException( unwrapType );
            }
        }
    }

}
