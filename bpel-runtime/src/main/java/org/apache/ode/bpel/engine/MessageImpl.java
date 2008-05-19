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

package org.apache.ode.bpel.engine;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.utils.DOMUtils;

/**
 * Implementation of the {@link Message} interface. 
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
abstract class MessageImpl implements Message {

    boolean _readOnly = false;

    public Element getPart(String partName) {
        Element message = getMessage();
        NodeList eltList = message.getElementsByTagName(partName);
        if (eltList.getLength() == 0) return null;
        else return (Element) eltList.item(0);
    }

    public void setPart(String partName, Element content) {
        Element message = getMessage();
        if (message == null) {
            Document doc = DOMUtils.newDocument();
            message = doc.createElement("message");
            doc.appendChild(message);
        }
        message.appendChild(message.getOwnerDocument().importNode(content, true));
        setMessage(message);
    }

    public Element getHeaderPart(String partName) {
        Element header = getHeader();
        if (header == null) return null;

        NodeList eltList = header.getElementsByTagName(partName);
        if (eltList.getLength() == 0) return null;
        else return (Element) eltList.item(0);
    }

    public void setHeaderPart(String name, Element content) {
        Element header =  getHeader();
        if (header == null) {
            Document doc = DOMUtils.newDocument();
            header = doc.createElement("header");
            doc.appendChild(header);
        }
        Element part = header.getOwnerDocument().createElement(name);
        header.appendChild(part);
        part.appendChild(header.getOwnerDocument().importNode(content, true));
        setHeader(header);
      }

    public abstract void setMessage(Element msg);

    public abstract Element getMessage();

    public abstract void setHeader(Element msg);

    public abstract Element getHeader();

    public abstract QName getType();

    public List<String> getParts() {
        ArrayList<String> parts = new ArrayList<String>();
        Element message = getMessage();
        NodeList nodeList = message.getChildNodes();
        for (int m = 0; m < nodeList.getLength(); m++) {
            Node node = nodeList.item(m);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                parts.add(node.getLocalName());
        }
        return parts;
    }

    public Map<String, Element> getHeaderParts() {
        HashMap<String,Element> l = new HashMap<String,Element>();
        Element header =  getHeader();
        if (header != null) {
            NodeList children = header.getChildNodes();
            for (int m = 0; m < children.getLength(); m++)
                if (children.item(m).getNodeType() == Node.ELEMENT_NODE) {
                    Element part = (Element) children.item(m);
                    l.put(part.getLocalName(), DOMUtils.getFirstChildElement(part));
                }
        }
        return l;
    }
    
    protected void makeReadOnly() {
        _readOnly = true;
    }
    
    protected void checkWrite() {
        if (_readOnly)
            throw new IllegalStateException("write attempted to read-only message.");
    }
}
