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
package org.apache.ode.bpel.extvar.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides methods to serialize (into XML) the name-value pairs of an external-variable key map.
 *
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class ExternalVariableKeyMapSerializer {

    /**
     * Convert to XML from map.
     * @param map
     * @return
     */
    public static Element toXML(Map<String,String> map) {
        Document doc = DOMUtils.newDocument();
        Element el = doc.createElementNS(null,"external-variable-ref");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Element nvel = doc.createElementNS(null,"nvpair");
            nvel.setAttribute("key", entry.getKey());
            if (entry.getValue() != null)
                nvel.setAttribute("value", entry.getValue());
            el.appendChild(nvel);
        }
        return el;

    }

    /**
     * Convert to map from XML.
     * @param el
     * @return
     */
    public static Map<String, String> toMap(Element el) {
        HashMap<String,String> ret = new HashMap<String,String>();
        if (el == null)
            return ret;
        NodeList nvs = el.getChildNodes();
        for (int i = 0; i < nvs.getLength(); ++i) {
            Node n = nvs.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if (!n.getLocalName().equals("nvpair"))
                continue;
            String key = ((Element)n).getAttribute("key");
            String val = ((Element)n).getAttribute("value");
            ret.put(key, val);
        }
        return ret;
    }

}
