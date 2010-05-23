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

package org.apache.ode.axis2.instancecleanup;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.ode.dao.bpel.CorrelationSetDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.ProcessDAO;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ProcessInstanceProfileDAO;
import org.apache.ode.dao.bpel.ProcessProfileDAO;
import org.apache.ode.dao.hib.SessionManager;
import org.apache.ode.dao.hib.bpel.BpelDAOConnectionFactoryImpl;
import org.apache.ode.dao.hib.bpel.BpelDAOConnectionImpl;
import org.apache.ode.dao.hib.bpel.ProcessDaoImpl;
import org.apache.ode.dao.hib.bpel.ProcessInstanceDaoImpl;
import org.apache.ode.dao.hib.bpel.ProcessInstanceProfileDaoImpl;
import org.apache.ode.dao.hib.bpel.ProcessProfileDaoImpl;
import org.apache.ode.dao.hib.bpel.hobj.HProcess;
import org.apache.ode.dao.hib.bpel.hobj.HProcessInstance;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;

@SuppressWarnings("serial")
public class HibDaoConnectionFactoryImpl extends BpelDAOConnectionFactoryImpl implements PostInsertEventListener {
    private static SessionManager _staticSessionManager;
    private static ProcessInstanceDaoImpl instance;
    private static ProcessDaoImpl process;
    
    @Override
    public void init(Properties initialProps, TransactionManager mgr, Object env) {
        _ds = (DataSource) env;
        _txm = mgr;
        Configuration conf = SessionManager.getDefaultConfiguration();
        conf.setListener("post-insert", HibDaoConnectionFactoryImpl.this);
        _sessionManager = setupSessionManager(conf, initialProps, _txm, _ds);
        _staticSessionManager = _sessionManager;
    }

    public BpelDAOConnection getConnection() {
        return new ProfilingBpelDAOConnectionImpl(_sessionManager);
    }
    
    public static Session getSession() {
        return _staticSessionManager.getSession();
    }
    
    public static ProcessInstanceDAO getInstance() {
        return instance;
    }
    
    public static ProcessDaoImpl getProcess() {
        return process;
    }

    public void onPostInsert(PostInsertEvent e) {
        if( HProcessInstance.class.equals( e.getEntity().getClass() ) ) {
            instance = new ProcessInstanceDaoImpl(_sessionManager, (HProcessInstance)e.getEntity());
        } else if( HProcess.class.equals( e.getEntity().getClass() ) ) {
            process = new ProcessDaoImpl(_sessionManager, (HProcess)e.getEntity());
        }
    }

    public static class ProfilingBpelDAOConnectionImpl extends BpelDAOConnectionImpl implements ProfilingBpelDAOConnection {
        public ProfilingBpelDAOConnectionImpl(SessionManager sm) {
            super(sm);
        }
        
        public ProcessProfileDAO createProcessProfile(ProcessDAO process) {
            return new ProcessProfileDaoImpl(_sm, (ProcessDaoImpl)process);
        }

        public ProcessInstanceProfileDAO createProcessInstanceProfile(ProcessInstanceDAO instance) {
            return new ProcessInstanceProfileDaoImpl(_sm, (ProcessInstanceDaoImpl)instance);
        }

    }
}