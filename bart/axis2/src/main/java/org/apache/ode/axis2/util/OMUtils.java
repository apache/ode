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

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility methods to convert from/to AxiOM and DOM.
 */
public class OMUtils {

    public static Element toDOM(OMElement element) {
        return toDOM(element, DOMUtils.newDocument());
    }

    public static Element toDOM(OMElement element, Document doc) { 
        return toDOM(element,doc,true);
    }
    
    @SuppressWarnings("unchecked")
    public static Element toDOM(OMElement element, Document doc, boolean deepNS) {
        final Element domElement = doc.createElementNS(element.getQName().getNamespaceURI(), element.getQName().getLocalPart());

        if (deepNS) {
            NSContext nscontext = new NSContext();
            buildNScontext(nscontext, element);
            DOMUtils.injectNamespaces(domElement,nscontext);
        } else {
            if (element.getAllDeclaredNamespaces() != null) {
                for (Iterator<OMNamespace> i = element.getAllDeclaredNamespaces(); i.hasNext(); ) {
                    OMNamespace omns = i.next();
                    if (omns.getPrefix().equals(""))
                        domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns", omns.getNamespaceURI() == null ? "" : omns.getNamespaceURI());
                    else
                        domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ omns.getPrefix(), omns.getNamespaceURI());
                }
    
            }
        }
            
        for (Iterator i = element.getAllAttributes(); i.hasNext();) {
            final OMAttribute attr = (OMAttribute) i.next();
            if (attr.getNamespace() != null)
                domElement.setAttributeNS(attr.getNamespace().getNamespaceURI(), attr.getLocalName(), attr.getAttributeValue());
            else
                domElement.setAttributeNS(null,attr.getLocalName(), attr.getAttributeValue());
                
        }

        for (Iterator<OMNode> i = element.getChildren(); i.hasNext();) {
            OMNode omn = i.next();

            switch (omn.getType()) {
            case OMNode.CDATA_SECTION_NODE:
                domElement.appendChild(doc.createCDATASection(((OMText)omn).getText()));
                break;
            case OMNode.TEXT_NODE:
                domElement.appendChild(doc.createTextNode(((OMText)omn).getText()));
                break;
            case OMNode.ELEMENT_NODE:
                domElement.appendChild(toDOM((OMElement)omn,doc, false));
                break;
            }
            
        }
        
        return domElement;
        
    }

    @SuppressWarnings("unchecked")
    private static void buildNScontext(NSContext nscontext, OMElement element) {
        if (element == null)
            return;
        
        if (element.getParent() instanceof OMElement)
            buildNScontext(nscontext, (OMElement) element.getParent());
        
        if (element.getAllDeclaredNamespaces() != null)
            for (Iterator<OMNamespace> i=element.getAllDeclaredNamespaces(); i.hasNext(); ){
                OMNamespace omn = i.next();
                nscontext.register(omn.getPrefix(), omn.getNamespaceURI());
            }
        
        if (element.getDefaultNamespace() != null)
            nscontext.register("", element.getDefaultNamespace().getNamespaceURI());
    }

    public static OMElement toOM(Element src, OMFactory omf) {
        return toOM(src,omf,null);
    }

    public static OMElement toOM(Element src, OMFactory omf, OMContainer parent) {
        OMNamespace elns = null;
        if (src.getNamespaceURI() != null) {
            elns = omf.createOMNamespace(src.getNamespaceURI(), src.getPrefix());
        }
        OMElement omElement = parent == null ? omf.createOMElement(src.getLocalName(),elns) :
        omf.createOMElement(src.getLocalName(),elns,parent);
        
        if (parent == null) {
            NSContext nscontext = DOMUtils.getMyNSContext(src);
            injectNamespaces(omElement,nscontext.toMap());
        } else {
            Map<String,String> nss = DOMUtils.getMyNamespaces(src);
            injectNamespaces(omElement, nss);
        }
        
        NamedNodeMap attrs = src.getAttributes();
        for (int i = 0; i <attrs.getLength(); ++i) {
            Attr attr = (Attr)attrs.item(i);
            if (attr.getLocalName().equals("xmlns") 
                    || (attr.getNamespaceURI() != null && attr.getNamespaceURI().equals(DOMUtils.NS_URI_XMLNS)))
                continue;
            OMNamespace attrOmNs = null;
            String attrNs = attr.getNamespaceURI();
            String attrPrefix = attr.getPrefix();
            if (attrNs != null)
                attrOmNs = omElement.findNamespace(attrNs,null);
            if (attrOmNs == null && attrPrefix != null)
                attrOmNs = omElement.findNamespace(null, attrPrefix);
            omElement.addAttribute(attr.getLocalName(), attr.getValue(), attrOmNs);
        }

        NodeList children = src.getChildNodes();
        for (int i = 0 ; i < children.getLength(); ++i) {
            Node n = children.item(i);

            switch (n.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
                omElement.addChild(omf.createOMText(((CDATASection)n).getTextContent(),XMLStreamConstants.CDATA));
                break;
            case Node.TEXT_NODE:
                omElement.addChild(omf.createOMText(((Text)n).getTextContent(),XMLStreamConstants.CHARACTERS));
                break;
            case Node.ELEMENT_NODE:
                toOM((Element)n,omf,omElement);
                break;
            }
            
        }
        
        return omElement;
    }


    private static void injectNamespaces(OMElement omElement, Map<String,String> nscontext) {
        for (String prefix : nscontext.keySet()) {
            String uri = nscontext.get(prefix);
            if (prefix.equals(""))
                omElement.declareDefaultNamespace(uri);
            else
                omElement.declareNamespace(uri, prefix);
        }
    }

    /**
     * Axiom is supposed to handle this properly however this method is buggy and doesn't work (whereas setting a QName as text
     * works).
     * 
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
            String ns = elmt.findNamespaceURI(prefix).getNamespaceURI();
            qname = new QName(ns, localpart, prefix);
        }
        return qname;
    }


}
