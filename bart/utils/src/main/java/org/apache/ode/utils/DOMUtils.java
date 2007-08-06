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
package org.apache.ode.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.sax.LoggingErrorHandler;
import org.apache.xerces.dom.DOMOutputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xml.serialize.DOMSerializerImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for dealing with the Document Object Model (DOM).
 */
public class DOMUtils {

    private static Log __log = LogFactory.getLog(DOMUtils.class);

    /** The namespaceURI represented by the prefix <code>xmlns</code>. */
    public static final String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/";

    private static ThreadLocal<Transformer> __txers = new ThreadLocal();
    private static ThreadLocal<DocumentBuilder> __builders = new ThreadLocal();
    private static TransformerFactory _transformerFactory = TransformerFactory.newInstance();

    private static DocumentBuilderFactory __documentBuilderFactory ;

    static {
        initDocumentBuilderFactory();
    }

    /**
     * Initialize the document-builder factory.
     */
    private static void initDocumentBuilderFactory() {
        DocumentBuilderFactory f = XMLParserUtils.getDocumentBuilderFactory();
        f.setNamespaceAware(true);
        __documentBuilderFactory = f;
    }

    /**
     * Returns the value of an attribute of an element. Returns null if the
     * attribute is not found (whereas Element.getAttribute returns "" if an
     * attrib is not found).
     *
     * @param el Element whose attrib is looked for
     * @param attrName name of attribute to look for
     *
     * @return the attribute value
     */
    static public String getAttribute(Element el, String attrName) {
        String sRet = null;
        Attr attr = el.getAttributeNode(attrName);
        if (attr != null) {
            sRet = attr.getValue();
        }
        return sRet;
    }

    static public String prettyPrint(Element e) throws IOException {
        OutputFormat format = new OutputFormat(e.getOwnerDocument());
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        StringWriter out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(e);

        return out.toString();
    }

    /**
     * Returns the value of an attribute of an element. Returns null if the
     * attribute is not found (whereas Element.getAttributeNS returns "" if an
     * attrib is not found).
     *
     * @param el Element whose attrib is looked for
     * @param namespaceURI namespace URI of attribute to look for
     * @param localPart local part of attribute to look for
     *
     * @return the attribute value
     */
    static public String getAttributeNS(Element el, String namespaceURI,
                                        String localPart) {
        String sRet = null;
        Attr attr = el.getAttributeNodeNS(namespaceURI, localPart);
        if (attr != null) {
            sRet = attr.getValue();
        }
        return sRet;
    }

    /**
     * Concat all the text and cdata node children of this elem and return the
     * resulting text.
     *
     * @param parentEl the element whose cdata/text node values are to be
     *        combined.
     *
     * @return the concatanated string.
     */
    static public String getChildCharacterData(Element parentEl) {
        if (parentEl == null) { return null; }
        Node tempNode = parentEl.getFirstChild();
        StringBuffer strBuf = new StringBuffer();
        CharacterData charData;
        while (tempNode != null) {
            switch (tempNode.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    charData = (CharacterData) tempNode;
                    strBuf.append(charData.getData());
                    break;
            }
            tempNode = tempNode.getNextSibling();
        }
        return strBuf.toString();
    }

