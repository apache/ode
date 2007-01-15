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

package org.apache.ode.dao.jpa.ojpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.dao.jpa.BPELDAOConnectionImpl;
import org.apache.openjpa.ee.ManagedRuntime;

public class BPELDAOConnectionFactoryImpl implements BpelDAOConnectionFactoryJDBC {

    private EntityManagerFactory _emf;

    private TransactionManager _tm;

    private DataSource _ds;

    private Object _dbdictionary;

    private DataSource _unmanagedDS;

    public BPELDAOConnectionFactoryImpl() {
    }

    public BpelDAOConnection getConnection() {
        return new BPELDAOConnectionImpl(_emf.createEntityManager());
    }

    public void init(Properties properties) {
        HashMap<String, Object> propMap = new HashMap<String,Object>();

        propMap.put("openjpa.ManagedRuntime", new TxMgrProvider());
//        propMap.put("openjpa.ConnectionDriverName", org.apache.derby.jdbc.EmbeddedDriver.class.getName());
        propMap.put("javax.persistence.nonJtaDataSource", _unmanagedDS == null ? _ds : _unmanagedDS);
        propMap.put("javax.persistence.DataSource", _ds);
        propMap.put("openjpa.Log", "DefaultLevel=TRACE");
        propMap.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");
        if (_dbdictionary != null)
            propMap.put("openjpa.jdbc.DBDictionary", _dbdictionary);

        if (properties != null)
            for (Map.Entry me : properties.entrySet())
                propMap.put((String)me.getKey(),me.getValue());
        
        _emf = Persistence.createEntityManagerFactory("ode-dao", propMap);
    }

    public void setTransactionManager(TransactionManager tm) {
        _tm = tm;
    }

    public void setDataSource(DataSource datasource) {
        _ds = datasource;

    }

    public void setDBDictionary(String dbd) {
        _dbdictionary = dbd;
    }

    public void setTransactionManager(Object tm) {
        _tm = (TransactionManager) tm;
        
    }

    public void setUnmanagedDataSource(DataSource ds) {
        _unmanagedDS = ds;
    }

    public void shutdown() {
        _emf.close();
    }
    

    private class TxMgrProvider implements ManagedRuntime {
        public TxMgrProvider() {
        }

        public TransactionManager getTransactionManager() throws Exception {
            return _tm;
        }
    }

}
