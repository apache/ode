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

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.hibernate.cfg.Environment;

import java.util.Properties;

/**
 * Testing BpelDAOConnectionImpl.listInstance. We're just producing a lot
 * of different filter combinations and test if they execute ok. To really
 * test that the result is the one expected would take a huge test database
 * (with at least a process and an instance for every possible combination).
 */
public class BaseTestDAO extends TestCase {

    protected BpelDAOConnection daoConn;
    private TransactionManager txm;
    private DataSource ds;

    protected void initTM() throws Exception {
        EmbeddedGeronimoFactory factory = new EmbeddedGeronimoFactory();
        txm = factory.getTransactionManager();
        ds = getDataSource();
        txm.begin();

        BpelDAOConnectionFactoryImpl factoryImpl = new BpelDAOConnectionFactoryImpl();
        factoryImpl.setTransactionManager(txm);
        factoryImpl.setDataSource(ds);
        Properties props = new Properties();
        props.put(Environment.HBM2DDL_AUTO, "create-drop");
        factoryImpl.init(props);

        daoConn = factoryImpl.getConnection();
    }

    protected void stopTM() throws Exception {
        txm.commit();
    }

    protected DataSource getDataSource() {
        if (ds == null) {
            jdbcDataSource hsqlds = new jdbcDataSource();
            hsqlds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
            hsqlds.setUser("sa");
            hsqlds.setPassword("");
            ds = hsqlds;
        }
        return ds;
    }

    protected TransactionManager getTransactionManager() {
        return txm;
    }

}