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
package org.apache.ode.daohib.bpel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.daohib.DataSourceConnectionProvider;
import org.apache.ode.daohib.HibernateTransactionManagerLookup;
import org.apache.ode.daohib.SessionManager;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;

/**
 * Hibernate-based {@link org.apache.ode.bpel.dao.BpelDAOConnectionFactory}
 * implementation.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactoryJDBC {
    private static final Log __log = LogFactory.getLog(BpelDAOConnectionFactoryImpl.class);

    protected SessionManager _sessionManager;

    private DataSource _ds;

    private TransactionManager _tm;

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
     * @see org.apache.ode.bpel.dao.BpelDAOConnectionFactory#init(java.util.Properties)
     */
    public void init(Properties initialProps) {
        if (_ds == null) {
            String errmsg = "setDataSource() not called!";
            __log.fatal(errmsg);
            throw new IllegalStateException(errmsg);
        }

        if (_tm == null) {
            String errmsg = "setTransactionManager() not called!";
            __log.fatal(errmsg);
            throw new IllegalStateException(errmsg);
        }

        if (initialProps == null) initialProps = new Properties();
        // Don't want to pollute original properties
        Properties properties = new Properties();
        for (Object prop : initialProps.keySet()) {
            properties.put(prop, initialProps.get(prop));
        }

        // Note that we don't allow the following properties to be overriden by
        // the client.
        if (properties.containsKey(Environment.CONNECTION_PROVIDER))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.CONNECTION_PROVIDER);
        if (properties.containsKey(Environment.TRANSACTION_MANAGER_STRATEGY))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.TRANSACTION_MANAGER_STRATEGY);
        if (properties.containsKey(Environment.SESSION_FACTORY_NAME))
            __log.warn("Ignoring user-specified Hibernate property: " + Environment.SESSION_FACTORY_NAME);

        properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
        properties.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
        properties.put(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JTATransactionFactory");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");

        // Guess Hibernate dialect if not specified in hibernate.properties
        if (properties.get(Environment.DIALECT) == null) {
            try {
                properties.put(Environment.DIALECT, guessDialect(_ds));
            } catch (Exception ex) {
                String errmsg = "Unable to detect Hibernate dialect!";

                if (__log.isDebugEnabled())
                    __log.debug(errmsg, ex);

                __log.error(errmsg);
            }
        }

        // Isolation levels override; when you use a ConnectionProvider, this has no effect
        String level = System.getProperty("ode.connection.isolation", "2");
        properties.put(Environment.ISOLATION, level);

        if (__log.isDebugEnabled()) {
            Enumeration<?> names = properties.propertyNames();
            __log.debug("Properties passed to Hibernate:");
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                __log.debug(name + "=" + properties.getProperty(name));
            }
        }
        _sessionManager = createSessionManager(properties, _ds, _tm);
    }

    protected SessionManager createSessionManager(Properties properties, DataSource ds, TransactionManager tm) {
        return new SessionManager(properties, ds, tm);
    }

    private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";

    private static final HashMap<String, DialectFactory.VersionInsensitiveMapper> HIBERNATE_DIALECTS = new HashMap<String, DialectFactory.VersionInsensitiveMapper>();

    static {
        // Hibernate has a nice table that resolves the dialect from the
        // database
        // product name,
        // but doesn't include all the drivers. So this is supplementary, and
        // some
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
        HIBERNATE_DIALECTS.put("Apache Derby", new DialectFactory.VersionInsensitiveMapper(
                "org.hibernate.dialect.DerbyDialect"));
        HIBERNATE_DIALECTS.put("INGRES", new DialectFactory.VersionInsensitiveMapper(
                "org.hibernate.dialect.IngresDialect"));
        HIBERNATE_DIALECTS.put("H2", new DialectFactory.VersionInsensitiveMapper(
                "org.hibernate.dialect.H2Dialect"));
    }

    public void shutdown() {
        _sessionManager.shutdown();
        _ds = null;
        _sessionManager = null;
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

    public void setDataSource(DataSource ds) {
        _ds = ds;
    }

    public DataSource getDataSource() {
        return _ds;
    }

    public void setTransactionManager(Object tm) {
        _tm = (TransactionManager) tm;
    }

    public void setUnmanagedDataSource(DataSource ds) {
        // Hibernate doesn't use this.
    }

}
