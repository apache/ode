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

import java.io.IOException;
import java.util.Date;

import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HActivityRecovery;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Hibernate based {@link ActivityRecoveryDao} implementation
 */
public class ActivityRecoveryDaoImpl extends HibernateDao implements ActivityRecoveryDAO {
    HActivityRecovery _self;

    public ActivityRecoveryDaoImpl(SessionManager sm, HActivityRecovery recovery) {
        super(sm, recovery);
        entering("ActivityRecoveryDaoImpl.ActivityRecoveryDaoImpl");
        _self = recovery;
    }

    public long getActivityId() {
        return _self.getActivityId();
    }

    public String getChannel() {
        return _self.getChannel();
    }

    public String getReason() {
        return _self.getReason();
    }

    public Date getDateTime() {
        return _self.getDateTime();
    }

    public Element getDetails() {
        entering("ActivityRecoveryDaoImpl.getDetails");
        if (_self.getDetails() == null) {
            return null;
        }
        try {
            return DOMUtils.stringToDOM(_self.getDetails());
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getActions() {
        return _self.getActions();
    }

    public String[] getActionsList() {
        return _self.getActions().split(" ");
    }

    public int getRetries() {
        return _self.getRetries();
    }
}
