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
package org.apache.ode.dao.jpa.openjpa;

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
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;
import org.apache.ode.il.config.OdeConfigProperties;

/**

 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

    static final Log __log = LogFactory.getLog(BpelDAOConnectionFactoryImpl.class);
    static Map _defaultProperties = new HashMap();
    static JpaOperator _operator = new JpaOperatorImpl();
    protected EntityManagerFactory _emf;
    protected DataSource _ds;
    protected TransactionManager _txm;

    static {
        _defaultProperties.put("javax.persistence.provider", "org.apache.openjpa.persistence.PersistenceProviderImpl");
        _defaultProperties.put("openjpa.Log", "log4j");
        //This was previously set to fault but caused some issues with in memory CorrelatorDAO rout finds.
        _defaultProperties.put("openjpa.FlushBeforeQueries", "true");
        _defaultProperties.put("openjpa.FetchBatchSize", 1000);

        // _defaultProperties.put("openjpa.Log", "DefaultLevel=TRACE");
    }

    public void init(Properties odeConfig, TransactionManager mgr, Object env) {
        _txm = mgr;
        _ds = (DataSource) env;
        Map emfProperties = buildConfig(OdeConfigProperties.PROP_DAOCF + ".", odeConfig, _txm, _ds);
        _emf = Persistence.createEntityManagerFactory("ode-bpel", emfProperties);
    }

    public BpelDAOConnection getConnection() {
        final ThreadLocal<BpelDAOConnectionImpl> currentConnection = BpelDAOConnectionImpl.getThreadLocal();
        BpelDAOConnectionImpl conn = (BpelDAOConnectionImpl) currentConnection.get();
        if (conn != null && !conn.isClosed()) {
            return conn;
        } else {
            EntityManager em = _emf.createEntityManager();
            conn = createBPELDAOConnection(em, _txm, _operator);
            currentConnection.set(conn);
            return conn;
        }
    }

    protected BpelDAOConnectionImpl createBPELDAOConnection(EntityManager em, TransactionManager mgr, JpaOperator operator) {
        return new BpelDAOConnectionImpl(em, mgr, operator);
    }

    public void shutdown() {
        _emf.close();
    }

    static Map buildConfig(String prefix, Properties odeConfig, TransactionManager mgr, DataSource ds) {
        Map props = new HashMap(_defaultProperties);
        if (mgr != null) {
            props.put("openjpa.TransactionMode","managed");
            /* The following was originally set to managed but rollback tests failed.
             * There is no code in ODE to automatically enlist  DataSource s in
             * global transactions
             */
            props.put("openjpa.ConnectionFactoryMode", "local");
            props.put("openjpa.jdbc.TransactionIsolation", "read-committed");
            props.put("openjpa.ManagedRuntime", new JpaTxMgrProvider(mgr));
            props.put("javax.persistence.transactionType", "JTA");
        } else {
            props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        }
        if (ds != null) {
            props.put("openjpa.ConnectionFactory", ds);
        }

        //props.put("openjpa.jdbc.DBDictionary", dictionary);

        if (Boolean.valueOf(odeConfig.getProperty(OdeConfigProperties.PROP_DB_EMBEDDED_CREATE, "true"))) {
            props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(SchemaAction=drop,SchemaAction=add,ForeignKeys=true)");
        }

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
}
