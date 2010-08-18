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
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;
import org.apache.ode.il.config.OdeConfigProperties;

/**

 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

    static final Log __log = LogFactory.getLog(BpelDAOConnectionFactoryImpl.class);
    JpaOperator _operator = new JpaOperatorImpl();
    EntityManagerFactory _emf;
    TransactionManager _txm;
    DataSource _ds;

    public void init(Properties odeConfig, TransactionManager txm, Object env) {
        this._txm = txm;
        this._ds = (DataSource) env;
        Map emfProperties = HibernateUtil.buildConfig(OdeConfigProperties.PROP_DAOCF + ".", odeConfig, _txm, _ds);
        _emf = Persistence.createEntityManagerFactory("ode-bpel", emfProperties);

    }

    public BpelDAOConnection getConnection() {
        final ThreadLocal<BpelDAOConnectionImpl> currentConnection = BpelDAOConnectionImpl.getThreadLocal();

        BpelDAOConnectionImpl conn = (BpelDAOConnectionImpl) currentConnection.get();
        if (conn != null && HibernateUtil.isOpen(conn)) {
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


    //private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";
    
    //Because the JBoss AS 5.1.0.GA uses Hibernate 3.3.1.GA
    //While SOA-P 5 uses Hibernate 3.3.2.GA, they are different for guessDialect, so comment it now.
    
/*    private static String guessDialect(DataSource dataSource) {

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

    }  */  

}

