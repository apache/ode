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
package org.apache.ode.bpel.compiler.bom;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;

/**
 * BPEL correlation set declaration. A correlation set is--like a
 * variable--declared in a scope-like construct (see {@link Scope}.
 */
public class CorrelationSet extends BpelObject {

    private QName[] _props;

    public CorrelationSet(Element el) {
        super(el);
    }

    /**
     * Get the name of this correlation set.
     *
     * @return correlation set name
     */
    public String getName() {
        return getAttribute("name", null);
    }

    /**
     * Get the (ordered) set of properties that define this correlation set.
     * Properties are returned by their qualified name.
     *
     * @return set of defining properties
     */
    public QName[] getProperties() {
        if (_props == null) {
            StringTokenizer st = new StringTokenizer(getAttribute("properties", ""));
            ArrayList<QName> al = new ArrayList<QName>();
            NSContext nsc = getNamespaceContext();
            for (; st.hasMoreTokens();) {
                String token = st.nextToken();
                if (token.startsWith("{")) {
                    String namespace = token.substring(1, token.indexOf("}"));
                    String localname = token.substring(token.indexOf("}") + 1, token.length());
                    al.add(new QName(namespace, localname));
                } else {
                    al.add(nsc.derefQName(token));
                }
            }
            _props = al.toArray(new QName[] {});
        }
        return _props;
    }

}
