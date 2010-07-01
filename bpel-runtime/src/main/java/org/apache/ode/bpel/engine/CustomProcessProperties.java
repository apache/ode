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

import java.net.InetAddress;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CustomProcessProperties {
    private static final Log __log = LogFactory.getLog(CustomProcessProperties.class);

    public Node getProperty(QName propertyName) {
        String name = propertyName.getLocalPart();
        try { 
            if (name.equals("ode.localhost.name")) {
                return stringToNode(InetAddress.getLocalHost().getHostName());
            } else if (name.equals("ode.localhost.address")) {
                return stringToNode(InetAddress.getLocalHost().getHostAddress());
            } else {
                return null;
            }
        } catch (Exception e) {
            __log.warn("Can't evaluate property " + propertyName, e);
            return null;
        }
    }
    
    public static Node stringToNode(String s) {
        Document d = DOMUtils.newDocument();
        Element e = d.createElement("value");
        e.setTextContent(s);
        d.appendChild(e);
        return d.getDocumentElement();
    }
}
