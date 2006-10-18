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

package org.apache.ode.axis2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * Utility methods to convert from/to AxiOM and DOM.
 */
public class OMUtils {

    public static Element toDOM(OMElement element) throws AxisFault {
        copyParentNamespaces(element);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            element.serialize(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return DOMUtils.parse(bais).getDocumentElement();
        } catch (Exception e) {
            throw new AxisFault("Unable to read Axis input message.", e);
        }
    }

    public static OMElement toOM(Element element, boolean replicateEmptyNS) throws AxisFault {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DOMUtils.serialize(element, baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(bais);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement result =  builder.getDocumentElement();
            if (replicateEmptyNS) reproduceEmptyNS(element, element, result);
            return result;
        } catch (Exception e) {
            throw new AxisFault("Unable to read Axis input message.", e);
        }
    }

    /**
     * Axiom is supposed to handle this properly however this method is buggy
     * and doesn't work (whereas setting a QName as text works).
     * @param elmt
     * @return text qname
     */
    public static QName getTextAsQName(OMElement elmt) {
        QName qname = elmt.getTextAsQName();
        // The getTextAsQName is buggy, it sometimes return the full text without extracting namespace
        if (qname.getNamespaceURI().length() == 0) {
            int colonIdx = elmt.getText().indexOf(":");
            String localpart = elmt.getText().substring(colonIdx + 1, elmt.getText().length());
            String prefix = elmt.getText().substring(0, colonIdx);
            String ns = elmt.findNamespaceURI(prefix).getName();
            qname = new QName(ns, localpart, prefix);
        }
        return qname;
    }

    /**
     * Translation from DOM to AXIOM loses empty namespace definitions. So if you have something
     * like:
     * <pre>
     *   <foo xmlns="ns:foo">
     *     <bar xmlns="">
     *   </pr:foo>
     * </pre>
     * After translation bar will be in the same namespace as foo. This is due to Woodstox (the
     * stax parser behind AXIOM) that considers xmlns="" as being a null namespace, hence it is
     * ignored.
     * @param root
     * @param elmt
     * @param omelmt
     */
    private static void reproduceEmptyNS(Element root, Element elmt, OMElement omelmt) {
        if (root.getNamespaceURI() != null && elmt.getNamespaceURI() == null) {
            OMAttribute emptynsa = omelmt.getOMFactory().createOMAttribute("xmlns", null, "");
            omelmt.addAttribute(emptynsa);
        }

        NodeList children = elmt.getChildNodes();
        Iterator omchildren = omelmt.getChildElements();
        for (int m = 0; m < children.getLength(); m++) {
            Node child = children.item(m);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                OMElement omchild = (OMElement) omchildren.next();
                reproduceEmptyNS(root, (Element) child, omchild);
            }
        }

    }

    /**
     * Copy namespaces found on parent elements on the element itself.
     * This is useful when detaching an element from its parent to maintain
     * namespace context.
     */
    public static void copyParentNamespaces(OMElement target) {
        if (target.getParent() instanceof OMElement) {
            HashSet<String> declaredNS = new HashSet<String>();
            Iterator iter = target.getAllDeclaredNamespaces();
            while (iter.hasNext()) {
                OMNamespace ns = (OMNamespace) iter.next();
                declaredNS.add(ns.getPrefix());
            }
            copyParentNamespaces(target, (OMElement) target.getParent(), declaredNS);
        }
    }

    private static void copyParentNamespaces(OMElement target, OMElement parent, HashSet<String> declaredNS) {
        Iterator iter = parent.getAllDeclaredNamespaces();
        while (iter.hasNext()) {
            OMNamespace ns = (OMNamespace) iter.next();
            // do not override local namespace mappings
            if (!ns.getPrefix().equals("") && !declaredNS.contains(ns.getPrefix())) {
                target.declareNamespace(ns.getName(), ns.getPrefix());
                declaredNS.add(ns.getPrefix());
            }
        }
        // recurse
        if (parent.getParent() instanceof OMElement) {
            copyParentNamespaces(target, (OMElement) parent.getParent(), declaredNS);
        }
    }
}
