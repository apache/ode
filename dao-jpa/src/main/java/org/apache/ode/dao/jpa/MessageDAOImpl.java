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

package org.apache.ode.dao.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;

@Entity
@Table(name = "ODE_MESSAGE")
@NamedQueries( { @NamedQuery(name = MessageDAOImpl.DELETE_MESSAGES_BY_PROCESS, query = "delete from MessageDAOImpl as m where m._messageExchange._process = :process") })
public class MessageDAOImpl implements MessageDAO {
    private static Log __log = LogFactory.getLog(MessageDAOImpl.class);
    public final static String DELETE_MESSAGES_BY_PROCESS = "DELETE_MESSAGES_BY_PROCESS";

    @Id
    @Column(name = "MESSAGE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Long _id;
    @Basic
    @Column(name = "TYPE")
    private String _type;
    @Lob
    @Column(name = "DATA")
    private String _data;
    @Lob
    @Column(name = "HEADER")
    private String _header;
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @Column(name = "MESSAGE_EXCHANGE_ID")
    private MessageExchangeDAOImpl _messageExchange;

    public MessageDAOImpl() {
    }

    public MessageDAOImpl(QName type, MessageExchangeDAOImpl me) {
        _type = type.toString();
        _messageExchange = me;
    }

    public Element getData() {
        if (__log.isDebugEnabled()) {
            __log.debug("getData " + _id + " " + _data);
        }
        try {
            return _data == null ? null : DOMUtils.stringToDOM(_data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setData(Element value) {
        if (value == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("setData " + _id + " null");
            }
            return;
        }
        _data = DOMUtils.domToString(value);
        
        if (__log.isDebugEnabled()) {
            __log.debug("setData " + _id + " " + _data);
        }
    }

    public Element getHeader() {
        try {
            return _header == null ? null : DOMUtils.stringToDOM(_header);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setHeader(Element value) {
        if (value == null)
            return;
        _header = DOMUtils.domToString(value);
    }

    public MessageExchangeDAO getMessageExchange() {
        return _messageExchange;
    }

    public QName getType() {
        return _type == null ? null : QName.valueOf(_type);
    }

    public void setType(QName type) {
        _type = type.toString();
    }

}
