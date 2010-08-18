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

package org.apache.ode.dao.jpa.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.jpa.JpaConnection;
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.DeploymentUnitDAO;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ConfStoreDAOConnectionImpl extends JpaConnection implements ConfStoreDAOConnection {
	
	private static Log LOG = LogFactory.getLog(ConfStoreDAOConnectionImpl.class);
	
    static final ThreadLocal<ConfStoreDAOConnectionImpl> _connections = new ThreadLocal<ConfStoreDAOConnectionImpl>();


    public ConfStoreDAOConnectionImpl(EntityManager mgr, TransactionManager txMgr, JpaOperator operator) {
        super(mgr, txMgr, operator);
    }

    public void close() {
    }

    public DeploymentUnitDAO createDeploymentUnit(String name) {
    	_txCtx.begin();
        DeploymentUnitDaoImpl du = new DeploymentUnitDaoImpl();
        du.setName(name);
        du.setDeployDate(new Date());
        _em.persist(du);
        _txCtx.commit();
        return du;
    }

    public DeploymentUnitDAO getDeploymentUnit(String name) {
    	_txCtx.begin();
    	DeploymentUnitDAO dao= _em.find(DeploymentUnitDaoImpl.class, name);
        _txCtx.commit();
        return dao;
    }

    public Collection<DeploymentUnitDAO> getDeploymentUnits() {
    	_txCtx.begin();
    	Collection<DeploymentUnitDAO> dao = _em.createQuery("SELECT du from DeploymentUnitDaoImpl du").getResultList();
        _txCtx.commit();
        return dao;
    }

    public long getNextVersion() {
    	_txCtx.begin();
        List<VersionTrackerDAOImpl> res = _em.createQuery("select v from VersionTrackerDAOImpl v").getResultList();
        _txCtx.commit();
        if (res.size() == 0) return 1;
        else {
            VersionTrackerDAOImpl vt = res.get(0);
            return vt.getVersion() + 1;
        }
    }

    public void setVersion(long version) {
    	_txCtx.begin();
        List<VersionTrackerDAOImpl> res = _em.createQuery("select v from VersionTrackerDAOImpl v").getResultList();
        VersionTrackerDAOImpl vt;
        if (res.size() == 0) vt = new VersionTrackerDAOImpl();
        else vt = res.get(0);
        vt.setVersion(version);
        _em.persist(vt);
        _txCtx.commit();
    }
    
    public static ThreadLocal<ConfStoreDAOConnectionImpl> getThreadLocal() {
        return _connections;
    }
}
