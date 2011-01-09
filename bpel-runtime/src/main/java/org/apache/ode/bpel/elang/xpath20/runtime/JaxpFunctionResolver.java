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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.YearMonthDurationValue;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.XslRuntimeUriResolver;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
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
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
        if (__log.isDebugEnabled()) {
            __log.debug("Resolving function " + functionName);
        }
        if (functionName.getNamespaceURI() == null) {
            throw new NullPointerException("Undeclared namespace for " + functionName);
        } else if (functionName.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS) ||
                functionName.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_EXEC)) {
            String localName = functionName.getLocalPart();
            if (Constants.EXT_FUNCTION_GETVARIABLEDATA.equals(localName)) {
                return new GetVariableData();
            } else if (Constants.EXT_FUNCTION_GETVARIABLEPROPERTY.equals(localName)) {
                return new GetVariableProperty();
            } else if (Constants.EXT_FUNCTION_GETLINKSTATUS.equals(localName)) {
                return new GetLinkStatus();
            } else if (Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(localName)) {
                return new DoXslTransform();
            } else {
                throw new NullPointerException("Unknown BPEL function: " + functionName);
            }
        } else if (functionName.getNamespaceURI().equals(Namespaces.ODE_EXTENSION_NS)) {
            String localName = functionName.getLocalPart();
            if (Constants.NON_STDRD_FUNCTION_SPLIT_TO_ELEMENTS.equals(localName) ||
                    Constants.NON_STDRD_FUNCTION_DEPRECATED_SPLIT_TO_ELEMENTS.equals(localName)) {
                return new SplitToElements();
            } else if (Constants.NON_STDRD_FUNCTION_COMBINE_URL.equals(localName) ||
                    Constants.NON_STDRD_FUNCTION_DEPRECATED_COMBINE_URL.equals(localName)) {
                return new CombineUrl();
            } else if (Constants.NON_STDRD_FUNCTION_COMPOSE_URL.equals(localName) ||
                    Constants.NON_STDRD_FUNCTION_DEPRECATED_COMPOSE_URL.equals(localName)) {
                return new ComposeUrl();
            } else if (Constants.NON_STDRD_FUNCTION_EXPAND_TEMPLATE.equals(localName) ||
                    Constants.NON_STDRD_FUNCTION_DEPRECATED_EXPAND_TEMPLATE.equals(localName)) {
                return new ComposeUrl(true, "expandTemplateInvalidSource");
            } else if (Constants.NON_STDRD_FUNCTION_DOM_TO_STRING.equals(localName) ||
                    Constants.NON_STDRD_FUNCTION_DEPRECATED_DOM_TO_STRING.equals(localName)) {
                return new DomToString();
            } else if (Constants.NON_STDRD_FUNCTION_INSERT_AFTER.equals(localName)) {
                return new InsertAfter();
            } else if (Constants.NON_STDRD_FUNCTION_INSERT_AS_FIRST_INTO.equals(localName)) {
                return new InsertAsFirstInto();
            } else if (Constants.NON_STDRD_FUNCTION_INSERT_AS_LAST_INTO.equals(localName)) {
                return new InsertAsLastInto();
            } else if (Constants.NON_STDRD_FUNCTION_INSERT_BEFORE.equals(localName)) {
                return new InsertBefore();
            } else if (Constants.NON_STDRD_FUNCTION_DELETE.equals(localName)) {
                return new Delete();
            } else if (Constants.NON_STDRD_FUNCTION_RENAME.equals(localName)) {
                return new Rename();
            } else if (Constants.NON_STDRD_FUNCTION_PROCESS_PROPERTY.equals(localName)) {
                return new ProcessProperty();
            }
        } else if (functionName.getNamespaceURI().equals(Namespaces.DEPRECATED_XDT_NS)) {
            String localName = functionName.getLocalPart();
            if (Constants.NON_STDRD_FUNCTION_DAY_TIME_DURATION.equals(localName)) {
                return new DayTimeDuration();
            } else if (Constants.NON_STDRD_FUNCTION_YEAR_MONTH_DURATION.equals(localName)) {
                return new YearMonthDuration();
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
            } catch (EvaluationException e) {
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
            OXslSheet xslSheet = _oxpath.getXslSheet(xslUri);
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
            Object result;
            XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(_oxpath, _ectx.getBaseResourceURI());
            XslTransformHandler.getInstance().cacheXSLSheet(_ectx.getProcessQName(), xslUri, xslSheet.sheetBody, resolver);
            try {
                result = XslTransformHandler.getInstance().transform(_ectx.getProcessQName(), xslUri, source, parametersMap, resolver);
            } catch (Exception e) {
                __log.error("Could not transform XSL sheet " + args.get(0) + " on element " + DOMUtils.domToString(varElmt), e);
                e.printStackTrace();
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSubLanguageExecutionFault,
                                e.toString()));
            }
            if(result instanceof Node)
                return ((Node)result).getChildNodes();
            return result;
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
                } else if (args.get(0) instanceof NodeWrapper) {
                    varElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    varElmt = (Element) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
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
                    Element elt = null;
                    if (args.get(1) instanceof List) {
                        List elmts = (List) args.get(1);
                        elt = (Element) elmts.get(0);
                    } else if (args.get(1) instanceof Element) {
                        elt = (Element) args.get(1);
                    }
                    pairs = Helper.extractNameValueMap(elt);
                } catch (ClassCastException e) {
                    throw new XPathFunctionException(new FaultException(faultQName, "Expected an element similar too: <foo><name1>value1</name1><name2>value2</name2>...</foo>"));
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

    public class InsertInto implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 3)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "insertIntoInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("insertInto call(context=" + _ectx + " args=" + args + ")");
            }

            Element parentElmt;
            int position;
            List childNodes;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:insertInto function MUST be passed a single " +
                                            "element node."));
                    parentElmt = (Element) elmts.get(0);
                } else if (args.get(0) instanceof NodeWrapper) {
                    parentElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    parentElmt = (Element) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                position = Helper.extractInteger(args.get(1));
                if (args.get(2) instanceof List) {
                    childNodes = (List) args.get(2);
                } else if (args.get(2) instanceof NodeWrapper) {
                    Node childElmt = (Node) ((NodeWrapper) args.get(2)).getUnderlyingNode();
                    childNodes = new ArrayList<Node>();
                    childNodes.add(childElmt);
                } else if (args.get(2) instanceof Element) {
                    Node childElmt = (Node) args.get(2);
                    childNodes = new ArrayList<Node>();
                    childNodes.add(childElmt);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:insertInto function MUST be passed a single " +
                                        "element node."));
            }
            Element clonedElmt = (Element) parentElmt.cloneNode(true);
            NodeList children = clonedElmt.getChildNodes();
            int childCount = children.getLength();
            Node refChild = null;
            if (position <= 1) {
                refChild = clonedElmt.getFirstChild();
            } else if (position == childCount) {
                refChild = clonedElmt.getLastChild();
            } else if (position > childCount) {
                refChild = null;
            } else {
                refChild = children.item(position + 1);
            }
            for (int i = 0; i < childNodes.size(); i++) {
                clonedElmt.insertBefore((Node) childNodes.get(i), refChild);
            }
            return clonedElmt;
        }
    }

    public class InsertAfter implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() < 2 || args.size() > 3)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "insertAfterInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("insertAfter call(context=" + _ectx + " args=" + args + ")");
            }

            Element targetElmt;
            List<Node> siblingNodes;
            Object childArg = null, siblingsArg = null;
            try {
                if (args.size() == 2) {
                    childArg = args.get(0);
                    siblingsArg = args.get(1);
                } else {
                    childArg = args.get(1);
                    siblingsArg = args.get(2);
                }
                if (childArg instanceof List) {
                    List elmts = (List) childArg;
                    // allow insertions after a sequence of node items
                    // if (elmts.size() != 1) throw new XPathFunctionException(
                    //        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                    //                "The bpws:insertAfter function MUST be passed a single " +
                    //                        "element node."));
                    targetElmt = (Element) elmts.get(elmts.size() - 1);
                } else if (childArg instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) childArg).getUnderlyingNode();
                } else if (childArg instanceof Element) {
                    targetElmt = (Element) childArg;
                } else {
                    throw new XPathFunctionException("Unexpected argument type: " + childArg.getClass());
                }
                if (siblingsArg instanceof List) {
                    siblingNodes = (List<Node>) siblingsArg;
                } else if (siblingsArg instanceof NodeWrapper) {
                    Node childElmt = (Node) ((NodeWrapper) siblingsArg).getUnderlyingNode();
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else if (siblingsArg instanceof Element) {
                    Node childElmt = (Node) siblingsArg;
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: " + siblingsArg.getClass());
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + siblingsArg, e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:insertAfter function MUST be passed a single " +
                                        "element node."));
            }
            Element parentElmt = (Element) targetElmt.getParentNode();
            NodeList children = parentElmt.getChildNodes();
            int position = 0;
            while (position < children.getLength()) {
                if (children.item(position++).isSameNode(targetElmt)) {
                    break;
                }
            }
            Element clonedElmt = (Element) parentElmt.cloneNode(true);
            children = clonedElmt.getChildNodes();
            Node refChild = (position < children.getLength()) ? children.item(position) : null;
            Document clonedDocument = clonedElmt.getOwnerDocument();
            for (int i = 0; i < siblingNodes.size(); i++) {
                clonedElmt.insertBefore(clonedDocument.importNode((Node) siblingNodes.get(i), true), refChild);
            }
            return clonedElmt;
        }
    }

    public class InsertBefore implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() < 2 || args.size() > 3)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "insertBeforeInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("insertBefore call(context=" + _ectx + " args=" + args + ")");
            }

            Element targetElmt;
            List<Node> siblingNodes;
            Object childArg = null, siblingsArg = null;
            try {
                if (args.size() == 2) {
                    childArg = args.get(0);
                    siblingsArg = args.get(1);
                } else {
                    childArg = args.get(1);
                    siblingsArg = args.get(2);
                }
                if (childArg instanceof List) {
                    List elmts = (List) childArg;
                    // allow insertions after a sequence of node items
                    // if (elmts.size() != 1) throw new XPathFunctionException(
                    //        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                    //                "The bpws:insertBefore function MUST be passed a single " +
                    //                        "element node."));
                    targetElmt = (Element) elmts.get(0);
                } else if (childArg instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) childArg).getUnderlyingNode();
                } else if (childArg instanceof Element) {
                    targetElmt = (Element) childArg;
                } else {
                    throw new XPathFunctionException("Unexpected argument type: " + childArg.getClass());
                }
                if (siblingsArg instanceof List) {
                    siblingNodes = (List) siblingsArg;
                } else if (siblingsArg instanceof NodeWrapper) {
                    Node childElmt = (Node) ((NodeWrapper) siblingsArg).getUnderlyingNode();
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else if (siblingsArg instanceof Element) {
                    Node childElmt = (Node) siblingsArg;
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: " + siblingsArg.getClass());
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + childArg, e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:insertBefore function MUST be passed a single " +
                                        "element node."));
            }
            Element parentElmt = (Element) targetElmt.getParentNode();
            NodeList children = parentElmt.getChildNodes();
            int position = 0;
            while (position < children.getLength()) {
                if (children.item(position++).isSameNode(targetElmt)) {
                    break;
                }
            }
            Element clonedElmt = (Element) parentElmt.cloneNode(true);
            children = clonedElmt.getChildNodes();
            Node refChild = (position <= children.getLength()) ? children.item(position - 1) : null;
            Document clonedDocument = clonedElmt.getOwnerDocument();
            for (int i = 0; i < siblingNodes.size(); i++) {
                clonedElmt.insertBefore(clonedDocument.importNode((Node) siblingNodes.get(i), true), refChild);
            }
            return clonedElmt;
        }
    }

    public class InsertAsFirstInto implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "insertAsFirstIntoInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("insertAsFirstInto call(context=" + _ectx + " args=" + args + ")");
            }

            Element targetElmt;
            List siblingNodes;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:insertAsFirstInto function MUST be passed a single " +
                                            "element node."));
                    targetElmt = (Element) elmts.get(0);
                } else if (args.get(0) instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    targetElmt = (Element) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                if (args.get(1) instanceof List) {
                    siblingNodes = (List) args.get(1);
                } else if (args.get(1) instanceof NodeWrapper) {
                    Node childElmt = (Node) ((NodeWrapper) args.get(1)).getUnderlyingNode();
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else if (args.get(1) instanceof Element) {
                    Node childElmt = (Node) args.get(1);
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:insertAsFirstInto function MUST be passed a single " +
                                        "element node."));
            }
            Element clonedElmt = (Element) targetElmt.cloneNode(true);
            Node refChild = clonedElmt.getFirstChild();
            Document clonedDocument = clonedElmt.getOwnerDocument();
            for (int i = 0; i < siblingNodes.size(); i++) {
                clonedElmt.insertBefore(clonedDocument.importNode((Node) siblingNodes.get(i), true), refChild);
            }
            return clonedElmt;
        }
    }

    public class InsertAsLastInto implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "insertAsLastIntoInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("insertAsLastInto call(context=" + _ectx + " args=" + args + ")");
            }

            Element targetElmt;
            List siblingNodes;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:insertAsLastInto function MUST be passed a single " +
                                            "element node."));
                    targetElmt = (Element) elmts.get(0);
                } else if (args.get(0) instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    targetElmt = (Element) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                if (args.get(1) instanceof List) {
                    siblingNodes = (List) args.get(1);
                } else if (args.get(1) instanceof NodeWrapper) {
                    Node childElmt = (Node) ((NodeWrapper) args.get(1)).getUnderlyingNode();
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else if (args.get(1) instanceof Element) {
                    Node childElmt = (Node) args.get(1);
                    siblingNodes = new ArrayList<Node>();
                    siblingNodes.add(childElmt);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:insertAsLastInto function MUST be passed a single " +
                                        "element node."));
            }
            Element clonedElmt = (Element) targetElmt.cloneNode(true);
            Document clonedDocument = clonedElmt.getOwnerDocument();
            for (int i = 0; i < siblingNodes.size(); i++) {
                clonedElmt.appendChild(clonedDocument.importNode((Node) siblingNodes.get(i), true));
            }
            return clonedElmt;
        }
    }

    public class Delete implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() < 1 || args.size() > 2)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "deleteInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("delete call(context=" + _ectx + " args=" + args + ")");
            }

            List<Node> targetNodes = new ArrayList();
            List siblingNodes;
            Object delete = args.size() == 2 ? delete = args.get(1) : args.get(0);
            try {
                if (delete instanceof List) {
                    List elmts = (List) delete;
                    // allow insertions after a sequence of node items
                    // if (elmts.size() != 1) throw new XPathFunctionException(
                    //        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                    //                "The bpws:delete function MUST be passed a single " +
                    //                        "element node."));
                    targetNodes.addAll(elmts);
                } else if (delete instanceof NodeWrapper) {
                    targetNodes.add((Element) ((NodeWrapper) delete).getUnderlyingNode());
                } else if (delete instanceof Element) {
                    targetNodes.add((Element) delete);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: " + delete.getClass());
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + delete, e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:delete function MUST be passed a valid " +
                                        "element node."));
            }
            Element parentElmt = null;
            for (Node targetNode : targetNodes) {
                if (parentElmt == null) {
                    parentElmt = (Element) targetNode.getParentNode();
                } else if (!parentElmt.isSameNode((Element) targetNode.getParentNode())) {
                    throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:delete function MUST be passed nodes that have " +
                                            "the same parent."));
                }
            }
            NodeList children = parentElmt.getChildNodes();
            int[] positions = new int[targetNodes.size()];
            for (int target = 0; target < positions.length; target++) {
                for (int position = 0; position < children.getLength(); position++) {
                    if (children.item(position).isSameNode(targetNodes.get(target))) {
                        positions[target] = position;
                    }
                }
            }
            final Element clonedElmt = (Element) parentElmt.cloneNode(true);
            children = clonedElmt.getChildNodes();
            for (int target = 0; target < positions.length; target++) {
                Element deleteElmt = (Element) children.item(positions[target]);
                clonedElmt.removeChild(deleteElmt);
            }
            // Saxon doesn't like clones with no children, so I'll oblige
            if (clonedElmt.getChildNodes().getLength() == 0) {
                try {
                    clonedElmt.appendChild(DOMUtils.toDOMDocument(parentElmt).createTextNode(""));
                } catch (TransformerException te) {
                    throw new XPathFunctionException(te);
                }
            }
            return clonedElmt;
        }
    }

    public class Rename implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() < 2)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "renameInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("rename call(context=" + _ectx + " args=" + args + ")");
            }

            Element targetElmt;
            QName elementQName = null, elementTypeQName = null;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:rename function MUST be passed a single " +
                                            "element node."));
                    targetElmt = (Element) elmts.get(0);
                } else if (args.get(0) instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    targetElmt = (Element) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                String localName = null, namespaceUri = null, prefix = null;
                if (args.get(1) instanceof QNameValue) {
                    QNameValue qNameValue = (QNameValue) args.get(1);
                    namespaceUri = qNameValue.getNamespaceURI();
                    localName = qNameValue.getLocalName();
                    prefix = qNameValue.getPrefix();
                } else if (args.get(1) instanceof List) {
                    List elmts = (List) args.get(1);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:rename function MUST be passed a single " +
                                            "element node."));
                    Element nameElmt = (Element) elmts.get(0);
                    namespaceUri = nameElmt.getNamespaceURI();
                    localName = nameElmt.getLocalName();
                    prefix = nameElmt.getPrefix();
                } else if (args.get(1) instanceof NodeWrapper) {
                    Element nameElmt = (Element) ((NodeWrapper) args.get(1)).getUnderlyingNode();
                    namespaceUri = nameElmt.getNamespaceURI();
                    localName = nameElmt.getLocalName();
                    prefix = nameElmt.getPrefix();
                } else if (args.get(1) instanceof Element) {
                    Element nameElmt = (Element) args.get(1);
                    namespaceUri = nameElmt.getNamespaceURI();
                    localName = nameElmt.getLocalName();
                    prefix = nameElmt.getPrefix();
                } else if (args.get(1) instanceof String)	{
                    String qName = (String) args.get(1);
                    if (qName.contains(":")) {
                        int index = qName.indexOf(":");
                        prefix = qName.substring(0, index);
                        localName = qName.substring(index + 1);
                    } else {
                        localName = qName;
                    }
                } else if (args.get(1) instanceof QName) {
                    QName qName = (QName) args.get(1);
                    namespaceUri = qName.getNamespaceURI();
                    localName = qName.getLocalPart();
                    prefix = qName.getPrefix();
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(1).getClass());
                }
                if (namespaceUri == null) {
                    namespaceUri = targetElmt.lookupNamespaceURI(prefix);
                }
                elementQName = new QName(namespaceUri, localName, prefix);
                if (args.size() > 2) {
                    if (args.get(2) instanceof QNameValue) {
                        QNameValue qNameValue = (QNameValue) args.get(2);
                        namespaceUri = qNameValue.getNamespaceURI();
                        localName = qNameValue.getLocalName();
                        prefix = qNameValue.getPrefix();
                    } else if (args.get(2) instanceof NodeWrapper) {
                        Element nameElmt = (Element) ((NodeWrapper) args.get(2)).getUnderlyingNode();
                        namespaceUri = nameElmt.getNamespaceURI();
                        localName = nameElmt.getLocalName();
                        prefix = nameElmt.getPrefix();
                    } else if (args.get(2) instanceof Element) {
                        Element nameElmt = (Element) args.get(2);
                        namespaceUri = nameElmt.getNamespaceURI();
                        localName = nameElmt.getLocalName();
                        prefix = nameElmt.getPrefix();
                    } else if (args.get(2) instanceof String)	{
                        String qName = (String) args.get(2);
                        if (qName.contains(":")) {
                            int index = qName.indexOf(":");
                            prefix = qName.substring(0, index);
                            localName = qName.substring(index + 1);
                        } else {
                            localName = qName;
                        }
                    } else {
                        throw new XPathFunctionException("Unexpected argument type: "+args.get(2).getClass());
                    }
                    if (namespaceUri == null) {
                        namespaceUri = targetElmt.lookupNamespaceURI(prefix);
                    }
                    elementTypeQName = new QName(namespaceUri, localName, prefix);;
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:rename function MUST be passed a single " +
                                        "element node."));
            }
            Element parentElmt = (Element) targetElmt.getParentNode();
            NodeList children = parentElmt.getChildNodes();
            int position = 0;
            while (position < children.getLength()) {
                if (children.item(position++).isSameNode(targetElmt)) {
                    break;
                }
            }
            Element clonedElmt = (Element) parentElmt.cloneNode(true);
            children = clonedElmt.getChildNodes();
            Element renamedElmt = targetElmt
                                    .getOwnerDocument()
                                    .createElementNS(
                                            elementQName.getNamespaceURI(),
                                            elementQName.getPrefix() + ":" + elementQName.getLocalPart());
            Element originalElmt = (Element) children.item(position - 1);
            children = originalElmt.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                renamedElmt.appendChild(children.item(i));
            }
            clonedElmt.replaceChild(renamedElmt, originalElmt);
            if (elementTypeQName != null) {
                renamedElmt.setAttributeNS(
                        Namespaces.XML_INSTANCE, "xsi:type",
                        elementTypeQName.getPrefix() + ":" + elementTypeQName.getLocalPart());
            }
            return clonedElmt;
        }
    }

    public class ProcessProperty implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 1)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "processPropertyInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("process-property call(context=" + _ectx + " args=" + args + ")");
            }

            QName propertyName = null;
            Element targetElmt = null;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:process-property function MUST be passed a single " +
                                            "element node."));
                    if (elmts.get(0) instanceof Element) {
                        targetElmt = (Element) elmts.get(0);
                    } else if (elmts.get(0) instanceof String) {
                        propertyName = new QName((String) elmts.get(0));
                    }
                } else if (args.get(0) instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    targetElmt = (Element) args.get(0);
                } else if (args.get(0) instanceof QNameValue) {
                    QNameValue qNameValue = (QNameValue) args.get(0);
                    propertyName = new QName(qNameValue.getNamespaceURI(), qNameValue.getLocalName(), qNameValue.getPrefix());
                } else if (args.get(0) instanceof String)	{
                    String stringValue = (String) args.get(0);
                    if (stringValue.indexOf(":") > 0) {
                        String prefix = stringValue.substring(0, stringValue.indexOf(":"));
                        String localPart = stringValue.substring(stringValue.indexOf(":") + 1);
                        String namespaceUri = _oxpath.namespaceCtx.getNamespaceURI(prefix);
                        propertyName = new QName(namespaceUri, localPart, prefix);
                    } else {
                        propertyName = new QName(stringValue);
                    }
                } else if (args.get(0) instanceof QName) {
                    propertyName = (QName) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                if (propertyName == null) {
                    if (targetElmt != null) {
                        propertyName = new QName(targetElmt.getTextContent());
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:process-property function MUST be passed a single " +
                                        "element node."));
            }
            return _ectx.getPropertyValue(propertyName);
        }
    }

    public class DayTimeDuration implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 1)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "dayTimeDurationPropertyInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("dayTimeDuration call(context=" + _ectx + " args=" + args + ")");
            }

            String argument = null;
            Element targetElmt = null;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:dayTimeDuration function MUST be passed a single " +
                                            "element node."));
                    if (elmts.get(0) instanceof Element) {
                        targetElmt = (Element) elmts.get(0);
                    } else if (elmts.get(0) instanceof String) {
                        argument = (String) elmts.get(0);
                    }
                } else if (args.get(0) instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    targetElmt = (Element) args.get(0);
                } else if (args.get(0) instanceof String)	{
                    argument = (String) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                if (argument == null) {
                    if (targetElmt != null) {
                        argument = targetElmt.getTextContent();
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:dayTimeDuration function MUST be passed a single " +
                                        "element node."));
            }
            return DayTimeDurationValue.makeDayTimeDurationValue(argument);
        }
    }

    public class YearMonthDuration implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 1)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "yearMonthDurationPropertyInvalidSource"), "Invalid arguments"));

            if (__log.isDebugEnabled()) {
                __log.debug("yearMonthDuration call(context=" + _ectx + " args=" + args + ")");
            }

            String argument = null;
            Element targetElmt = null;
            try {
                if (args.get(0) instanceof List) {
                    List elmts = (List) args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                    "The bpws:yearMonthDuration function MUST be passed a single " +
                                            "element node."));
                    if (elmts.get(0) instanceof Element) {
                        targetElmt = (Element) elmts.get(0);
                    } else if (elmts.get(0) instanceof String) {
                        argument = (String) elmts.get(0);
                    }
                } else if (args.get(0) instanceof NodeWrapper) {
                    targetElmt = (Element) ((NodeWrapper) args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Element) {
                    targetElmt = (Element) args.get(0);
                } else if (args.get(0) instanceof String)	{
                    argument = (String) args.get(0);
                } else {
                    throw new XPathFunctionException("Unexpected argument type: "+args.get(0).getClass());
                }
                if (argument == null) {
                    if (targetElmt != null) {
                        argument = targetElmt.getTextContent();
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnInvalidExpressionValue,
                                "Invalid argument: URI Template expected. " + args.get(0), e));
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSelectionFailure,
                                "The bpws:yearMonthDuration function MUST be passed a single " +
                                        "element node."));
            }
            return YearMonthDurationValue.makeYearMonthDurationValue(argument);
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
         * Extract an integer from the given parameter.<br/>
         * The parameter could be:
         * <ol>
         * <li>a {@link java.util.List} containing exactly one {@link org.w3c.dom.Node}</li>
         * <li>a {@link net.sf.saxon.dom.NodeWrapper}</li>
         * <li>a {@link org.w3c.dom.Node}</li>
         * <li>a {@link String}</li>
         * <li>or an {@link Integer}</li>
         * </ol>
         * In the first 3 cases, if the {@linkplain org.w3c.dom.Node node} type is {@link Node#ELEMENT_NODE} the (trimmed) {@linkplain org.w3c.dom.Node#getTextContent() text content} is returned.
         * if the {@linkplain org.w3c.dom.Node node} type is {@link Node#TEXT_NODE} the (trimmed) {@linkplain org.w3c.dom.Text#getWholeText() text content} is returned.
         * <p/>
         *
         * @param arg
         * @return a string
         * @throws IllegalArgumentException if none of the conditions mentioned above are met
         */
        public static int extractInteger(Object arg) throws IllegalArgumentException {
            try {
                return Integer.parseInt(extractString(arg));
            } catch (ClassCastException cce) {
                try {
                    return (int) ((IntegerValue) arg).longValue();
                } catch (XPathException xpe) {
                    throw new IllegalArgumentException("Parameter MUST point to an integer, single element or text node.", xpe);
                } catch (ClassCastException ccce) {
                    throw new IllegalArgumentException("Parameter MUST point to an integer, single element or text node.", ccce);
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Parameter MUST point to an integer, single element or text node.", nfe);
            }
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
