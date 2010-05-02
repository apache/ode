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
package org.apache.ode.bpel.memdao;

import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Connection factory for the in-memory state store.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {
    private final Map<QName, ProcessDaoImpl> _StateStore = new HashMap<QName, ProcessDaoImpl>();
    private TransactionManager _txm;
    private BpelDAOConnectionImpl _conn; 

    public BpelDAOConnectionFactoryImpl() {
        
    }

    public BpelDAOConnection getConnection() {
        return _conn;
    }

    /**
     * @see org.apache.ode.dao.bpel.BpelDAOConnectionFactory#init(java.util.Properties, Object ctx)
     */
    public void init(Properties properties,TransactionManager mgr, Object ctx) {
      _txm = mgr;
      _conn = new BpelDAOConnectionImpl(_StateStore, _txm);
    }

    public void shutdown() {
    }

}
