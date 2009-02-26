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

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.iapi.Scheduler;

import javax.xml.namespace.QName;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Connection factory for the in-memory state store.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {
    private static final Map<QName, ProcessDaoImpl> __StateStore = new HashMap<QName, ProcessDaoImpl>();

    private Scheduler _scheduler;
    private long _mexTtl = 10*60*1000;

    public BpelDAOConnectionFactoryImpl(Scheduler sched) {
        _scheduler = sched;
    }
    public BpelDAOConnectionFactoryImpl(Scheduler sched, long ttl) {
        _scheduler = sched;
        _mexTtl = ttl;
    }

    public BpelDAOConnection getConnection() {
        return new BpelDAOConnectionImpl(__StateStore, _scheduler, _mexTtl);
    }

    /**
     * @see org.apache.ode.bpel.dao.BpelDAOConnectionFactory#init(java.util.Properties)
     */
    public void init(Properties properties) {
    }

    public void shutdown() {
    }

    public DataSource getDataSource() {
        return null;
    }
}
