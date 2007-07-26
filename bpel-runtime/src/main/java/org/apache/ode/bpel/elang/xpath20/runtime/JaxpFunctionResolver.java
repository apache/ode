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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

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
            }
        }

        return null;
    }

    public class GetLinkStatus implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            assert args.size() == 1;

            OLink olink = _oxpath.links.get(args.get(0));
            try {
                return _ectx.isLinkActive(olink) ? Boolean.TRUE : Boolean.FALSE;
            } catch (FaultException e) {
                throw new XPathFunctionException(e);
            }
        }
    }

    public class GetVariableData implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (__log.isDebugEnabled()) {
                __log.debug("call(context=" + _ectx + " args=" + args + ")");
            }

            String varname  = (String) args.get(0);
            String partname = args.size() > 1 ? (String) args.get(1) : null;
            String xpathStr = args.size() > 2 ? (String)args.get(2) : null;

            OXPath10Expression.OSigGetVariableData sig = _oxpath.resolveGetVariableDataSig(varname,partname,xpathStr);
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
                    __log.debug("bpws:getVariableData(" + args +  ")' = " + ret);
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
                throw new XPathFunctionException("missing required arguments");
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
            assert args.size() >= 2;
            assert args.size() % 2 == 0;
            if (__log.isDebugEnabled()) {
                __log.debug("call(context=" + _ectx + " args=" + args + ")");
            }
            if(!(_oxpath instanceof OXPath10ExpressionBPEL20)) {
                throw new IllegalStateException("XPath function bpws:doXslTransform not supported in " +
                        "BPEL 1.1!");
            }

            Element varElmt;
            try {
                if (args.get(1) instanceof List) {
                    List elmts = (List)args.get(1);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                    "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                            "element node."));
                    varElmt = (Element) elmts.get(0);
                } else {
                    if (args.get(1) instanceof NodeWrapper)
                        varElmt = (Element) ((NodeWrapper)args.get(1)).getUnderlyingNode();
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
                for (int idx = 2; idx < args.size(); idx+=2) {
                    QName keyQName = _oxpath.namespaceCtx.derefQName((String) args.get(idx));
                    Object paramElmt;
                    if (args.get(idx + 1) instanceof NodeWrapper) {
                        Element tmpElmt = (Element) ((NodeWrapper)args.get(idx + 1)).getUnderlyingNode();
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
            XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(_oxpath);
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

    /**
     * Compile time checking for the non standard ode:splitToElements function.
     */
    public class SplitToElements implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            assert args.size() > 2;
            assert args.size() < 5;
            if (__log.isDebugEnabled()) {
                __log.debug("splitToElements call(context=" + _ectx + " args=" + args + ")");
            }

            // Checking the first parameters, should be a proper element or a text node. Java verbosity at its best.
            String strToSplit = null;
            try {
                Node firstParam = null;
                if (args.get(0) instanceof List) {
                    List elmts = (List)args.get(0);
                    if (elmts.size() != 1) throw new XPathFunctionException(
                            new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "splitInvalidSource"),
                                    "First parameter of the ode:splitToElements function MUST point to a single " +
                                            "element or text node."));
                    firstParam = (Node) elmts.get(0);
                } else  if (args.get(0) instanceof NodeWrapper) {
                    firstParam = (Node) ((NodeWrapper)args.get(0)).getUnderlyingNode();
                } else if (args.get(0) instanceof Node) {
                    firstParam = (Node) args.get(0);
                } else {
                    strToSplit = (String) args.get(0);
                }

                if (strToSplit == null) {
                    if (Node.ELEMENT_NODE == firstParam.getNodeType()) {
                        strToSplit = firstParam.getTextContent().trim();
                    } else if (Node.TEXT_NODE == firstParam.getNodeType()) {
                        strToSplit = ((Text)firstParam).getWholeText().trim();
                    }
                }
            } catch (ClassCastException e) {
                throw new XPathFunctionException(
                        new FaultException(new QName(Namespaces.ODE_EXTENSION_NS, "splitInvalidSource"),
                                "First parameter of the ode:splitToElements function MUST point to a single " +
                                        "element node."));
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
}
