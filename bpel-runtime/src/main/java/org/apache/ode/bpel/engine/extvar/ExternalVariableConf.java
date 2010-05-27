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
package org.apache.ode.bpel.engine.extvar;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

public class ExternalVariableConf {
    public final static QName EXTVARCONF_ELEMENT = new QName("http://ode.apache.org/externalVariables",
            "externalVariable");


    private final HashMap<String,Variable> _vars = new HashMap<String, Variable>();

    public ExternalVariableConf(List<Element> els) {

        for (Element el : els) {
            String varId = el.getAttribute("id");
            if (varId == null || "".equals(varId))
                throw new BpelEngineException("Invalid external variable configuration; id not specified.");
            if (_vars.containsKey(varId))
                throw new BpelEngineException("Invalid external variable configuration; duplicate id \""+  varId + " \".");


            Element child = DOMUtils.getFirstChildElement(el);
            if (child == null)
                throw new BpelEngineException("Invalid external variable configuration for id \"" + varId + "\"; no engine configuration!");

            QName engineQName = new QName(child.getNamespaceURI(), child.getLocalName());

            Variable var = new Variable(varId, engineQName, child);
            _vars.put(varId,var);
        }

    }


    public class Variable {
        public final String extVariableId;
        public final QName engineQName;
        public final Element configuration;

        Variable(String id, QName engine, Element config) {
            this.extVariableId = id;
            this.engineQName = engine;
            this.configuration = config;

        }
    }


    public Variable getVariable(String id) {
        return _vars.get(id);
    }

    public Collection<Variable> getVariables() {
        return _vars.values();
    }


}
