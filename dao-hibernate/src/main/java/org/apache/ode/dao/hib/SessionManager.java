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
package org.apache.ode.dao.hib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.hib.bpel.hobj.*;
import org.apache.ode.dao.hib.store.hobj.DeploymentUnitDaoImpl;
import org.apache.ode.dao.hib.store.hobj.ProcessConfDaoImpl;
import org.apache.ode.dao.hib.store.hobj.VersionTrackerDAOImpl;
import org.apache.ode.utils.uuid.UUID;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

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
    private static final Log __log = LogFactory.getLog(SessionManager.class);

    public static final String PROP_GUID = "ode.hibernate.guid";

    private static final Map<String, TransactionManager> _txManagers =
            Collections.synchronizedMap(new HashMap<String, TransactionManager>());
    private static final Map<String, DataSource> _dataSources =
            Collections.synchronizedMap(new HashMap<String,DataSource>());

    private static final String[] CANNOT_JOIN_FOR_UPDATE_DIALECTS =
            {"org.hibernate.dialect.IngresDialect"};

    private final String _uuid = new UUID().toString();
    private final TransactionManager _txManager;
    private final SessionFactory _sessionFactory;
    private boolean _canJoinForUpdate = true;
    
    private final TxContext _ctx;

    public SessionManager(Properties env, DataSource ds, TransactionManager tx) throws HibernateException {
        this(getDefaultConfiguration(), env, ds, tx);
    }

    /** Inaccessible constructor. */
    public SessionManager(Configuration conf, Properties env, DataSource ds, TransactionManager tx) throws HibernateException {
        
    	if (tx!=null){
            _ctx = new HibernateJtaTxContext();
        }else{
            _ctx = new HibernateNonTxContext();
        }

        _txManager = tx;
        _txManagers.put(_uuid,tx);
        _dataSources.put(_uuid,ds);

        _sessionFactory = conf.setProperties(env).setProperty(PROP_GUID, _uuid).buildSessionFactory();

        /*
        Some Hibernate dialects (like IngresDialect) do not support update for join.
        We need to distinguish them and explicitly define subqueries, otherwise Hibernate
        implicitly generates joins which causes problems during update for such DBMS.
        See org.apache.ode.dao.hib.bpel.CorrelatorDaoImpl for instance.
        */
        String currentHibDialect = env.getProperty(Environment.DIALECT);
        for (String dialect : CANNOT_JOIN_FOR_UPDATE_DIALECTS) {
            if (dialect.equals(currentHibDialect)) {
                _canJoinForUpdate = false;
            }
        }
    }

    TransactionManager getTransactionManager() {
        return _txManager;
    }

    public static void registerTransactionManager(String uuid, TransactionManager txm) {
        _txManagers.put(uuid, txm);
    }

    /**
     * Get the current Hibernate Session.
     */
    public Session getSession() {
        return _sessionFactory.getCurrentSession();
    }

    /**
     * Returns flag which shows whether " where .. join ... for update" kind of queries can be used (supported
     * by currently effective {@link org.hibernate.dialect.Dialect}. If it's {@code false} than sub-query fallback
     * should be invoked instead.
     *
     * @return currently returns false only for {@link org.hibernate.dialect.IngresDialect}
     */
    public boolean canJoinForUpdate() {
        return _canJoinForUpdate;
    }


    /**
     * Returns a hibernate configuration with hibernate DAO objects added as resources.
     * @return
     * @throws MappingException
     */
    public static Configuration getDefaultConfiguration() throws MappingException {
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
                .addClass(HMessageExchangeProperty.class)//store classes
                .addClass(ProcessConfDaoImpl.class)
                .addClass(DeploymentUnitDaoImpl.class)
                .addClass(VersionTrackerDAOImpl.class);
    }

    public static TransactionManager getTransactionManager(Properties props) {
        String guid = props.getProperty(PROP_GUID);
        return _txManagers.get(guid);
    }

    public static Connection getConnection(Properties props) throws SQLException {
        String guid = props.getProperty(PROP_GUID);
        return _dataSources.get(guid).getConnection();
    }
    
    public boolean isClosed() {
        return _sessionFactory.isClosed();
    }
    
    public void shutdown() {
        _sessionFactory.close();
    }
    
    
    public void begin(){
        _ctx.begin();
     }

     public void commit(){
       _ctx.commit();
     }

     public void rollback(){
       _ctx.rollback();
     }

     public interface TxContext {

         public void begin();

         public void commit();

         public void rollback();
     }

     public class HibernateNonTxContext implements TxContext {

         public void begin() {
             getSession().beginTransaction();
         }

         public void commit() {
             getSession().getTransaction().commit();
         }

         public void rollback() {
             getSession().getTransaction().rollback();
         }
     }

     public class HibernateJtaTxContext implements TxContext {

         public void begin() {
         }

         public void commit() {
         }

         public void rollback() {
         }
     }
}
