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

package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * A very simple, in-memory implementation of the
 * {@link org.apache.ode.bpel.dao.PartnerLinkDAO} interface.
 */
public class PartnerLinkDAOImpl extends DaoBaseImpl implements PartnerLinkDAO {

    private int _modelId;

    private String _linkName;

    private String _myRoleName;

    private QName _myRoleServiceName;

    private String _partnerRoleName;

    private Element _myEpr;

    private Element _partnerEpr;

    private String _partnerSessionId;

    private String _mySessionId;

    public String getPartnerLinkName() {
        return _linkName;
    }

    public void setPartnerLinkName(String partnerLinkName) {
        _linkName = partnerLinkName;
    }

    public int getPartnerLinkModelId() {
        return _modelId;
    }

    public void setPartnerLinkModelId(int modelId) {
        _modelId = modelId;
    }

    public String getMyRoleName() {
        return _myRoleName;
    }

    public QName getMyRoleServiceName() {
        return _myRoleServiceName;
    }

    public void setMyRoleServiceName(QName svcName) {
        _myRoleServiceName = svcName;
    }

    public void setMyRoleName(String myRoleName) {
        _myRoleName = myRoleName;
    }

    public String getPartnerRoleName() {
        return _partnerRoleName;
    }

    public void setPartnerRoleName(String partnerRoleName) {
        _partnerRoleName = partnerRoleName;
    }

    public Element getMyEPR() {
        return _myEpr;
    }

    public void setMyEPR(Element myEpr) {
        _myEpr = myEpr;
    }

    public Element getPartnerEPR() {
        return _partnerEpr;
    }

    public void setPartnerEPR(Element partnerEpr) {
        _partnerEpr = partnerEpr;
    }

    public String getMySessionId() {
        return _mySessionId;
    }

    public String getPartnerSessionId() {
        return _partnerSessionId;
    }

    public void setPartnerSessionId(String session) {
        _partnerSessionId = session;
    }

    public void setMySessionId(String sessionId) {
        _mySessionId = sessionId;
    }

}
