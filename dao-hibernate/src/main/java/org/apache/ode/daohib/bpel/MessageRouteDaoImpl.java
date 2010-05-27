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

import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.hibernate.Query;

/**
 * Hibernate-based {@link MessageRouteDAO} implementation.
 */
class MessageRouteDaoImpl extends HibernateDao implements MessageRouteDAO {

    private static final String LOCK_INSTANCE = "update " + HProcessInstance.class.getName()
            + " set lock=lock+1 where id=?";

    private HCorrelatorSelector _selector;

    private boolean _locked = false;

    public MessageRouteDaoImpl(SessionManager sm, HCorrelatorSelector hobj) {
        super(sm, hobj);
        entering("MessageRouteDaoImpl.MessageRouteDaoImpl");
        _selector = hobj;
    }

    /**
     * @see org.apache.ode.bpel.dao.MessageRouteDAO#getTargetInstance()
     */
    public ProcessInstanceDAO getTargetInstance() {
        entering("MessageRouteDaoImpl.getTargetInstance");
        // First we need to reliably lock the instance:
        if (!_locked) {
            Query q = getSession().createQuery(LOCK_INSTANCE);
            q.setLong(0, _selector.getInstance().getId());
            q.executeUpdate();
            _locked = true;
        }

        // now it is safe to return
        return new ProcessInstanceDaoImpl(_sm, _selector.getInstance());
    }

    public String getGroupId() {
        entering("MessageRouteDaoImpl.getGroupId");
        return _selector.getGroupId();
    }

    public int getIndex() {
        entering("MessageRouteDaoImpl.getIndex");
        return _selector.getIndex();
    }

    public String getRoute() {
        return _selector.getRoute();
    }

    public CorrelationKeySet getCorrelationKeySet() {
        return new CorrelationKeySet(_selector.getCorrelationKey());
    }

    public void setCorrelationKeySet(CorrelationKeySet keySet) {
        _selector.setCorrelationKey(keySet.toCanonicalString());
    }

    public void setCorrelationKey(CorrelationKey key) {
         _selector.setCorrelationKey(key.toCanonicalString());
     }

     public CorrelationKey getCorrelationKey() {
         return new CorrelationKey(_selector.getCorrelationKey());
     }

}
