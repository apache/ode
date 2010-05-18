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

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HPartnerLink;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Hibernate based {EndpointReferenceDAO} implementation. can either be related
 * to a scope (when it's specific to a scope instance, for example because it
 * has been assigned during the instance execution) or to a process definition
 * (general endpoint configuration).
 */
public class PartnerLinkDAOImpl extends HibernateDao implements PartnerLinkDAO {


    /** Cached copy of my epr */
    private Element _myEPR;

    /** Cached copy of partner epr. */
    private Element _partnerEPR;

    HPartnerLink _self;

    public PartnerLinkDAOImpl(SessionManager sessionManager, HPartnerLink hobj) {
        super(sessionManager, hobj);
        entering("PartnerLinkDAOImpl.PartnerLinkDAOImpl");
        _self = hobj;
    }

    public String getPartnerLinkName() {
        return _self.getLinkName();
    }

    public String getPartnerRoleName() {
        return _self.getPartnerRole();
    }

    public String getMyRoleName() {
        return _self.getMyRole();
    }

    public int getPartnerLinkModelId() {
        return _self.getModelId();
    }

    public QName getMyRoleServiceName() {
        return _self.getServiceName() == null ? null : QName.valueOf(_self.getServiceName());
    }

    public void setMyRoleServiceName(QName svcName) {
        entering("PartnerLinkDAOImpl.setMyRoleServiceName");
        _self.setServiceName(svcName == null ? null : svcName.toString());
        update();
    }

    public Element getMyEPR() {
        entering("PartnerLinkDAOImpl.getMyEPR");
        if (_myEPR != null)
            return _myEPR;
        if (_self.getMyEPR() == null)
            return null;
        try {
            return DOMUtils.stringToDOM(_self.getMyEPR());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setMyEPR(Element val) {
        entering("PartnerLinkDAOImpl.setMyEPR");
        _myEPR = val;
        if (val == null) {
            _self.setMyEPR(null);
        } else {
            _self.setMyEPR(DOMUtils.domToBytes(val));
        }
        getSession().update(_self);
    }

    public Element getPartnerEPR() {
        entering("PartnerLinkDAOImpl.getPartnerEPR");
        if (_partnerEPR != null)
            return _partnerEPR;
        if (_self.getPartnerEPR() == null)
            return null;
        try {
            return _partnerEPR = DOMUtils.stringToDOM(_self.getPartnerEPR());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPartnerEPR(Element val) {
        entering("PartnerLinkDAOImpl.setPartnerEPR");
        _partnerEPR = val;
        if (val == null) {
            _self.setPartnerEPR(null);
        } else {
            _self.setPartnerEPR(DOMUtils.domToBytes(val));
        }
        getSession().update(_self);
    }

    public String getMySessionId() {
        return _self.getMySessionId();
    }

    public String getPartnerSessionId() {
        return _self.getPartnerSessionId();
    }

    public void setPartnerSessionId(String session) {
        entering("PartnerLinkDAOImpl.setPartnerSessionId");
        _self.setPartnerSessionId(session);
    }

    public void setMySessionId(String sessionId) {
        entering("PartnerLinkDAOImpl.setMySessionId");
        _self.setMySessionId(sessionId);

    }

}
