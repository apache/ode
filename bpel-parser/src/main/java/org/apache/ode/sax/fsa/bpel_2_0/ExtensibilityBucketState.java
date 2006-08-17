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
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.fsa.AbstractState;
import org.apache.ode.sax.fsa.DOMGenerator;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Bucket state to capture schema-level extensibility elements. Extensibility
 * attributes must be handled elsewhere.
 */
class ExtensibilityBucketState extends AbstractState {

    private static final StateFactory _factory = new Factory();

    private QName _elementQName;
    private Element _extensibility;
    private DOMGenerator _domGenerator;

    ExtensibilityBucketState(StartElement se, ParseContext pc) {
        super(pc);
        _elementQName = se.getName();
        _domGenerator = new DOMGenerator();
    }

    static class Factory implements StateFactory
    {
        public State newInstance(StartElement se, ParseContext pc)
                throws ParseException {
            return new ExtensibilityBucketState(se, pc);
        }
    }

    public void handleSaxEvent(SaxEvent se) throws ParseException {
        /*
        * For the moment, this is a basic implementation, but if supporting
        * extensions is desired, those extensions can be hooked from here.
        * Ideally, we'd have some kind of registry implementation that routes
        * SaxEvent streams based on URI or some other scheme. However, for the
        * moment, we don't have any use cases. WS-BPEL 2.0 extensibility can be
        * implemented according to the spec, once that's settled.
        */
        _domGenerator.handleSaxEvent(se);
    }

    public void done() {
        if (_domGenerator.getRoot() != null) {
            Document doc = DOMUtils.newDocument();
            Element root = doc.createElementNS(_elementQName.getNamespaceURI(), _elementQName.getLocalPart());
            root.appendChild(doc.importNode(_domGenerator.getRoot(), true));
            doc.appendChild(root);
            _extensibility = root;
        }
    }

    public StateFactory getFactory() {
        return _factory;
    }

    public int getType() {
        return EXTENSIBILITY_ELEMENT;
    }

    public QName getElementQName() {
        return _elementQName;
    }

    public Element getExtensibility() {
        return _extensibility;
    }

}
