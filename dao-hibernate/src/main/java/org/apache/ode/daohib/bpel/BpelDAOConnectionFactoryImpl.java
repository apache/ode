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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.daohib.DataSourceConnectionProvider;
import org.apache.ode.daohib.HibernateTransactionManagerLookup;
import org.apache.ode.daohib.SessionManager;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectFactory;

/**
 * Hibernate-based {@link org.apache.ode.bpel.dao.BpelDAOConnectionFactory}
 * implementation.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactoryJDBC {
    private static final Logger __log = LoggerFactory.getLogger(BpelDAOConnectionFactoryImpl.class);

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
            __log.error(errmsg);
            throw new IllegalStateException(errmsg);
        }

        if (_tm == null) {
            String errmsg = "setTransactionManager() not called!";
            __log.error(errmsg);
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

    public void shutdown() {
    	_sessionManager.shutdown();
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
