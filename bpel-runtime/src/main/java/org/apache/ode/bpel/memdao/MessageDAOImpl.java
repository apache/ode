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

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public class MessageDAOImpl extends DaoBaseImpl implements MessageDAO {
    private QName type;
    private Element data;
    private Element header;
    private MessageExchangeDAO messageExchange;

    public MessageDAOImpl(MessageExchangeDAO messageExchange) {
        this.messageExchange = messageExchange;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public QName getType() {
        return type;
    }

    public void setData(Element value) {
        this.data = value;
    }

    public Element getData() {
        if (data == null) data = DOMUtils.newDocument().getDocumentElement();
        return data;
    }

    public void setHeader(Element value) {
        this.header = value;
    }

    public Element getHeader() {
        if ( header == null ) header = DOMUtils.newDocument().getDocumentElement();
        return header;
    }

    public MessageExchangeDAO getMessageExchange() {
        return messageExchange;
    }

}
