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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.daohib.HibernateTransactionManagerLookup;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.store.ConfStoreConnectionFactory;
import org.apache.ode.store.Messages;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.msg.MessageBundle;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;

public class DbConfStoreConnectionFactory implements ConfStoreConnectionFactory {
    private static final Log __log = LogFactory.getLog(DbConfStoreConnectionFactory.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";

    private static final HashMap<String, DialectFactory.VersionInsensitiveMapper> HIBERNATE_DIALECTS = new HashMap<String, DialectFactory.VersionInsensitiveMapper>();

    private static final String _guid = new GUID().toString();

    private static final Map<String, DataSource> _dataSources = new ConcurrentHashMap<String, DataSource>();

    static {
        // Hibernate has a nice table that resolves the dialect from the database
        // product name,
        // but doesn't include all the drivers. So this is supplementary, and some
        // day in the
        // future they'll add more drivers and we can get rid of this.
        // Drivers already recognized by Hibernate:
        // HSQL Database Engine
        // DB2/NT
        // MySQL
        // PostgreSQL
        // Microsoft SQL Server Database, Microsoft SQL Server
        // Sybase SQL Server
        // Informix Dynamic Server
        // Oracle 8 and Oracle >8
        HIBERNATE_DIALECTS.put("Apache Derby", new DialectFactory.VersionInsensitiveMapper(DEFAULT_HIBERNATE_DIALECT));
        HIBERNATE_DIALECTS.put("H2", new DialectFactory.VersionInsensitiveMapper("org.hibernate.dialect.H2Dialect"));
    }

    private TransactionManager _txMgr;

    final SessionFactory _sessionFactory;

    public DbConfStoreConnectionFactory(DataSource ds, Properties initialProps, boolean createDatamodel, String txFactoryClassName) {
        // Don't want to pollute original properties
        Properties properties = new Properties();
        for (Object prop : initialProps.keySet()) {
            properties.put(prop, initialProps.get(prop));
        }

        __log.debug("using data source: " + ds);
        _dataSources.put(_guid, ds);

        if (properties.get(Environment.DIALECT) == null) {
            try {
                properties.put(Environment.DIALECT, guessDialect(ds));
            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeInitHibernateDialectDetectFailed();
                __log.error(errmsg, ex);
                throw new BpelEngineException(errmsg, ex);
            }
        }

        if (createDatamodel) {
            properties.put(Environment.HBM2DDL_AUTO, "create-drop");
        }

        
        // Note that we don't allow the following properties to be overriden by the client.
        if (properties.containsKey(Environment.CONNECTION_PROVIDER))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.CONNECTION_PROVIDER);
        if (properties.containsKey(Environment.TRANSACTION_MANAGER_STRATEGY))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.TRANSACTION_MANAGER_STRATEGY);
        if (properties.containsKey(Environment.SESSION_FACTORY_NAME))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.SESSION_FACTORY_NAME);

        properties.put(SessionManager.PROP_GUID, _guid);
        properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
        properties.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
        properties.put(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JTATransactionFactory");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");

        if(__log.isDebugEnabled()) __log.debug("Store connection properties: " + properties );

        initTxMgr(txFactoryClassName);
        SessionManager.registerTransactionManager(_guid, _txMgr);

        _sessionFactory = getDefaultConfiguration().setProperties(properties).buildSessionFactory();
    }

    public ConfStoreConnectionHib getConnection() {
        return new ConfStoreConnectionHib(_sessionFactory.getCurrentSession());
    }

	private void initTxMgr(String txFactoryClassName) {
		__log.info("ProcessStore initializing transaction manager using " + txFactoryClassName);
		try {
			Class<?> txFactClass = getClass().getClassLoader().loadClass(txFactoryClassName);
			Object txFact = txFactClass.newInstance();
			_txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
		} catch (Exception e) {
			__log.fatal("Couldn't initialize a transaction manager with factory: " + txFactoryClassName, e);
			throw new RuntimeException("Couldn't initialize a transaction manager with factory: " + txFactoryClassName, e);
		}
	}

    private String guessDialect(DataSource dataSource) throws Exception {

        String dialect = null;
        // Open a connection and use that connection to figure out database
        // product name/version number in order to decide which Hibernate
        // dialect to use.
        Connection conn = dataSource.getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            if (metaData != null) {
                String dbProductName = metaData.getDatabaseProductName();
                int dbMajorVer = metaData.getDatabaseMajorVersion();
                __log.info("Using database " + dbProductName + " major version " + dbMajorVer);
                DialectFactory.DatabaseDialectMapper mapper = HIBERNATE_DIALECTS.get(dbProductName);
                if (mapper != null) {
                    dialect = mapper.getDialectClass(dbMajorVer);
                } else {
                    Dialect hbDialect = DialectFactory.determineDialect(dbProductName, dbMajorVer);
                    if (hbDialect != null)
                        dialect = hbDialect.getClass().getName();
                }
            }
        } finally {
            conn.close();
        }

        if (dialect == null) {
            __log.info("Cannot determine hibernate dialect for this database: using the default one.");
            dialect = DEFAULT_HIBERNATE_DIALECT;
        }

        return dialect;

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

    public static class DataSourceConnectionProvider implements ConnectionProvider {
        private String _guid;

        public DataSourceConnectionProvider() {
        }

        public void configure(Properties props) throws HibernateException {
        	_guid = props.getProperty(SessionManager.PROP_GUID);
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
    }

}
