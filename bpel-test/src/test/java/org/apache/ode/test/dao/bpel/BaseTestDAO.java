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

package org.apache.ode.test.dao.bpel;

import java.util.Properties;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.apache.ode.dao.jpa.openjpa.BpelDAOConnectionFactoryImpl;

import org.apache.ode.il.EmbeddedGeronimoFactory;
//import org.hsqldb.jdbc.jdbcDataSource;

import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;


/**
 * Testing BpelDAOConnectionImpl.listInstance. We're just producing a lot
 * of different filter combinations and test if they execute ok. To really
 * test that the result is the one expected would take a huge test database
 * (with at least a process and an instance for every possible combination).
 */
public class BaseTestDAO extends TestCase {

    protected BpelDAOConnection daoConn;
    protected TransactionManager _txm;
    private Database _db;
    protected BpelDAOConnectionFactory _factoryImpl;

    protected void initTM() throws Exception {
        _txm = new EmbeddedGeronimoFactory().getTransactionManager();
        Properties props = new Properties();
        props.setProperty(OdeConfigProperties.PROP_DAOCF, System.getProperty(OdeConfigProperties.PROP_DAOCF,OdeConfigProperties.DEFAULT_DAOCF_CLASS));
        OdeConfigProperties odeProps = new OdeConfigProperties(props,"");
		_db = new Database(odeProps);
        _db.setTransactionManager(_txm);
        _db.start();
        //txm.begin();
        
        _factoryImpl = _db.createDaoCF();
        
        _txm.begin();
        daoConn = _factoryImpl.getConnection();
        
    }

    protected void stopTM() throws Exception {
     _txm.commit();
     daoConn.close();
      //txm.commit();
     _factoryImpl.shutdown();
     _db.shutdown();
      

    }

    protected TransactionManager getTransactionManager() {
        return _txm;
    }

}