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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.ContextValueDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HContextValue;
import org.apache.ode.daohib.bpel.hobj.HLargeData;
import org.apache.ode.daohib.bpel.hobj.HPartnerLink;
import org.apache.ode.utils.DOMUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.w3c.dom.Element;

/**
 * Hibernate based {EndpointReferenceDAO} implementation. can either be related
 * to a scope (when it's specific to a scope instance, for example because it
 * has been assigned during the instance execution) or to a process definition
 * (general endpoint configuration).
 */
public class PartnerLinkDAOImpl extends HibernateDao implements PartnerLinkDAO {
    private static final String QRY_DEL_CTX_VALUES = "delete from " + HContextValue.class.getName()
    + " where namespace = ? and key = ?".intern();


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
            return DOMUtils.stringToDOM(_self.getMyEPR().getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setMyEPR(Element val) {
        entering("PartnerLinkDAOImpl.setMyEPR");
        _myEPR = val;
        if (_self.getMyEPR() != null)
            _sm.getSession().delete(_self.getMyEPR());
        if (val == null) {
            _self.setMyEPR(null);
        } else {
            HLargeData ld = new HLargeData(DOMUtils.domToString(val));
            getSession().save(ld);
            _self.setMyEPR(ld);
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
            return _partnerEPR = DOMUtils.stringToDOM(_self.getPartnerEPR().getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPartnerEPR(Element val) {
        entering("PartnerLinkDAOImpl.setPartnerEPR");
        _partnerEPR = val;
        if (_self.getPartnerEPR() != null)
            _sm.getSession().delete(_self.getPartnerEPR());
        if (val == null) {
            _self.setPartnerEPR(null);
        } else {
            HLargeData ld = new HLargeData(DOMUtils.domToString(val));
            getSession().save(ld);
            _self.setPartnerEPR(ld);
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

    public Collection<ContextValueDAO> getContextValues() {
        entering("PartnerLinkDAOImpl.getContextValues");
        Set<ContextValueDAO> results = new HashSet<ContextValueDAO>();
        for (HContextValue hContextValue : _self.getContextValues()) {
        	results.add(new ContextValueDaoImpl(_sm, hContextValue));
        }
        return results;
    }
    
	public void removeContextValue(String namespace, String key) {
        entering("PartnerLinkDAOImpl.removeContextValue");
        Session session = getSession();
        Query q = session.createQuery(QRY_DEL_CTX_VALUES);
        q.setString(0, namespace); // namespace
        q.setString(0, key); // key
        q.executeUpdate();
        session.flush(); // explicit flush to ensure value removed.
	}

	public void setContextValue(String namespace, String key, String value) {
        entering("PartnerLinkDAOImpl.setContextValue");
        HContextValue hvalue = null;
        for (HContextValue c : _self.getContextValues()) {
            if (c.getNamespace().equals(namespace) && c.getKey().equals(key)) {
                hvalue = c;
            }
        }

		if (hvalue == null) {
	        hvalue = new HContextValue();
	        hvalue.setNamespace(namespace);
	        hvalue.setKey(key);
	        hvalue.setPartnerLink(_self);
	        _self.getContextValues().add(hvalue);
	        
	        hvalue.setLock(0);
	        hvalue.setCreated(new Date());
	        getSession().save(hvalue);
		}
		
        hvalue.setValue(value);
	}

}
