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
package org.apache.ode.bpel.iapi;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A representation of a WSDL-typed message. The implementation of this 
 * interface is provided by the integration layer. 
 */
public interface Message {

    /**
     * Get the message type.
     * @return message type.
     */
    QName getType();

    List<String> getParts();

    /**
     * Get a message part.
     * @param partName name of the part
     * @return named {@l
     */
    Element getPart(String partName);

    /**
     * Set the message part.
     * @param partName name of part
     * @param content part content
     */
    void setPart(String partName, Element content);

    /**
     * Get a header part.
     * @param partName name of the header part
     * @return named
     */
    Element getHeaderPart(String partName);

    /**
     * Set a header part element.
     * @param name header part name
     * @param content header part element
     */
    void setHeaderPart(String name, Element content);

    /**
     * Set a header part value
     * @param name header part name
     * @param content header part text content
     */
    void setHeaderPart(String name, String content);

    /**
     * Gets all header parts in the message.
     * @return
     */
    Map<String, Node> getHeaderParts();

    /**
     * Set the message as an element. The name of the element is irrelevant,
     * but it should have one child element for each message part.
     * TODO: remove this, temporary hack.
     */
    void setMessage(Element msg);

    /**
     * Get the message as an element. The returned element will have one
     * child element corresponding (and named after) each part in the message.
     * TODO: remove this, temporary hack.
     */
    Element getMessage();

}
