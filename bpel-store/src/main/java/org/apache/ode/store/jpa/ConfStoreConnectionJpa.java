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

import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.DeploymentUnitDAO;

import javax.persistence.EntityManager;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ConfStoreConnectionJpa implements ConfStoreConnection {

    private EntityManager _em;
    static final ThreadLocal<EntityManager> _current = new ThreadLocal<EntityManager>();


    public ConfStoreConnectionJpa(EntityManager em) {
        _em = em;
        _current.set(em);
    }

    public void begin() {
        _em.getTransaction().begin();
    }

    public void close() {
    }

    public void commit() {
        _em.getTransaction().commit();
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

    public void rollback() {
        _em.getTransaction().rollback();
    }

    public int getNextVersion(QName processName) {
        VersionTrackerDAOImpl vt = _em.find(VersionTrackerDAOImpl.class,processName.toString());
        if (vt == null) return 1;
        else return vt.getVersion() + 1;
    }

    public void setVersion(QName processName, int version) {
        VersionTrackerDAOImpl vt = _em.find(VersionTrackerDAOImpl.class,processName.toString());
        if (vt == null) {
            vt = new VersionTrackerDAOImpl();
            vt.setNamespace(processName.toString());
        }
        vt.setVersion(version);
        _em.persist(vt);
    }
}
