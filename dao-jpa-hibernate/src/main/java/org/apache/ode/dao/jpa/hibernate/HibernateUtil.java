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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.jpa.JpaConnection;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.GUID;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.EntityManagerImpl;

/**
 * Manages datasource and transaction, for hibernate usage.
 * 
 * setup Hibernate specific initialization.
 * 
 */
public class HibernateUtil {
	
	static final Log __log = LogFactory.getLog(HibernateUtil.class);
	
    public static final String PROP_GUID = "ode.hibernate.guid";

    private static final Map<String, TransactionManager> _txManagers = Collections.synchronizedMap(new HashMap<String, TransactionManager>());
    private static final Map<String, DataSource> _dataSources = Collections.synchronizedMap(new HashMap<String,DataSource>());

    public static void registerTransactionManager(String uuid, TransactionManager txm) {
        _txManagers.put(uuid, txm);
    }
    
    public static void registerDatasource(String uuid, DataSource ds){
    	_dataSources.put(uuid, ds);
    }

    public static TransactionManager getTransactionManager(Properties props) {
        String guid = props.getProperty(PROP_GUID);
        TransactionManager mgr = _txManagers.get(guid);
        if (__log.isDebugEnabled()) {
        	__log.debug("guid is: " + guid + ", TransactionManager is: " + mgr);
        }
        return mgr;
    }

    public static Connection getConnection(Properties props) throws SQLException {
        String guid = props.getProperty(PROP_GUID);
        Connection conn =  _dataSources.get(guid).getConnection();
        if (__log.isDebugEnabled()) {
        	__log.debug("guid is: " + guid + ", Connection is: " + conn);
        }
        return conn;
    }
    
    public static Map buildConfig(String prefix, Properties odeConfig, TransactionManager txm, DataSource ds) {
        Map props = new HashMap();
        props.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
        
        addEntries(prefix, odeConfig, props);

        String guid = new GUID().toString();
        if (ds != null) {
            props.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
            registerDatasource(guid, ds);
        }
        if (txm != null) {
            props.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
            props.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
            //props.put(Environment.TRANSACTION_STRATEGY, "org.apache.ode.dao.jpa.hibernate.JotmTransactionFactory");
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
            __log.debug("create-drop DDL by Hibernate automatically");
        }

        if (__log.isDebugEnabled()) {
        	__log.debug("========= Hibernate properties ==============");
        	for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
        		Object key = it.next();
        		__log.debug("key : " + key + ", value : " + props.get(key));
        	}
        	__log.debug("==============================================");
        }

        return props;
    }

    private static void addEntries(String prefix, Properties odeConfig, Map props) {
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
                } else if (key.startsWith("hibernate")) {
                	props.put(key, me.getValue());
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
    
    
}
