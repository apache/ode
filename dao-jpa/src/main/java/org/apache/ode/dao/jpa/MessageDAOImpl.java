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


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;


@Entity
@Table(name="ODE_MESSAGE")
public class MessageDAOImpl implements MessageDAO {

    @Id @Column(name="MESSAGE_ID")
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long _id;
    @Basic @Column(name="TYPE")
    private String _type;
    @Lob @Column(name="DATA")
    private String _data;
    @Lob @Column(name="HEADER")
    private String _header;
    @Transient
    private Element _element;
    @Transient
    private Element _headerElement;

    public MessageDAOImpl() {

    }
    public MessageDAOImpl(QName type, MessageExchangeDAOImpl me) {
        if (_type != null) _type = type.toString();
    }

    public Element getData() {
        if ( _element == null && _data != null && !"".equals(_data) ) {
            try {
                _element = DOMUtils.stringToDOM(_data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return _element;
    }

    public QName getType() {
        return _type == null ? null : QName.valueOf(_type);
    }

    public void setData(Element value) {
        if (value == null) return;
        _data = DOMUtils.domToString(value);
        _element = value;
    }

    public void setType(QName type) {
        _type = type.toString();
    }

    public Element getHeader() {
        if ( _headerElement == null && _header != null && !"".equals(_header)) {
            try {
                _headerElement = DOMUtils.stringToDOM(_header);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return _headerElement;
    }

    public void setHeader(Element value) {
        if (value == null) return;
        _header = DOMUtils.domToString(value);
        _headerElement = value;
    }

}
