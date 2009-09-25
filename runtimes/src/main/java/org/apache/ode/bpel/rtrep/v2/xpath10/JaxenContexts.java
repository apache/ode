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
package org.apache.ode.bpel.rtrep.v2.xpath10;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.v2.*;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.FunctionContext;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;
import org.jaxen.XPathFunctionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the various JAXEN evaluation contexts in terms of the
 * {@link EvaluationContext}.
 */
class JaxenContexts implements FunctionContext, VariableContext {
    private static final Log __log = LogFactory.getLog(JaxenContexts.class);

    /** Static, thread-safe singleton implementing default XPath functions */
    private static final FunctionContext __defaultXPathFunctions = XPathFunctionContext.getInstance();

    private OXPath10Expression _oxpath;
    private EvaluationContext _xpathEvalCtx;
    private Function _getVariableProperty;
    private Function _getVariableData;
    private Function _getLinkStatus;
    private Function _doXslTransform;
    private Map _extensionFunctions;

    public JaxenContexts(OXPath10Expression oxpath,
                         Map extensionFunctions,
                         EvaluationContext xpathEvalCtx) {
        _oxpath = oxpath;
        _xpathEvalCtx = xpathEvalCtx;
        _extensionFunctions = extensionFunctions;
        _getVariableProperty = new BpelVariablePropertyFunction();
        _getVariableData = new BpelVariableDataFunction();
        _getLinkStatus = new GetLinkStatusFunction();
        _doXslTransform = new DoXslTransformFunction();
    }

    /**
     * @see org.jaxen.FunctionContext#getFunction(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public Function getFunction(String namespaceURI, String prefix,
                                String localName)
            throws UnresolvableException {
        if (__log.isDebugEnabled()) {
            __log.debug("getFunction(" + namespaceURI + "," + prefix + ","
                    + localName);
        }

        if ((namespaceURI != null)) {
            QName fnQName = new QName(namespaceURI, localName);

            if (fnQName.equals(_oxpath.qname_getVariableProperty))
                return _getVariableProperty;
            if (fnQName.equals(_oxpath.qname_getVariableData))
                return _getVariableData;
            if (fnQName.equals(_oxpath.qname_getLinkStatus))
                return _getLinkStatus;
            if (_oxpath instanceof OXPath10ExpressionBPEL20) {
                OXPath10ExpressionBPEL20 oxpath20 = (OXPath10ExpressionBPEL20) _oxpath;
                if (fnQName.equals(oxpath20.qname_doXslTransform)) {
                    return _doXslTransform;
                }
            }
            Function f = (Function)_extensionFunctions.get(localName);

            if (f != null) {
                return f;
            }
        }

        // Defer to the default XPath context.
        return __defaultXPathFunctions.getFunction(null, prefix, localName);
    }

    /**
     * @see org.jaxen.VariableContext#getVariableValue(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public Object getVariableValue(String namespaceURI, String prefix,
                                   String localName)
            throws UnresolvableException {
        if(!(_oxpath instanceof OXPath10ExpressionBPEL20)){
            throw new IllegalStateException("XPath variables not supported for bpel 1.1");
        }

        // Custom variables
        if ("ode".equals(prefix)) {
            if ("pid".equals(localName)) {
                return _xpathEvalCtx.getProcessId();
            }
        }

        OXPath10ExpressionBPEL20 expr = (OXPath10ExpressionBPEL20)_oxpath;
        if(expr.isJoinExpression){
            OLink olink = _oxpath.links.get(localName);

            try {
                return _xpathEvalCtx.isLinkActive(olink) ? Boolean.TRUE : Boolean.FALSE;
            } catch (FaultException e) {
                throw new WrappedFaultException.JaxenUnresolvableException(e);
            }
        }else{
            String varName;
            String partName;
            int dotloc = localName.indexOf('.');
            if (dotloc == -1) {
                varName = localName;
                partName = null;
            } else {
                varName = localName.substring(0, dotloc);
                partName = localName.substring(dotloc + 1);
            }
            OScope.Variable variable = _oxpath.vars.get(varName);
            OMessageVarType.Part part = partName == null ? null : ((OMessageVarType)variable.type).parts.get(partName);

            try{
                Node variableNode = _xpathEvalCtx.readVariable(variable, part);
                if (variableNode == null)
                    throw new WrappedFaultException.JaxenUnresolvableException(
                            new FaultException(variable.getOwner().constants.qnSelectionFailure,
                                    "Unknown variable " + localName));
                OVarType type = variable.type;
                if (type instanceof OMessageVarType) {
                    OMessageVarType.Part typePart = ((OMessageVarType)type).parts.get(partName);
                    if (typePart == null) {
                        throw new WrappedFaultException.JaxenUnresolvableException(
                                new FaultException(variable.getOwner().constants.qnSelectionFailure,
                                        "Unknown part " + partName + " for variable " + localName));
                    }
                    type = typePart.type;
                }

                if (_xpathEvalCtx.narrowTypes() && type instanceof OXsdTypeVarType && ((OXsdTypeVarType)type).simple) {
                    return variableNode.getTextContent();
                } else {
                    return variableNode;
                }
            }catch(FaultException e){
                __log.error("bpws:getVariableValue threw FaultException", e);
                throw new WrappedFaultException.JaxenUnresolvableException(e);
            }
        }
    }

    /**
     * bpws:getVariableData()
     */
    class BpelVariableDataFunction implements Function {
        public Object call(Context context, List args)
                throws FunctionCallException {
            if (__log.isDebugEnabled()) {
                __log.debug("call(context=" + context + " args=" + args + ")");
            }

            String varname  = (String) args.get(0);
            String partname = args.size() > 1 ? (String) args.get(1) : null;
            String xpathStr = args.size() > 2 ? (String)args.get(2) : null;

            OXPath10Expression.OSigGetVariableData sig = _oxpath.resolveGetVariableDataSig(varname,partname,xpathStr);
            if (sig == null) {
                String msg = "InternalError: Attempt to use an unknown getVariableData signature: " + args;
                if (__log.isFatalEnabled())
                    __log.fatal(msg);
                throw new FunctionCallException(msg);
            }

            try {
                Node ret = _xpathEvalCtx.readVariable(sig.variable, sig.part);
                if (sig.location != null)
                    ret = _xpathEvalCtx.evaluateQuery(ret, sig.location);

                if (__log.isDebugEnabled()) {
                    __log.debug("bpws:getVariableData(" + args +  ")' = " + ret);
                }

                return ret;
            } catch (FaultException e) {
                __log.error("bpws:getVariableData(" + args + ") threw FaultException", e);
                throw new WrappedFaultException.JaxenFunctionException(e);
            }
        }
    }

