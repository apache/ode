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
package org.apache.ode.bpel.o;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Message variable type.
 */
public class OMessageVarType extends OVarType {
    private static final long serialVersionUID = 256680050844726425L;
    
    public QName messageType;
    public final Map<String, Part> parts = new LinkedHashMap<String,Part>();

    /** For doc-lit-like message types , the element type of the only part. */
    public final OElementVarType docLitType;

    public OMessageVarType(OProcess owner, QName messageType, Collection<Part> parts) {
        super(owner);
        this.messageType = messageType;
        for (Iterator<Part> i = parts.iterator(); i.hasNext();) {
            Part part = i.next();
            this.parts.put(part.name,part);
        }

        if ((parts.size() == 1 && parts.iterator().next().type instanceof OElementVarType))
            docLitType = (OElementVarType) parts.iterator().next().type;
        else
            docLitType = null;
    }

    boolean isDocLit() { return docLitType != null; }


    public Node newInstance(Document doc) {
        Element el = doc.createElementNS(null, "message");
        for(OMessageVarType.Part part : parts.values()){
            Element partElement = doc.createElementNS(null, part.name);
            partElement.appendChild(part.type.newInstance(doc));
            el.appendChild(partElement);
        }
        return el;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
        buf.append('(');
        buf.append(this.messageType.toString());
        buf.append(')');
        return buf.toString();
    } 

    public static class Part extends OBase {
        private static final long serialVersionUID = -2356665271228433779L;
        
        public String name;
        public OVarType type;

        public Part(OProcess owner, String partName, OVarType partType) {
            super(owner);
            this.name = partName;
            this.type = partType;
        }

    }

}
