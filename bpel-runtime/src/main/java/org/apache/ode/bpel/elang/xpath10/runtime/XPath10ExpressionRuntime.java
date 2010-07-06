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
package org.apache.ode.bpel.elang.xpath10.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.explang.ExpressionLanguageRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.xsd.Duration;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.transform.TransformerFactory;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XPath 1.0 Expression Language run-time subsytem.
 */
public class XPath10ExpressionRuntime implements ExpressionLanguageRuntime {
    /** Class-level logger. */
    private static final Log __log = LogFactory.getLog(XPath10ExpressionRuntime.class);

    /** Compiled expression cache. */
    private final Map<String, XPath> _compiledExpressions = new HashMap<String, XPath>();

    /** Registered extension functions. */
    private final Map _extensionFunctions = new HashMap();

    public void initialize(Map properties) throws ConfigurationException {
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);
    }

    public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        try {
            return compile((OXPath10Expression) cexp).stringValueOf(createContext((OXPath10Expression) cexp, ctx));
        } catch (JaxenException e) {
            handleJaxenException(e);
        }
        throw new AssertionError("UNREACHABLE");
    }

    public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException,
            EvaluationException {
        try {
            return compile((OXPath10Expression) cexp).booleanValueOf(createContext((OXPath10Expression) cexp, ctx));
        } catch (JaxenException e) {
            handleJaxenException(e);
        }
        throw new AssertionError("UNREACHABLE");
    }

    public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        try {
            return compile((OXPath10Expression) cexp).numberValueOf(createContext((OXPath10Expression) cexp, ctx));
        } catch (JaxenException e) {
            handleJaxenException(e);
        }
        throw new AssertionError("UNREACHABLE");
    }

    public List evaluate(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        try {
            XPath compiledXPath = compile((OXPath10Expression) cexp);
            Context context = createContext((OXPath10Expression) cexp, ctx);

            List retVal = compiledXPath.selectNodes(context);

            if ((retVal.size() == 1) && !(retVal.get(0) instanceof Node)) {
                Document d = DOMUtils.newDocument();
                // Giving our node a parent just in case it's an LValue
                // expression
                Element wrapper = d.createElement("wrapper");
                Object ret = retVal.get(0);
                
                if (ret instanceof Double) {
                    // safely convert a double into a long if they are numerically equal. This
                    // makes 1 from 1.0, which is more reliable when calling web services.
                    if (Double.compare((Double)ret, Math.ceil((Double)ret)) == 0) {
                        // the double is actually an int/long
                        ret = ((Double)ret).longValue();
                    }
                }
                Text text = d.createTextNode(ret.toString());
                wrapper.appendChild(text);
                d.appendChild(wrapper);
                retVal = Collections.singletonList(text);
            }

            return retVal;

        } catch (JaxenException je) {
            handleJaxenException(je);
        }
        throw new AssertionError("UNREACHABLE");
    }

    public Node evaluateNode(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        List retVal = evaluate(cexp, ctx);
        if (retVal.size() == 0 || retVal.size() > 1) {
            StringBuffer msg = new StringBuffer((retVal.size() == 0) ? "No results for expression: '" : "Multiple results for expression: '");
            if (cexp instanceof OXPath10Expression) {
                msg.append(((OXPath10Expression)cexp).xpath);
            } else {
                msg.append(cexp.toString());                
            }
            msg.append("'");
            if (ctx.getRootNode() != null) {
                msg.append(" against '");
                msg.append(DOMUtils.domToString(ctx.getRootNode()));
                msg.append("'");
            }
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, msg.toString());
        }
            
        return (Node) retVal.get(0);
    }

    public Calendar evaluateAsDate(OExpression cexp, EvaluationContext context) throws FaultException,
            EvaluationException {

        String literal = evaluateAsString(cexp, context);
        try {
            return ISO8601DateParser.parseCal(literal);
        } catch (Exception ex) {
            String errmsg = "Invalid date: " + literal;
            __log.error(errmsg, ex);
            throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue, errmsg);
        }
    }

    public Duration evaluateAsDuration(OExpression cexp, EvaluationContext context) throws FaultException,
            EvaluationException {
        String literal = this.evaluateAsString(cexp, context);
        try {
            Duration duration = new org.apache.ode.utils.xsd.Duration(literal);
            return duration;
        } catch (Exception ex) {
            String errmsg = "Invalid duration: " + literal;
            __log.error(errmsg, ex);
            throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue, errmsg);
        }
    }

    private Context createContext(OXPath10Expression oxpath, EvaluationContext ctx) {
        JaxenContexts bpelSupport = new JaxenContexts(oxpath, _extensionFunctions, ctx);
        ContextSupport support = new ContextSupport(new JaxenNamespaceContextAdapter(oxpath.namespaceCtx), bpelSupport,
                bpelSupport, new BpelDocumentNavigator(ctx.getRootNode()));
        Context jctx = new Context(support);

        if (ctx.getRootNode() != null)
            jctx.setNodeSet(Collections.singletonList(ctx.getRootNode()));

        return jctx;
    }

    private XPath compile(OXPath10Expression exp) throws JaxenException {
        XPath xpath = _compiledExpressions.get(exp.xpath);
        if (xpath == null) {
            xpath = new DOMXPath(exp.xpath);
            synchronized (_compiledExpressions) {
                _compiledExpressions.put(exp.xpath, xpath);
            }
        }
        return xpath;
    }

    private void handleJaxenException(JaxenException je) throws EvaluationException, FaultException {
        if (je instanceof WrappedFaultException) {
            throw ((WrappedFaultException) je).getFaultException();
        } else if (je.getCause() instanceof WrappedFaultException) {
            throw ((WrappedFaultException) je.getCause()).getFaultException();
        } else {
            throw new EvaluationException(je.getMessage(), je);
        }

    }
}
