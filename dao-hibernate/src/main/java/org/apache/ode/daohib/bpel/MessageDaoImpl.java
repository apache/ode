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

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HLargeData;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.utils.DOMUtils;
import org.hibernate.Session;
import org.w3c.dom.Element;

public class MessageDaoImpl extends HibernateDao implements MessageDAO {

    private HMessage _hself;
    private Session _session;

    protected MessageDaoImpl(SessionManager sessionManager, HMessage hobj) {
        super(sessionManager, hobj);
        _hself = hobj;
        _session = sessionManager.getSession();
    }

    public void setType(QName type) {
        _hself.setType(type == null ? null : type.toString());
    }

    public QName getType() {
        return _hself.getType() == null ? null : QName.valueOf(_hself.getType());
    }

    public void setData(Element value) {
        if (value == null) return;
        if (_hself.getMessageData() != null)
            _session.delete(_hself.getMessageData());
        HLargeData newdata = new HLargeData(DOMUtils.domToString(value));
        _session.save(newdata);
        _hself.setMessageData(newdata);
        update();
    }

    public Element getData() {
        if (_hself.getMessageData() == null)
            return null;
        try {
            return DOMUtils.stringToDOM(_hself.getMessageData().getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