    /**
     * bpws:getVariableProperty()
     */
    class BpelVariablePropertyFunction implements Function {
        public Object call(Context context, List args)
                throws FunctionCallException {
            if (args.size() != 2) {
                throw new FunctionCallException("missing required arguments");
            }

            OScope.Variable var = _oxpath.vars.get(args.get(0));
            OProcess.OProperty property = _oxpath.properties.get(args.get(1));

            if (__log.isDebugEnabled()) {
                __log.debug("function call:'bpws:getVariableProperty(" + var + ","
                        + property + ")'");
            }

            try {
                return _xpathEvalCtx.readMessageProperty(var, property);
            } catch (FaultException e) {
                __log.error("bpws:getVariableProperty(" + args + ") threw FaultException", e);
                throw new WrappedFaultException.JaxenFunctionException(e);
            }
        }
    }

    class GetLinkStatusFunction implements Function {
        public Object call(Context context, List args)
                throws FunctionCallException {
            assert args.size() == 1;

            OLink olink = _oxpath.links.get(args.get(0));

            try {
                return _xpathEvalCtx.isLinkActive(olink) ? Boolean.TRUE : Boolean.FALSE;
            } catch (FaultException e) {
                __log.error("bpws:getLinkStatus(" + args + ") threw FaultException", e);
                throw new WrappedFaultException.JaxenFunctionException(e);
            }
        }
    }

    class DoXslTransformFunction implements Function {
        public Object call(Context context, List args) throws FunctionCallException {
            assert args.size() >= 2;
            assert args.size() % 2 == 0;
            if (__log.isDebugEnabled()) {
                __log.debug("call(context=" + context + " args=" + args + ")");
            }
            if(!(_oxpath instanceof OXPath10ExpressionBPEL20)) {
                throw new IllegalStateException("XPath function bpws:doXslTransform not supported in " +
                        "BPEL 1.1!");
            }

            Element varElmt;
            try {
                if (args.get(1) instanceof List) {
                    List elmts = (List)args.get(1);
                    if (elmts.size() != 1) throw new WrappedFaultException.JaxenFunctionException(
                            new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                    "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                            "element node."));
                    varElmt = (Element) elmts.get(0);
                } else {
                    varElmt = (Element) args.get(1);
                }
            } catch (ClassCastException e) {
                throw new WrappedFaultException.JaxenFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                        "element node."));
            }

            URI xslUri;
            try {
                xslUri = new URI((String) args.get(0));
            } catch (URISyntaxException use) {
                // Shouldn't happen, checked at compilation time
                throw new FunctionCallException("First parameter of the bpws:doXslTransform isn't a valid URI!", use);
            }
            OXslSheet xslSheet = _oxpath.xslSheets.get(xslUri);
            // Shouldn't happen, checked at compilation time
            if (xslSheet == null) throw new FunctionCallException("Couldn't find the XSL sheet " + args.get(0)
                    + ", process compilation or deployment was probably incomplete!");

            if (!(varElmt instanceof Element)) {
                throw new WrappedFaultException.JaxenFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnXsltInvalidSource,
                                "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                                        "element node."));
            }

            HashMap<QName, Object> parametersMap = null;
            if (args.size() > 2) {
                parametersMap = new HashMap<QName, Object>();
                for (int idx = 2; idx < args.size(); idx+=2) {
                    QName keyQName = _oxpath.namespaceCtx.derefQName((String) args.get(idx));
                    parametersMap.put(keyQName, args.get(idx + 1));
                }
            }

            Document varDoc = DOMUtils.newDocument();
            varDoc.appendChild(varDoc.importNode(varElmt, true));

            Object result;
            DOMSource source = new DOMSource(varDoc);
            XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(_oxpath, _xpathEvalCtx.getBaseResourceURI());
            XslTransformHandler.getInstance().cacheXSLSheet(_xpathEvalCtx.getBaseResourceURI(), xslUri, xslSheet.sheetBody, resolver);
            try {
                result = XslTransformHandler.getInstance().transform(_xpathEvalCtx.getBaseResourceURI(), xslUri, source, parametersMap, resolver);
            } catch (Exception e) {
                throw new org.apache.ode.bpel.rtrep.v1.xpath10.WrappedFaultException.JaxenFunctionException(
                        new FaultException(_oxpath.getOwner().constants.qnSubLanguageExecutionFault,
                                e.toString()));
            }
            return result;
        }
    }

}
