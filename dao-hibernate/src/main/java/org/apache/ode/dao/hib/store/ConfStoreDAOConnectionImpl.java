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
package org.apache.ode.dao.hib.store;

import org.apache.ode.dao.hib.store.hobj.VersionTrackerDAOImpl;
import org.apache.ode.dao.hib.store.hobj.DeploymentUnitDaoImpl;
import org.apache.ode.dao.hib.store.hobj.ProcessConfDaoImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.DeploymentUnitDAO;
import org.apache.ode.dao.store.ProcessConfDAO;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;
import org.apache.ode.dao.hib.SessionManager;

/**
 * Connection to a Hibernate data store. Essentially a thin wrapper around Hibernate's
 * {@link org.hibernate.Session} interface.
 * @author mriou <mriou at apache dot org>
 */
public class ConfStoreDAOConnectionImpl implements ConfStoreDAOConnection {

  private static final Log __log = LogFactory.getLog(ConfStoreDAOConnectionImpl.class);
  SessionManager _sm;
  static final ThreadLocal<ConfStoreDAOConnectionImpl> _connections = new ThreadLocal<ConfStoreDAOConnectionImpl>();


  public ConfStoreDAOConnectionImpl(SessionManager session) {
    _sm = session;
  }

  public ProcessConfDAO getProcess(QName pid) {
    _sm.begin();
    try {
      ProcessConfDAO ret = (ProcessConfDaoImpl) _sm.getSession().get(ProcessConfDaoImpl.class, pid.toString());
      _sm.commit();
      return ret;
    } catch (HibernateException e) {
      __log.error("DbError", e);
      _sm.rollback();
      throw e;
    }
  }

  public DeploymentUnitDAO createDeploymentUnit(String name) {
    DeploymentUnitDaoImpl du = new DeploymentUnitDaoImpl();
    du.setName(name);
    du.setDeployDate(new Date());
    _sm.getSession().save(du);
    return du;
  }

  public DeploymentUnitDAO getDeploymentUnit(String name) {
    try {
      DeploymentUnitDaoImpl du = (DeploymentUnitDaoImpl) _sm.getSession().get(DeploymentUnitDaoImpl.class, name);
      return du;
    } catch (HibernateException e) {
      __log.error("DbError", e);
      throw e;
    }
  }

  public long getNextVersion() {
    VersionTrackerDAOImpl vt = (VersionTrackerDAOImpl) _sm.getSession().createQuery("from VersionTrackerDAOImpl v ").uniqueResult();
    if (vt == null) {
      return 1;
    } else {
      return vt.getVersion() + 1;
    }
  }

  public void setVersion(long version) {
    VersionTrackerDAOImpl vt = (VersionTrackerDAOImpl) _sm.getSession().createQuery("from VersionTrackerDAOImpl v ").uniqueResult();
    if (vt == null) {
      vt = new VersionTrackerDAOImpl();
      vt.setId(1);
    }
    vt.setVersion(version);
    _sm.getSession().save(vt);
  }

  @SuppressWarnings("unchecked")
  public Collection<DeploymentUnitDAO> getDeploymentUnits() {
    Criteria c = _sm.getSession().createCriteria(DeploymentUnitDaoImpl.class);
    return c.list();
  }

  public void close() {
    
  }

  public boolean isClosed() {
    return _sm.isClosed();
  }

  public static ThreadLocal<ConfStoreDAOConnectionImpl> getThreadLocal() {
        return _connections;
  }
    
}
