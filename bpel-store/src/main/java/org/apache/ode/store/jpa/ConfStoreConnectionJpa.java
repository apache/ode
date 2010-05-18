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

package org.apache.ode.store.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.DeploymentUnitDAO;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ConfStoreConnectionJpa implements ConfStoreConnection {
	private static Log LOG = LogFactory.getLog(ConfStoreConnectionJpa.class);
	
    private EntityManager _em;
    static final ThreadLocal<EntityManager> _current = new ThreadLocal<EntityManager>();


    public ConfStoreConnectionJpa(EntityManager em) {
        _em = em;
        _current.set(em);
    }

    public void close() {
    }

    public DeploymentUnitDAO createDeploymentUnit(String name) {
        DeploymentUnitDaoImpl du = new DeploymentUnitDaoImpl();
        du.setName(name);
        du.setDeployDate(new Date());
        _em.persist(du);
        return du;
    }

    public DeploymentUnitDAO getDeploymentUnit(String name) {
        return _em.find(DeploymentUnitDaoImpl.class, name);
    }

    public Collection<DeploymentUnitDAO> getDeploymentUnits() {
        return _em.createQuery("SELECT du from org.apache.ode.store.jpa.DeploymentUnitDaoImpl du").getResultList();
    }

    public long getNextVersion() {
        List<VersionTrackerDAOImpl> res = _em.createQuery("select v from VersionTrackerDAOImpl v").getResultList();
        if (res.size() == 0) return 1;
        else {
            VersionTrackerDAOImpl vt = res.get(0);
            return vt.getVersion() + 1;
        }
    }

    public void setVersion(long version) {
        List<VersionTrackerDAOImpl> res = _em.createQuery("select v from VersionTrackerDAOImpl v").getResultList();
        VersionTrackerDAOImpl vt;
        if (res.size() == 0) vt = new VersionTrackerDAOImpl();
        else vt = res.get(0);
        vt.setVersion(version);
        _em.persist(vt);
    }
}
