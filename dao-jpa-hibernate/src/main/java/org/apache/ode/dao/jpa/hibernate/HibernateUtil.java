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
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * Manages datasource and transaction, for hibernate usage.
 * 
 */
public class HibernateUtil {
	

    public static final String PROP_GUID = "ode.hibernate.guid";

    private static final Map<String, TransactionManager> _txManagers =
            Collections.synchronizedMap(new HashMap<String, TransactionManager>());
    private static final Map<String, DataSource> _dataSources =
            Collections.synchronizedMap(new HashMap<String,DataSource>());

    private static final String[] CANNOT_JOIN_FOR_UPDATE_DIALECTS =
            {"org.hibernate.dialect.IngresDialect"};

    private final TransactionManager _txManager = null;


    TransactionManager getTransactionManager() {
        return _txManager;
    }

    public static void registerTransactionManager(String uuid, TransactionManager txm) {
        _txManagers.put(uuid, txm);
    }
    
    public static void registerDatasource(String uuid, DataSource ds){
    	_dataSources.put(uuid, ds);
    }


    public static TransactionManager getTransactionManager(Properties props) {
        String guid = props.getProperty(PROP_GUID);
        return _txManagers.get(guid);
    }

    public static Connection getConnection(Properties props) throws SQLException {
        String guid = props.getProperty(PROP_GUID);
        return _dataSources.get(guid).getConnection();
    }
}
