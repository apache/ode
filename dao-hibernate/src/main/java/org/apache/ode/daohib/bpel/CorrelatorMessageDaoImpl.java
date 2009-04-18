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

import org.apache.ode.bpel.dao.CorrelatorMessageDAO;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage;
import org.apache.ode.daohib.SessionManager;

public class CorrelatorMessageDaoImpl extends HibernateDao implements CorrelatorMessageDAO {

    private HCorrelatorMessage _hobj;

    public CorrelatorMessageDaoImpl(SessionManager sm, HCorrelatorMessage hobj) {
        super(sm, hobj);
        entering("CorrelatorDaoImpl.CorrelatorDaoImpl");
        _hobj = hobj;
    }

    public CorrelationKey getCorrelationKey() {
        return new CorrelationKey(_hobj.getCorrelationKey());
    }

    public void setCorrelationKey(CorrelationKey ckey) {
        _hobj.setCorrelationKey(ckey.toCanonicalString());
    }
}
