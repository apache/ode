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

package org.apache.ode.bpel.dao;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Representation of a message (i.e. request/response) in the database.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public interface MessageDAO {

    /** Set the message type (i.e. the <wsdl:message> type). */
    void setType(QName type);

    /** Get the message type (i.e. the <wsdl:message> type). */
    QName getType();

    /** Set the message data (content). */
    void setData(Element value);

    /** Get the message data (content). */
    Element getData();

}
