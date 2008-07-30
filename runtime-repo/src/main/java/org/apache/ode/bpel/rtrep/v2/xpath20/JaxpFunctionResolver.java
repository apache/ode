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

package org.apache.ode.bpel.elang.xpath20.runtime;

import net.sf.saxon.dom.NodeWrapper;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.bpel.elang.xpath20.compiler.WrappedResolverException;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.URITemplate;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mriou <mriou at apache dot org>
 */
public class JaxpFunctionResolver implements XPathFunctionResolver {

    private static final Log __log = LogFactory.getLog(JaxpFunctionResolver.class);

    private EvaluationContext _ectx;
    private OXPath20ExpressionBPEL20 _oxpath;

    public JaxpFunctionResolver(EvaluationContext ectx, OXPath20ExpressionBPEL20 oxpath) {
        _ectx = ectx;
        _oxpath = oxpath;
    }

    public XPathFunction resolveFunction(QName functionName, int arity) {
        __log.debug("Resolving function " + functionName);
        if (functionName.getNamespaceURI() == null) {
            throw new WrappedResolverException("Undeclared namespace for " + functionName);
        } else if (functionName.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS) ||
                functionName.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_EXEC)) {
            String localName = functionName.getLocalPart();
            if (Constants.EXT_FUNCTION_GETVARIABLEDATA.equals(localName)) {
                return new GetVariableData();
            } else if (Constants.EXT_FUNCTION_GETVARIABLEPROPRTY.equals(localName)) {
                return new GetVariableProperty();
            } else if (Constants.EXT_FUNCTION_GETLINKSTATUS.equals(localName)) {
                return new GetLinkStatus();
            } else if (Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(localName)) {
                return new DoXslTransform();
            } else {
                throw new WrappedResolverException("Unknown BPEL function: " + functionName);
            }
        } else if (functionName.getNamespaceURI().equals(Namespaces.ODE_EXTENSION_NS)) {
            String localName = functionName.getLocalPart();
            if (Constants.NON_STDRD_FUNCTION_SPLITTOELEMENTS.equals(localName)) {
                return new SplitToElements();
            } else if (Constants.NON_STDRD_FUNCTION_COMBINE_URL.equals(localName)) {
                return new CombineUrl();
            } else if (Constants.NON_STDRD_FUNCTION_COMPOSE_URL.equals(localName)) {
                return new ComposeUrl();
            } else if (Constants.NON_STDRD_FUNCTION_EXPAND_TEMPLATE.equals(localName)) {
                return new ComposeUrl(true, "expandTemplateInvalidSource");
            } else if ( Constants.NON_STDRD_FUNCTION_DOM_TO_STRING.equals(localName)) {
            	return new DomToString();
            }
        }

        return null;
    }

    public class GetLinkStatus implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 1)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "getLinkStatusInvalidSource"), "Illegal Arguments"));

            OLink olink = _oxpath.links.get(args.get(0));
            try {
                return _ectx.isLinkActive(olink) ? Boolean.TRUE : Boolean.FALSE;
            } catch (FaultException e) {
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "getLinkStatusInvalidSource"), e));
            }
        }
    }

    public class GetVariableData implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (__log.isDebugEnabled()) {
                __log.debug("call(context=" + _ectx + " args=" + args + ")");
            }

            String varname = (String) args.get(0);
            String partname = args.size() > 1 ? (String) args.get(1) : null;
            String xpathStr = args.size() > 2 ? (String) args.get(2) : null;

            OXPath10Expression.OSigGetVariableData sig = _oxpath.resolveGetVariableDataSig(varname, partname, xpathStr);
            if (sig == null) {
                String msg = "InternalError: Attempt to use an unknown getVariableData signature: " + args;
                if (__log.isFatalEnabled())
                    __log.fatal(msg);
                throw new XPathFunctionException(msg);
            }

            try {
                Node ret = _ectx.readVariable(sig.variable, sig.part);
                if (sig.location != null)
                    ret = _ectx.evaluateQuery(ret, sig.location);

                if (__log.isDebugEnabled()) {
                    __log.debug("bpws:getVariableData(" + args + ")' = " + ret);
                }

                return ret;
            } catch (FaultException e) {
                __log.error("bpws:getVariableData(" + args + ") threw FaultException");
                throw new XPathFunctionException(e);
            }
        }
    }

    public class GetVariableProperty implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2) {
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "getVariablePropertyInvalidSource"), "Missing required arguments"));
            }

            OScope.Variable var = _oxpath.vars.get(args.get(0));
            OProcess.OProperty property = _oxpath.properties.get(args.get(1));

            if (__log.isDebugEnabled()) {
                __log.debug("function call:'bpws:getVariableProperty(" + var + ","
                        + property + ")'");
            }

            try {
                return _ectx.readMessageProperty(var, property);
            } catch (FaultException e) {
                throw new XPathFunctionException(e);
            }
        }
    }

    public class DoXslTransform implements XPathFunction {

		public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() < 2 || (args.size() % 2) != 0)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "doXslTransformInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("call(context=" + _ectx + " args=" + args + ")");
            }
            if (!(_oxpath instanceof OXPath10ExpressionBPEL20)) {
                throw new IllegalStateException("XPath function bpws:doXslTransform not supported in BPEL 1.1!");
            }

            Element varElmt;
            try {
                if (args.get(1) instanceof List) {
                    List elmts = (List) args.get(1);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                    "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                            "element node."));
                    varElmt = (Element) elmts.get(0);
                } else {
                    if (args.get(1) instanceof NodeWrapper)
                        varElmt = (Element) ((NodeWrapper) args.get(1)).getUnderlyingNode();
                    else varElmt = (Element) args.get(1);
                }
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                        "element node."));
            }

            URI xslUri;
            try {
                xslUri = new URI((String) args.get(0));
            } catch (URISyntaxException use) {
                // Shouldn't happen, checked at compilation time
                throw new XPathFunctionException("First parameter of the bpws:doXslTransform isn't a valid URI!");
            }
            OXslSheet xslSheet = _oxpath.xslSheets.get(xslUri);
            // Shouldn't happen, checked at compilation time
            if (xslSheet == null) throw new XPathFunctionException("Couldn't find the XSL sheet " + args.get(0)
                    + ", process compilation or deployment was probably incomplete!");

            if (!(varElmt instanceof Element)) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                        "element node."));
            }

            HashMap<QName, Object> parametersMap = null;
            if (args.size() > 2) {
                parametersMap = new HashMap<QName, Object>();
                for (int idx = 2; idx < args.size(); idx += 2) {
                    QName keyQName = _oxpath.namespaceCtx.derefQName((String) args.get(idx));
                    Object paramElmt;
                    if (args.get(idx + 1) instanceof NodeWrapper) {
                        Element tmpElmt = (Element) ((NodeWrapper) args.get(idx + 1)).getUnderlyingNode();
                        Document paramDoc = DOMUtils.newDocument();
                        paramDoc.appendChild(paramDoc.importNode(tmpElmt, true));
                        paramElmt = paramDoc;
                        if (__log.isDebugEnabled())
                            __log.debug("Passing parameter " + keyQName + " " + DOMUtils.domToString(paramDoc));
                    } else if (args.get(idx + 1) instanceof List) {
                        paramElmt = ((List) args.get(idx + 1)).get(0);
                    } else paramElmt = args.get(idx + 1);

                    parametersMap.put(keyQName, paramElmt);
                }
            }

            if (__log.isDebugEnabled())
                __log.debug("Executing XSL sheet " + args.get(0) + " on element " + DOMUtils.domToString(varElmt));

            Document varDoc = DOMUtils.newDocument();
            varDoc.appendChild(varDoc.importNode(varElmt, true));

            DOMSource source = new DOMSource(varDoc);
            // Using a StreamResult as a DOMResult doesn't behaves properly when the result
            // of the transformation is just a string.
            StringWriter writerResult = new StringWriter();
            StreamResult result = new StreamResult(writerResult);
            XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(_oxpath, _ectx.getBaseResourceURI());
            XslTransformHandler.getInstance().cacheXSLSheet(xslUri, xslSheet.sheetBody, resolver);
            try {
                XslTransformHandler.getInstance().transform(xslUri, source, result, parametersMap, resolver);
            } catch (Exception e) {
                e.printStackTrace();
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSubLanguageExecutionFault,
                                e.toString()));
            }
            writerResult.flush();

            String output = writerResult.toString();
            if (__log.isDebugEnabled())
                __log.debug("Returned by XSL Sheet: " + output);
            // I'm not really proud of that but hey, it does the job and I don't think there's
            // any other easy way.
            if (output.startsWith("<?xml")) {
                try {
                    return DOMUtils.stringToDOM(output).getChildNodes();
                } catch (SAXException e) {
                    throw new XPathFunctionException("Parsing the result of the XSL sheet " + args.get(0) +
                            " didn't produce a parsable XML result: " + output);
                } catch (IOException e) {
                    throw new XPathFunctionException(e);
                } catch (Exception e) {
                    throw new XPathFunctionException("Parsing the result of the XSL sheet " + args.get(0) +
                            " didn't produce a parsable XML result: " + output);
                }
            } else {
                return output;
            }
        }
    }
    
    public class DomToString implements XPathFunction {
    	public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 1)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "domToStringInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("domToString call(context=" + _ectx + " args=" + args + ")");
            }
            
            Element varElmt;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                    "The bpws:domToString function MUST be passed a single " +
                                            "element node."));
                    varElmt = (Element) elmts.get(0);
                } else {
                    if (args.get(1) instanceof NodeWrapper)
                        varElmt = (Element) ((NodeWrapper) args.get(1)).getUnderlyingNode();
                    else varElmt = (Element) args.get(1);
                }
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                "The bpws:domToString function MUST be passed a single " +
                                        "element node."));
            }    
            String result= DOMUtils.domToString(varElmt); 
            return result;
    	}
    }

    /**
     * Compile time checking for the non standard ode:splitToElements function.
     */
    public class SplitToElements implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() <= 2 || args.size() >= 5)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "splitInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("splitToElements call(context=" + _ectx + " args=" + args + ")");
            }

            String strToSplit;
            try {
                strToSplit = Helper.extractString(args.get(0));
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "splitInvalidSource"), e));
            }

            // Other parameters
            String separator = (String) args.get(1);
            String localName = (String) args.get(2);
            String namespace = args.size() == 4 ? (String) args.get(3) : null;

            // Preparing the result document
            Document doc = DOMUtils.newDocument();
            Element wrapper = doc.createElement("wrapper");
            doc.appendChild(wrapper);

            // Creating nodes for each string element of the split string and appending to result
            String[] strElmts = strToSplit.split(separator);
            for (String strElmt : strElmts) {
                Element elmt = doc.createElementNS(namespace, localName);
                elmt.setTextContent(strElmt.trim());
                wrapper.appendChild(elmt);
            }

            return wrapper;
        }

    }

    /**
     * Takes the relative URL and combines it with the base URL to return a new absolute URL.
     * If the relative parameter is an absolute URL, returns it instead.
     * <p/>
     * As described in section 5 of <a href="http://www.ietf.org/rfc/rfc2396.txt">rfc2396</a>.
     * <p/>
     * This implementation relies heavily on {@link java.net.URL}. As thus, the same restrictions apply, especially regarding encoding.
     * <p/>
     * <i>"The URL class does not itself encode or decode any URL components according
     * to the escaping mechanism defined in RFC2396. It is the responsibility of the caller
     * to encode any fields, which need to be escaped prior to calling URL, and also to decode
     * any escaped fields, that are returned from URL."</i>
     *
     * @see java.net.URL
     * @see URL#URL(java.net.URL, String)
     */
    public static class CombineUrl implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            final QName FAULT_QNAME = new QName(Namespaces.ODE_EXTENSION_NS, "combineUrlInvalidSource");
            if (args.size() != 2) {
                throw new XPathFunctionException(new FaultException(FAULT_QNAME, "Invalid arguments"));
            }


            String base;
            try {
                base = Helper.extractString(args.get(0));
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(new FaultException(FAULT_QNAME, "Invalid argument: " + args.get(0), e));
            }
            String relative;
            try {
                relative = Helper.extractString(args.get(1));
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(new FaultException(FAULT_QNAME, "Invalid argument: " + args.get(1), e));
            }

            URL baseURL;
            try {
                baseURL = new URL(base);
            } catch (MalformedURLException e) {
                throw new XPathFunctionException(new FaultException(FAULT_QNAME, "First parameter [" + base + "] MUST point to a well-formed URL.", e));
            }
            try {
                URL combined = new URL(baseURL, relative);
                return combined.toExternalForm();
            } catch (MalformedURLException e) {
                throw new XPathFunctionException(new FaultException(FAULT_QNAME, e.getMessage(), e));
            }
        }
    }

    public static class ComposeUrl implements XPathFunction {
        boolean preserveUndefinedVar = false;
        String faultLocalPart = "composeUrlInvalidSource";
        QName faultQName;

        public ComposeUrl() {
            faultQName = new QName(Namespaces.ODE_EXTENSION_NS, faultLocalPart);
        }

        public ComposeUrl(boolean preserveUndefinedVar, String faultLocalPart) {
            this.preserveUndefinedVar = preserveUndefinedVar;
            this.faultLocalPart = faultLocalPart;
            faultQName = new QName(Namespaces.ODE_EXTENSION_NS, faultLocalPart);
        }

        public Object evaluate(List args) throws XPathFunctionException {
            // prepare these 2 arguments 
            String uriTemplate;
            Map<String, String> pairs;

            boolean separareParameteters;
            if (args.size() == 2) {
                separareParameteters = false;
            } else if (args.size() > 2 && args.size() % 2 == 1) {
                separareParameteters = true;
            } else {
                throw new XPathFunctionException(new FaultException(faultQName, "Illegal Arguments"));
            }
            try {
                uriTemplate = Helper.extractString(args.get(0));
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(new FaultException(faultQName, "Invalid argument: URI Template expected. " + args.get(0), e));
            }
            if (separareParameteters) {
                // /!\ Do NOT get the first element
                try {
                    pairs = Helper.buildNameValueMap(args, 1);
                } catch (IllegalArgumentException e) {
                    throw new XPathFunctionException(new FaultException(faultQName, "Invalid argument", e));
                }
            } else {
                try {
                    List elmts = (List) args.get(1);
                    Element elt = (Element) elmts.get(0);
                    pairs = Helper.extractNameValueMap(elt);
                } catch (ClassCastException e) {
                    throw new XPathFunctionException(new FaultException(faultQName, "Expected an element similar too: <foo><name1>value1</name1>name2>value2</name2>...</foo>"));
                }
            }

            try {
                if (preserveUndefinedVar) {
                    return URITemplate.expandLazily(uriTemplate, pairs);
                } else {
                    return URITemplate.expand(uriTemplate, pairs);
                }
            } catch (URIException e) {
                throw new XPathFunctionException(new FaultException(faultQName, "Invalid argument", e));
            } catch (UnsupportedOperationException e) {
                throw new XPathFunctionException(new FaultException(faultQName, "Invalid argument", e));
            }
        }

    }

    public static class Helper {
        /**
         * Extract a string from the given parameter.<br/>
         * The parameter could be:
         * <ol>
         * <li>a {@link java.util.List} containing exactly one {@link org.w3c.dom.Node}</li>
         * <li>a {@link net.sf.saxon.dom.NodeWrapper}</li>
         * <li>a {@link org.w3c.dom.Node}</li>
         * <li>or a {@link String}</li>
         * </ol>
         * In the first 3 cases, if the {@linkplain org.w3c.dom.Node node} type is {@link Node#ELEMENT_NODE} the (trimmed) {@linkplain org.w3c.dom.Node#getTextContent() text content} is returned.
         * if the {@linkplain org.w3c.dom.Node node} type is {@link Node#TEXT_NODE} the (trimmed) {@linkplain org.w3c.dom.Text#getWholeText() text content} is returned.
         * <p/>
         *
         * @param arg
         * @return a string
         * @throws IllegalArgumentException if none of the conditions mentioned above are met
         */
        public static String extractString(Object arg) throws IllegalArgumentException {
            // Checking the parameter, should be a proper element or a text node. Java verbosity at its best.
            String res = null;
            try {
                Node node = null;
                if (arg instanceof List) {
                    List elmts = (List) arg;
                    if (elmts.size() != 1)
                        throw new IllegalArgumentException("Parameter MUST point to a string, single element or text node.");
                    node = (Node) elmts.get(0);
                } else if (arg instanceof NodeWrapper) {
                    node = (Node) ((NodeWrapper) arg).getUnderlyingNode();
                } else if (arg instanceof Node) {
                    node = (Node) arg;
                } else {
                    res = (String) arg;
                }

                if (res == null) {
                    if (Node.ELEMENT_NODE == node.getNodeType()) {
                        res = node.getTextContent().trim();
                    } else if (Node.TEXT_NODE == node.getNodeType()) {
                        res = ((Text) node).getWholeText().trim();
                    }
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Parameter MUST point to a string, single element or text node.", e);
            }
            return res;
        }

        /**
         * Extract the name/value from an xml element similar too:
         * <br/>
         * {@literal <elt>
         * <foovar>bar</foovar>
         * <myvar>value1</myvar>
         * </elt>}
         *
         * <p/>
         * The local name of the element is the map key, the text content the associated value.
         *
         * @return a Map of name/value pair
         */
        public static Map<String, String> extractNameValueMap(Element elt) {
            Map<String, String> pairs = new HashMap<String, String>();

            for (int i = 0; i < elt.getChildNodes().getLength(); i++) {
                Node n = elt.getChildNodes().item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    pairs.put(n.getLocalName(), DOMUtils.getTextContent(n));
                }
            }
            return pairs;
        }

        /**
         * Same as {@link #buildNameValueMap(java.util.List, int)} but index equals zero. 
         * @see #buildNameValueMap(java.util.List, int)
         */
        public static Map<String, String> buildNameValueMap(List args) {
            return buildNameValueMap(args, 0);
        }

        /**
         * {@linkplain #extractString(Object) Extract a string} from each list element and build a map with them.
         * <br/>Elements at even indices would be the keys, Elements at odd indices the values.
         * 
         * @param args the list containing a serie of name, value, name, value, and so on
         * @param begin index of the first name to include in the map, (args.size - begin) must be an even number
         * or an IndexOutOfBoundsException will be thrown 
         * @return a Map of name/value pairs
         * @throws IndexOutOfBoundsException
         * @see #extractString(Object)
         */
        public static Map<String, String> buildNameValueMap(List args, int begin) {
            Map<String, String> pairs;
            pairs = new HashMap<String, String>();
            for (int i = begin; i < args.size(); i = i + 2) {
                pairs.put(Helper.extractString(args.get(i)), Helper.extractString(args.get(i + 1)));
            }
            return pairs;
        }
    }

}
