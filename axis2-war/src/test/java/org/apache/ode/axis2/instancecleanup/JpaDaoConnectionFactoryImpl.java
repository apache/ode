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

import java.util.Properties;

import javax.persistence.EntityManager;

import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessInstanceProfileDAO;
import org.apache.ode.bpel.dao.ProcessProfileDAO;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.dao.jpa.BPELDAOConnectionImpl;
import org.apache.ode.dao.jpa.ProcessDAOImpl;
import org.apache.ode.dao.jpa.ProcessInstanceDAOImpl;
import org.apache.ode.dao.jpa.ProcessInstanceProfileDAOImpl;
import org.apache.ode.dao.jpa.ProcessProfileDAOImpl;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public class JpaDaoConnectionFactoryImpl extends BPELDAOConnectionFactoryImpl implements PersistListener {
    private static ProcessInstanceDAO instance;
    private static ProcessDAO process;

    public static ProcessInstanceDAO getInstance() {
        return instance;
    }

    public static ProcessDAO getProcess() {
        return process;
    }

    @Override
    public void init(Properties properties) {
        super.init(properties);
        if( _emf instanceof OpenJPAEntityManagerFactorySPI ) {
            ((OpenJPAEntityManagerFactorySPI)_emf).addLifecycleListener(this, ProcessInstanceDAOImpl.class, ProcessDAOImpl.class);
        }
    }

    @Override
    protected BPELDAOConnectionImpl createBPELDAOConnection(EntityManager em) {
        return new ProfilingBPELDAOConnectionImpl(em);
    }

    public void afterPersist(LifecycleEvent event) {
        if( event.getSource() instanceof ProcessInstanceDAOImpl ) {
            instance = (ProcessInstanceDAOImpl)event.getSource();
        } else {
            process = (ProcessDAOImpl)event.getSource();
        }
    }

    public void beforePersist(LifecycleEvent event) {
    }

    public static class ProfilingBPELDAOConnectionImpl extends BPELDAOConnectionImpl implements ProfilingBpelDAOConnection {
        public ProfilingBPELDAOConnectionImpl(EntityManager em) {
            super(em);
        }

        public ProcessProfileDAO createProcessProfile(ProcessDAO process) {
            return new ProcessProfileDAOImpl(_em, (ProcessDAOImpl)process);
        }

        public ProcessInstanceProfileDAO createProcessInstanceProfile(ProcessInstanceDAO instance) {
            return new ProcessInstanceProfileDAOImpl(_em, (ProcessInstanceDAOImpl)instance);
        }
    }
}