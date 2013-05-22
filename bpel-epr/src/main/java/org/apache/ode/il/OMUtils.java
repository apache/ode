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

package org.apache.ode.il;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Source;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.xml.sax.InputSource;

/**
 * Utility methods to convert from/to AxiOM and DOM.
 */
public class OMUtils {

    private static Log __log = LogFactory.getLog(OMUtils.class);

    public static OMElement getFirstChildWithName(OMElement parent, String name) {
        if (parent == null)
            throw new IllegalArgumentException("null parent");
        if (name == null)
            throw new IllegalArgumentException("null name");
        for (Iterator it = parent.getChildElements(); it.hasNext();) {
            OMElement e = (OMElement) it.next();
            if (name.equals(e.getQName().getLocalPart())) return e;
        }
        return null;
    }

    public static Element toDOM(OMElement element) {
        return toDOM(element, DOMUtils.newDocument());
    }

    public static Element toDOM(OMElement element, Document doc) {
        return toDOM(element,doc,true);
    }

    @SuppressWarnings("unchecked")
    public static Element toDOM(OMElement element, Document doc, boolean deepNS) {
        //
        //  Fix regarding lost qnames on response of invoke activity:
        //    * copy an element including its prefix.
        //    * add all namespase attributes.
        //
        String domElementNsUri = element.getQName().getNamespaceURI();
        String domElementQName;
        if (element.getQName().getPrefix() == null || element.getQName().getPrefix().trim().length() == 0) {
            domElementQName = element.getQName().getLocalPart();
        } else {
            domElementQName = element.getQName().getPrefix() + ":" + element.getQName().getLocalPart();
        }
        if (__log.isTraceEnabled())
            __log.trace("toDOM: creating element with nsUri=" + domElementNsUri
                    + " qname=" + domElementQName
                    + " from omElement, name=" + element.getLocalName());

        final Element domElement = doc.createElementNS(
                domElementNsUri,
                domElementQName);
        

        if (deepNS) {
            NSContext nscontext = new NSContext();
            buildNScontext(nscontext, element);
            DOMUtils.injectNamespacesWithAllPrefixes(domElement,nscontext);
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
        if (__log.isTraceEnabled())
            __log.trace("toDOM: created root element (deepNS=" + deepNS + "): " + DOMUtils.domToString(domElement));

        for (Iterator i = element.getAllAttributes(); i.hasNext();) {
            final OMAttribute attr = (OMAttribute) i.next();
            Attr newAttr;
            if (attr.getNamespace() != null) newAttr = doc.createAttributeNS(attr.getNamespace().getNamespaceURI(), attr.getLocalName());
            else newAttr = doc.createAttributeNS(null,attr.getLocalName());

            newAttr.appendChild(doc.createTextNode(attr.getAttributeValue()));
            domElement.setAttributeNodeNS(newAttr);

            // Case of qualified attribute values, we're forced to add corresponding namespace declaration manually...
            int colonIdx = attr.getAttributeValue().indexOf(":");
            if (colonIdx > 0) {
                OMNamespace attrValNs = element.findNamespaceURI(attr.getAttributeValue().substring(0, colonIdx));
                if (attrValNs != null)
                    domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ attrValNs.getPrefix(), attrValNs.getNamespaceURI());
            }
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
        OMElement omElement = parent == null ? omf.createOMElement(src.getLocalName(), null) :
                omf.createOMElement(src.getLocalName(), null, parent);
        if (src.getNamespaceURI() != null) {
            if (src.getPrefix() != null)
                omElement.setNamespace(omf.createOMNamespace(src.getNamespaceURI(), src.getPrefix()));
            else omElement.declareDefaultNamespace(src.getNamespaceURI());
        }

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
        if (qname == null || qname.getNamespaceURI().length() == 0) {
            int colonIdx = elmt.getText().indexOf(":");
            String localpart = elmt.getText().substring(colonIdx + 1, elmt.getText().length());
            String prefix = elmt.getText().substring(0, colonIdx);
            String ns = elmt.findNamespaceURI(prefix).getNamespaceURI();
            qname = new QName(ns, localpart, prefix);
        }
        return qname;
    }

    /**
     * Parse an XML document located using an {@link InputSource} using the
     * pooled document builder.
     */
    public static OMElement toOM(Source inputSource) throws IOException {
        Document doc = DOMUtils.sourceToDOM(inputSource);
        return toOM(doc.getDocumentElement(), OMAbstractFactory.getOMFactory());
    }

}