    /**
     * DOCUMENTME
     *
     * @param el DOCUMENTME
     * @param id DOCUMENTME
     *
     * @return DOCUMENTME
     */
    public static Element getElementByID(Element el, String id) {
        if (el == null) { return null; }
        String thisId = el.getAttribute("id");
        if (id.equals(thisId)) { return el; }
        NodeList list = el.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element ret = getElementByID((Element) node, id);
                if (ret != null) { return ret; }
            }
        }
        return null;
    }

    /**
     * Return the first child element of the given element. Null if no children
     * are found.
     *
     * @param elem Element whose child is to be returned
     *
     * @return the first child element.
     */
    public static Element getFirstChildElement(Element elem) {
        if (elem == null)
            throw new NullPointerException("elem parameter must not be null!");

        for (Node n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) { return (Element) n; }
        }
        return null;
    }

    /**
     * Given a prefix and a node, return the namespace URI that the prefix has
     * been associated with. This method is useful in resolving the namespace
     * URI of attribute values which are being interpreted as QNames. If prefix
     * is null, this method will return the default namespace.
     *
     * @param context the starting node (looks up recursively from here)
     * @param prefix the prefix to find an xmlns:prefix=uri for
     *
     * @return the namespace URI or null if not found
     */
    public static String getNamespaceURIFromPrefix(Node context, String prefix) {
        short nodeType = context.getNodeType();
        Node tempNode = null;
        switch (nodeType) {
            case Node.ATTRIBUTE_NODE: {
                tempNode = ((Attr) context).getOwnerElement();
                break;
            }
            case Node.ELEMENT_NODE: {
                tempNode = context;
                break;
            }
            default: {
                tempNode = context.getParentNode();
                break;
            }
        }
        while ((tempNode != null) && (tempNode.getNodeType() == Node.ELEMENT_NODE)) {
            Element tempEl = (Element) tempNode;
            String namespaceURI = (prefix == null) ? getAttribute(tempEl, "xmlns")
                    : getAttributeNS(tempEl, NS_URI_XMLNS, prefix);
            if (namespaceURI != null) {
                return namespaceURI;
            }
            tempNode = tempEl.getParentNode();
        }
        return null;
    }

    /**
     * Return the next sibling element of the given element. Null if no more
     * sibling elements are found.
     *
     * @param elem Element whose sibling element is to be returned
     *
     * @return the next sibling element.
     */
    public static Element getNextSiblingElement(Element elem) {
        for (Node n = elem.getNextSibling(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) { return (Element) n; }
        }
        return null;
    }

    /**
     * DOCUMENTME
     *
     * @param el DOCUMENTME
     * @param attrName DOCUMENTME
     *
     * @return DOCUMENTME
     *
     * @throws IllegalArgumentException DOCUMENTME
     */
    public static QName getQualifiedAttributeValue(Element el, String attrName)
            throws IllegalArgumentException {
        String attrValue = DOMUtils.getAttribute(el, attrName);
        if (attrValue != null) {
            int index = attrValue.indexOf(':');
            String attrValuePrefix = (index != -1) ? attrValue.substring(0, index)
                    : null;
            String attrValueLocalPart = attrValue.substring(index + 1);
            String attrValueNamespaceURI = DOMUtils.getNamespaceURIFromPrefix(el,
                    attrValuePrefix);
            if (attrValueNamespaceURI != null) {
                return new QName(attrValueNamespaceURI, attrValueLocalPart);
            }
            throw new IllegalArgumentException("Unable to determine "
                    + "namespace of '"
                    + ((attrValuePrefix != null) ? (attrValuePrefix + ":") : "")
                    + attrValueLocalPart + "'.");
        }
        return null;
    }

    /**
     * Count number of children of a certain type of the given element.
     *
     * @param elem the element whose kids are to be counted
     * @param nodeType DOCUMENTME
     *
     * @return the number of matching kids.
     */
    public static int countKids(Element elem, short nodeType) {
        int nkids = 0;
        for (Node n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == nodeType) {
                nkids++;
            }
        }
        return nkids;
    }



    /**
     * This method traverses the DOM and grabs namespace declarations
     * on parent elements with the intent of preserving them for children.  <em>Note
     * that the DOM level 3 document method {@link Element#getAttribute(java.lang.String)}
     * is not desirable in this case, as it does not respect namespace prefix
     * bindings that may affect attribute values.  (Namespaces in DOM are
     * uncategorically a mess, especially in the context of XML Schema.)</em>
     * @param el the starting element
     * @return a {@link Map} containing prefix bindings.
     */
    public static Map<String, String> getParentNamespaces(Element el) {
        HashMap<String,String> pref = new HashMap<String,String>();
        Map mine = getMyNamespaces(el);
        Node n = el.getParentNode();
        do {
            if (n instanceof Element) {
                Element l = (Element) n;
                NamedNodeMap nnm = l.getAttributes();
                int len = nnm.getLength();
                for (int i = 0; i < len; ++i) {
                    Attr a = (Attr) nnm.item(i);
                    if (isNSAttribute(a)) {
                        String key = getNSPrefixFromNSAttr(a);
                        String uri = a.getValue();
                        // prefer prefix bindings that are lower down in the tree.
                        if (pref.containsKey(key) || mine.containsKey(key)) continue;
                        pref.put(key, uri);
                    }
                }
            }
            n = n.getParentNode();
        } while (n != null && n.getNodeType() != Node.DOCUMENT_NODE);
        return pref;
    }

    /**
     * Construct a {@link NSContext} instance for the supplied element.
     * @param el the <code>Element</code> to gather the namespace context for
     * @return the <code>NSContext</code>
     */
    public static NSContext getMyNSContext(Element el) {
        NSContext ns = new NSContext();
        ns.register(getParentNamespaces(el));
        ns.register(getMyNamespaces(el));
        return ns;
    }

    public static Map<String,String> getMyNamespaces(Element el) {
        HashMap<String,String> mine = new HashMap<String,String>();
        NamedNodeMap nnm = el.getAttributes();
        int len = nnm.getLength();
        for (int i=0; i < len; ++i) {
            Attr a = (Attr) nnm.item(i);
            if (isNSAttribute(a)) {
                mine.put(getNSPrefixFromNSAttr(a),a.getValue());
            }
        }
        return mine;
    }

    /**
     * Test whether an attribute contains a namespace declaration.
     * @param a an {@link Attr} to test.
     * @return <code>true</code> if the {@link Attr} is a namespace declaration
     */
    public static boolean isNSAttribute(Attr a) {
        assert a != null;
        String s = a.getNamespaceURI();
        return (s != null && s.equals(NS_URI_XMLNS));
    }

    /**
     * Fetch the non-null namespace prefix from a {@link Attr} that declares
     * a namespace.  (The DOM APIs will return <code>null</code> for a non-prefixed
     * declaration.
     * @param a the {@link Attr} with the declaration (must be non-<code>null</code).
     * @return the namespace prefix or <code>&quot;&quot;</code> if none was
     * declared, e.g., <code>xmlns=&quot;foo&quot;</code>.
     */
    public static String getNSPrefixFromNSAttr(Attr a) {
        assert a != null;
        assert isNSAttribute(a);
        if (a.getPrefix() == null) {
            return "";
        }
        return a.getName().substring(a.getPrefix().length()+1);
    }

    /**
     * Convert a DOM node to a stringified XML representation.
     */
    static public String domToString(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot stringify null Node!");
        }

        String value = null;
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE || nodeType == Node.DOCUMENT_NODE) {
            // serializer doesn't handle Node type well, only Element
            DOMSerializerImpl ser = new DOMSerializerImpl();
            ser.setParameter(Constants.DOM_NAMESPACES, Boolean.TRUE);
            ser.setParameter(Constants.DOM_WELLFORMED, Boolean.FALSE );
            ser.setParameter(Constants.DOM_VALIDATE, Boolean.FALSE);

            // create a proper XML encoding header based on the input document;
            // default to UTF-8 if the parent document's encoding is not accessible
            String usedEncoding = "UTF-8";
            Document parent = node.getOwnerDocument();
            if (parent != null) {
                String parentEncoding = parent.getXmlEncoding();
                if (parentEncoding != null) {
                    usedEncoding = parentEncoding;
                }
            }

            // the receiver of the DOM
            DOMOutputImpl out = new DOMOutputImpl();
            out.setEncoding(usedEncoding);

            // we write into a String
            StringWriter writer = new StringWriter(4096);
            out.setCharacterStream(writer);

            // out, ye characters!
            ser.write(node, out);
            writer.flush();

            // finally get the String
            value = writer.toString();
        } else {
            value = node.getNodeValue();
        }
        return value;
    }

    public static void serialize(Element elmt, OutputStream ostr) {
        String usedEncoding = "UTF-8";
        Document parent = elmt.getOwnerDocument();
        if (parent != null) {
            String parentEncoding = parent.getXmlEncoding();
            if (parentEncoding != null) {
                usedEncoding = parentEncoding;
            }
        }

        DOMOutputImpl out = new DOMOutputImpl();
        out.setEncoding(usedEncoding);

        DOMSerializerImpl ser = new DOMSerializerImpl();
        out.setByteStream(ostr);
        ser.write(elmt, out);
    }

    /**
     * Convert a DOM node to a stringified XML representation.
     */
    static public String domToStringLevel2(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot stringify null Node!");
        }

        String value = null;
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE || nodeType == Node.DOCUMENT_NODE) {
            // serializer doesn't handle Node type well, only Element
            DOMSerializerImpl ser = new DOMSerializerImpl();
            ser.setParameter(Constants.DOM_NAMESPACES, Boolean.TRUE);
            ser.setParameter(Constants.DOM_WELLFORMED, Boolean.FALSE );
            ser.setParameter(Constants.DOM_VALIDATE, Boolean.FALSE);

            // the receiver of the DOM
            DOMOutputImpl out = new DOMOutputImpl();
            out.setEncoding("UTF-8");

            // we write into a String
            StringWriter writer = new StringWriter(4096);
            out.setCharacterStream(writer);

            // out, ye characters!
            ser.write(node, out);
            writer.flush();

            // finally get the String
            value = writer.toString();
        } else {
            value = node.getNodeValue();
        }
        return value;
    }

    /**
     * Return the first child element of the given element which has the given
     * attribute with the given value.
     *
     * @param elem the element whose children are to be searched
     * @param attrName the attrib that must be present
     * @param attrValue the desired value of the attribute
     *
     * @return the first matching child element.
     */
    public static Element findChildElementWithAttribute(Element elem,
                                                        String attrName, String attrValue) {
        for (Node n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (attrValue.equals(DOMUtils.getAttribute((Element) n, attrName))) { return (Element) n; }
            }
        }
        return null;
    }

    /**
     * Parse a String into a DOM.
     *
     * @param s DOCUMENTME
     *
     * @return DOCUMENTME
     *
     * @throws SAXException DOCUMENTME
     * @throws IOException DOCUMENTME
     */
    static public Element stringToDOM(String s) throws SAXException, IOException {
        return parse(new InputSource(new StringReader(s))).getDocumentElement();
    }

    /**
     * Perform a naive check to see if a document is a WSDL document
     * based on the root element name and namespace URI.
     * @param d the {@link Document} to check
     * @return <code>true</code> if the root element appears correct
     */
    public static boolean isWsdlDocument(Document d) {
        Element e = d.getDocumentElement();
        String uri = e.getNamespaceURI();
        String localName = e.getLocalName();
        if (uri == null || localName == null) { return false; }
        return uri.equals(WSDL_NS) && localName.equals(WSDL_ROOT_ELEMENT);
    }

    /**
     * Perform a naive check to see if a document is an XML schema document
     * based on the root element name and namespace URI.
     * @param d the {@link Document} to check
     * @return <code>true</code> if the root element appears correct
     */
    public static boolean isXmlSchemaDocument(Document d) {
        Element e = d.getDocumentElement();
        String uri = e.getNamespaceURI();
        String localName = e.getLocalName();
        if (uri == null || localName == null) { return false; }
        return uri.equals(XSD_NS) && localName.equals(XSD_ROOT_ELEMENT);
    }

    public static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_ROOT_ELEMENT = "definitions";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    public static final String XSD_ROOT_ELEMENT = "schema";

    /**
     * @param el
     */
    public static void pancakeNamespaces(Element el) {
        Map ns = getParentNamespaces(el);
        Document d = el.getOwnerDocument();
        assert d != null;
        Iterator it = ns.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String uri = (String) ns.get(key);
            Attr a = d.createAttributeNS(NS_URI_XMLNS,
                    (key.length() != 0)?("xmlns:" + key):("xmlns"));
            a.setValue(uri);
            el.setAttributeNodeNS(a);
        }
    }

    public static Document newDocument() {
        DocumentBuilder db = getBuilder();
        return db.newDocument();
    }

    /**
     * Parse an XML stream using the pooled document builder.
     * @param inputStream input stream
     * @return parsed XML document
     */
    public static Document parse(InputStream inputStream) throws SAXException, IOException {
        return parse(new InputSource(inputStream));
    }

    /**
     * Parse an XML document located using an {@link InputSource} using the
     * pooled document builder.
     */
    public static Document parse(InputSource inputSource) throws SAXException,IOException{
        DocumentBuilder db = getBuilder();
        return db.parse(inputSource);
    }

    /**
     * Parse an XML document located using an {@link InputSource} using the
     * pooled document builder.
     */
    public static Document sourceToDOM(Source inputSource) throws IOException {
        try {
            /*
            // Requires JDK 1.6+
            if (inputSource instanceof StAXSource) {
                StAXSource stax = (StAXSource) inputSource;
                //if (stax.getXMLEventReader() != null || sax.getXMLStreamReader() != null) {
                if (sax.getXMLStreamReader() != null) {
                    return parse(stax.getXMLStreamReader());
                }
            }
            */
            if (inputSource instanceof SAXSource) {
                InputSource sax = ((SAXSource) inputSource).getInputSource();
                if (sax.getCharacterStream() != null || sax.getByteStream() != null) {
                    return parse( ((SAXSource) inputSource).getInputSource() );
                }
            }
            if (inputSource instanceof DOMSource) {
                Node node = ((DOMSource) inputSource).getNode();
                if (node != null) {
                    return toDOMDocument(node);
                }
            }
            if (inputSource instanceof StreamSource) {
                StreamSource stream = (StreamSource) inputSource;
                if (stream.getReader() != null || stream.getInputStream() != null) {
                    return toDocumentFromStream( (StreamSource) inputSource);
                }
            }
            DOMResult domresult = new DOMResult(newDocument());
            Transformer txer = getTransformer();
            txer.transform(inputSource, domresult);
            return (Document) domresult.getNode();
        } catch (SAXException e) {
            throwIOException(e);
        } catch (TransformerException e) {
            throwIOException(e);
        }
        throw new IllegalArgumentException("Cannot parse XML source: " + inputSource.getClass());
    }

    /**
     * Check that an element is empty, i.e., it contains no non-whitespace text or
     * elements as children.
     * @param el the element
     * @return <code>true</code> if the element is empty, <code>false</code> if not.
     */
    public static boolean isEmptyElement(Element el) {
        NodeList nl = el.getChildNodes();
        int len = nl.getLength();
        for (int i=0; i < len; ++i) {
            switch (nl.item(i).getNodeType()) {
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                    String s = nl.item(i).getNodeValue();
                    if (s != null && s.trim().length() > 0) {
                        return false;
                    }
                    break;
                case Node.ELEMENT_NODE:
                    return false;
            }
        }
        return true;
    }

    public static QName getElementQName(Element el) {
        return new QName(el.getNamespaceURI(),el.getLocalName());
    }

    /**
     * Remove the child nodes under another node.
     * @param target the <code>Node</code> to remove the children from.
     */
    public static void removeChildren(Node target) {
        while (target.hasChildNodes()) {
            target.removeChild(target.getFirstChild());
        }
    }

    /**
     * Drop the attributes from an element, except possibly an <code>xmlns</code>
     * attribute that declares its namespace.
     * @param target the element whose attributes will be removed.
     * @param flag preserve namespace declaration
     */
    public static void removeAttributes(Element target, boolean flag) {
        if (!target.hasAttributes()) {
            return;
        }
        String prefix = target.getPrefix();
        NamedNodeMap nnm = target.getAttributes();
        Attr toPutBack = null;
        if (flag) {
            if (prefix== null) {
                toPutBack = target.getAttributeNodeNS(NS_URI_XMLNS,"xmlns");
            } else {
                toPutBack = target.getAttributeNodeNS(NS_URI_XMLNS,"xmlns:" + prefix);
            }

        }
        while(nnm.getLength() != 0) {
            target.removeAttributeNode((Attr) nnm.item(0));
        }
        if (toPutBack != null) {
            target.setAttributeNodeNS(toPutBack);
        }
    }

    public static Element findChildByName(Element parent, QName name) {
        return findChildByName(parent, name, false);
    }

    public static Element findChildByName(Element parent, QName name, boolean recurse) {
        if (parent == null)
            throw new IllegalArgumentException("null parent");
        if (name == null)
            throw new IllegalArgumentException("null name");

        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            Node c = nl.item(i);
            if(c.getNodeType() != Node.ELEMENT_NODE)
                continue;
            // For a reason that I can't fathom, when using in-mem DAO we actually get elements with
            // no localname.
            String nodeName = c.getLocalName() != null ? c.getLocalName() : c.getNodeName();
            if (new QName(c.getNamespaceURI(),nodeName).equals(name))
                return (Element) c;
        }

        if(recurse){
            NodeList cnl = parent.getChildNodes();
            for (int i = 0; i < cnl.getLength(); ++i) {
                Node c = cnl.item(i);
                if(c.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element result = findChildByName((Element)c, name, recurse);
                if(result != null)
                    return result;
            }
        }
        return null;
    }

    public static String getTextContent(Node node) {
        for (int m = 0; m < node.getChildNodes().getLength(); m++) {
            Node child = node.getChildNodes().item(m);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String childText = child.getNodeValue().trim();
                if (childText.length() > 0) return childText;
            }
        }
        return null;
    }

    public static Element getElementContent(Node node) {
        for (int m = 0; m < node.getChildNodes().getLength(); m++) {
            Node child = node.getChildNodes().item(m);
            if (child.getNodeType() == Node.ELEMENT_NODE) return (Element) child;
        }
        return null;
    }

    public static void injectNamespaces(Element domElement, NSContext nscontext) {
        for (String uri : nscontext.getUriSet()) {
            String prefix = nscontext.getPrefix(uri);
            if (prefix == null || "".equals(prefix))
                domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns", uri);
            else
                domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ prefix, uri);

        }
    }

    public static Document toDOMDocument(Node node) throws TransformerException {
        // If the node is the document, just cast it
        if (node instanceof Document) {
            return (Document) node;
        // If the node is an element
        } else if (node instanceof Element) {
            Element elem = (Element) node;
            // If this is the root element, return its owner document
            if (elem.getOwnerDocument().getDocumentElement() == elem) {
                return elem.getOwnerDocument();
            // else, create a new doc and copy the element inside it
            } else {
                Document doc = newDocument();
                doc.appendChild(doc.importNode(node, true));
                return doc;
            }
        // other element types are not handled
        } else {
            throw new TransformerException("Unable to convert DOM node to a Document");
        }
    }

    public static Document toDocumentFromStream(StreamSource source) throws IOException, SAXException {
        DocumentBuilder builder = getBuilder();
        Document document = null;
        Reader reader = source.getReader();
        if (reader != null) {
            document = builder.parse(new InputSource(reader));
        } else {
            InputStream inputStream = source.getInputStream();
            if (inputStream != null) {
                InputSource inputsource = new InputSource(inputStream);
                inputsource.setSystemId( source.getSystemId() );
                document = builder.parse(inputsource);
            }
            else {
                throw new IOException("No input stream or reader available");
            }
        }
        return document;
    }

    // sadly, as of JDK 5.0 IOException still doesn't support new IOException(Throwable)
    private static void throwIOException(Throwable t) throws IOException {
        IOException e = new IOException(t.getMessage());
        e.setStackTrace(t.getStackTrace());
        throw e;
    }


    public static Document parse(XMLStreamReader reader)
        throws XMLStreamException
    {
        Document doc = newDocument();
        parse(reader, doc, doc);
        return doc;
    }

    private static void parse(XMLStreamReader reader, Document doc, Node parent)
        throws XMLStreamException
    {
        int event = reader.getEventType();

        while (reader.hasNext()) {
            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                // create element
                Element e = doc.createElementNS(reader.getNamespaceURI(), reader.getLocalName());
                if (reader.getPrefix() != null && reader.getPrefix() != "") {
                    e.setPrefix(reader.getPrefix());
                }
                parent.appendChild(e);

                // copy namespaces
                for (int ns = 0; ns < reader.getNamespaceCount(); ns++) {
                    String uri = reader.getNamespaceURI(ns);
                    String prefix = reader.getNamespacePrefix(ns);
                    declare(e, uri, prefix);
                }

                // copy attributes
                for (int att = 0; att < reader.getAttributeCount(); att++) {
                    String name = reader.getAttributeLocalName(att);
                    String prefix = reader.getAttributePrefix(att);
                    if (prefix != null && prefix.length() > 0) {
                        name = prefix + ":" + name;
                    }
                    Attr attr = doc.createAttributeNS(reader.getAttributeNamespace(att), name);
                    attr.setValue(reader.getAttributeValue(att));
                    e.setAttributeNode(attr);
                }
                // sub-nodes
                if (reader.hasNext()) {
                    reader.next();
                    parse(reader, doc, e);
                }
                if (parent instanceof Document) {
                    while (reader.hasNext()) reader.next();
                    return;
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                return;
            case XMLStreamConstants.CHARACTERS:
                if (parent != null) {
                    parent.appendChild(doc.createTextNode(reader.getText()));
                }
                break;
            case XMLStreamConstants.COMMENT:
                if (parent != null) {
                    parent.appendChild(doc.createComment(reader.getText()));
                }
                break;
            case XMLStreamConstants.CDATA:
                parent.appendChild(doc.createCDATASection(reader.getText()));
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                parent.appendChild(doc.createProcessingInstruction(reader.getPITarget(), reader.getPIData()));
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                parent.appendChild(doc.createProcessingInstruction(reader.getPITarget(), reader.getPIData()));
                break;
            case XMLStreamConstants.NAMESPACE:
            case XMLStreamConstants.ATTRIBUTE:
                break;
            default:
                break;
            }

            if (reader.hasNext()) {
                event = reader.next();
            }
        }
    }

    private static void declare(Element node, String uri, String prefix) {
        if (prefix != null && prefix.length() > 0) {
            node.setAttributeNS(NS_URI_XMLNS, "xmlns:" + prefix, uri);
        } else {
            if (uri != null) {
                node.setAttributeNS(NS_URI_XMLNS, "xmlns", uri);
            }
        }
    }

    private static Transformer getTransformer() {
        Transformer txer = __txers.get();
        if (txer == null) {
            synchronized(_transformerFactory) {
            try {
                txer = _transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                String errmsg = "Transformer configuration error!";
                __log.fatal(errmsg, e);
                throw new Error(errmsg, e);
            }
        }
            __txers.set(txer);
        }
        return txer;
    }

    private static DocumentBuilder getBuilder() {
        DocumentBuilder builder = __builders.get();
        if (builder == null) {
            synchronized (__documentBuilderFactory) {
                try {
                    builder = __documentBuilderFactory.newDocumentBuilder();
                    builder.setErrorHandler(new LoggingErrorHandler());
                } catch (ParserConfigurationException e) {
                    __log.error(e);
                    throw new RuntimeException(e);
                }
            }
            __builders.set(builder);
        }
        return builder;
    }

}