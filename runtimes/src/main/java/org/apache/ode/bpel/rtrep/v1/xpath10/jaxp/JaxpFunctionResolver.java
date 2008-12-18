/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.ode.bpel.rtrep.v1.xpath10.jaxp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.common.Constants;
import org.apache.ode.bpel.rtrep.v1.EvaluationContext;
import org.apache.ode.bpel.rtrep.v1.OLink;
import org.apache.ode.bpel.rtrep.v1.OProcess;
import org.apache.ode.bpel.rtrep.v1.OScope;
import org.apache.ode.bpel.rtrep.v1.OXslSheet;
import org.apache.ode.bpel.rtrep.v1.xpath10.OXPath10Expression;
import org.apache.ode.bpel.rtrep.v1.xpath10.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.rtrep.v1.xpath10.XslRuntimeUriResolver;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * JAXP function resolver for BPEL XPath functions
 */
public class JaxpFunctionResolver implements XPathFunctionResolver {

    private static final Log __log = LogFactory.getLog(JaxpFunctionResolver.class);

    private EvaluationContext _ectx;

    private OXPath10Expression _oxpath;

    public JaxpFunctionResolver(EvaluationContext ectx, OXPath10Expression oxpath) {
        _ectx = ectx;
        _oxpath = oxpath;
    }

    public XPathFunction resolveFunction(QName functionName, int arity) {
        __log.debug("JAXP runtime: Resolving function " + functionName);
        final String namespaceURI = functionName.getNamespaceURI();
        if (namespaceURI == null) {
            throw new NullPointerException("Undeclared namespace for " + functionName);
        }
        if (namespaceURI.equals(Namespaces.BPEL11_NS)
            || namespaceURI.equals(Namespaces.WS_BPEL_20_NS)
            || namespaceURI.equals(Namespaces.WSBPEL2_0_FINAL_EXEC)) {
            String localName = functionName.getLocalPart();
            if (Constants.EXT_FUNCTION_GETVARIABLEDATA.equals(localName)) {
                return new GetVariableData();
            }
            if (Constants.EXT_FUNCTION_GETVARIABLEPROPERTY.equals(localName)) {
                return new GetVariableProperty();
            }
            if (Constants.EXT_FUNCTION_GETLINKSTATUS.equals(localName)) {
                return new GetLinkStatus();
            }
            if (Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(localName)) {
                return new DoXslTransform();
            }
            throw new NullPointerException("Unknown BPEL function: " + functionName);
        }
        return null;
    }

    public class GetLinkStatus implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 1)
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS,
                    "getLinkStatusInvalidSource"), "Illegal Arguments"));

            OLink olink = _oxpath.links.get(args.get(0));
            try {
                return _ectx.isLinkActive(olink) ? Boolean.TRUE : Boolean.FALSE;
            } catch (FaultException e) {
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS,
                    "getLinkStatusInvalidSource"), e));
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

            OXPath10Expression.OSigGetVariableData sig =
                _oxpath.resolveGetVariableDataSig(varname, partname, xpathStr);
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
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS,
                    "getVariablePropertyInvalidSource"), "Missing required arguments"));
            }

            OScope.Variable var = _oxpath.vars.get(args.get(0));
            OProcess.OProperty property = _oxpath.properties.get(args.get(1));

            if (__log.isDebugEnabled()) {
                __log.debug("function call:'bpws:getVariableProperty(" + var + "," + property + ")'");
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
                throw new XPathFunctionException(new FaultException(new QName(Namespaces.ODE_EXTENSION_NS,
                    "doXslTransformInvalidSource"), "Invalid arguments"));

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
                    if (elmts.size() != 1)
                        throw new XPathFunctionException(new FaultException(
                            _oxpath.getOwner().constants.qnXsltInvalidSource,
                            "Second parameter of the bpws:doXslTransform function MUST point to a single "
                                + "element node."));
                    varElmt = (Element) elmts.get(0);
                } else {
                    varElmt = (Element) args.get(1);
                }
            } catch (ClassCastException e) {
                throw new XPathFunctionException(new FaultException(
                    _oxpath.getOwner().constants.qnXsltInvalidSource,
                    "Second parameter of the bpws:doXslTransform function MUST point to a single "
                        + "element node."));
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
            if (xslSheet == null)
                throw new XPathFunctionException("Couldn't find the XSL sheet " + args.get(0)
                    + ", process compilation or deployment was probably incomplete!");

            if (!(varElmt instanceof Element)) {
                throw new XPathFunctionException(new FaultException(
                    _oxpath.getOwner().constants.qnXsltInvalidSource,
                    "Second parameter of the bpws:doXslTransform function MUST point to a single "
                        + "element node."));
            }

            HashMap<QName, Object> parametersMap = null;
            if (args.size() > 2) {
                parametersMap = new HashMap<QName, Object>();
                for (int idx = 2; idx < args.size(); idx += 2) {
                    QName keyQName = _oxpath.namespaceCtx.derefQName((String) args.get(idx));
                    Object paramElmt;
                    if (args.get(idx + 1) instanceof List) {
                        paramElmt = ((List) args.get(idx + 1)).get(0);
                    } else
                        paramElmt = args.get(idx + 1);

                    parametersMap.put(keyQName, paramElmt);
                }
            }

            if (__log.isDebugEnabled())
                __log.debug("Executing XSL sheet " + args.get(0) + " on element " + DOMUtils.domToString(varElmt));

            Document varDoc = DOMUtils.newDocument();
            varDoc.appendChild(varDoc.importNode(varElmt, true));

            Object result;
            DOMSource source = new DOMSource(varDoc);
            XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(_oxpath, _ectx.getBaseResourceURI());
            XslTransformHandler.getInstance().cacheXSLSheet(_ectx.getBaseResourceURI(), xslUri, xslSheet.sheetBody, resolver);
            try {
                result = XslTransformHandler.getInstance().transform(_ectx.getBaseResourceURI(), xslUri, source, parametersMap, resolver);
            } catch (Exception e) {
                e.printStackTrace();
                throw new XPathFunctionException(new FaultException(
                    _oxpath.getOwner().constants.qnSubLanguageExecutionFault, e.toString()));
            }
            if (result instanceof Node)
                return ((Node) result).getChildNodes();
            else
                return result;
        }
    }

}
