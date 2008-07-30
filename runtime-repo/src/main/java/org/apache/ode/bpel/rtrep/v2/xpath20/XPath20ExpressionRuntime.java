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

import net.sf.saxon.trans.DynamicError;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.xpath.XPathEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath20.compiler.WrappedResolverException;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.ExpressionLanguageRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.xsd.Duration;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;

/**
 * XPath 2.0 Expression Language run-time subsytem.
 * Saxon implementation.
 */
public class XPath20ExpressionRuntime implements ExpressionLanguageRuntime {

    static final short NODE_TYPE = 1;
    static final short NODESET_TYPE = 2;
    static final short STRING_TYPE = 3;
    static final short BOOLEAN_TYPE = 4;
    static final short NUMBER_TYPE = 5;

    /** Class-level logger. */
    private static final Log __log = LogFactory.getLog(XPath20ExpressionRuntime.class);

    public XPath20ExpressionRuntime(){
    }

    public void initialize(Map properties) throws ConfigurationException {
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);
    }

    /**
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluateAsString(org.apache.ode.bpel.o.OExpression, org.apache.ode.bpel.explang.EvaluationContext)
     */
    public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException{
        return (String)evaluate(cexp, ctx, XPathConstants.STRING);
    }

    /**
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluateAsBoolean(org.apache.ode.bpel.o.OExpression, org.apache.ode.bpel.explang.EvaluationContext)
     */
    public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException{
        return (Boolean) evaluate(cexp, ctx, XPathConstants.BOOLEAN);
    }

    public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException {
        return (Number) evaluate(cexp, ctx, XPathConstants.NUMBER);
    }

    /**
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluate(org.apache.ode.bpel.o.OExpression, org.apache.ode.bpel.explang.EvaluationContext)
     */
    public List evaluate(OExpression cexp, EvaluationContext ctx) throws FaultException {
        List result;
        Object someRes = evaluate(cexp, ctx, XPathConstants.NODESET);
        if (someRes instanceof List) {
            result = (List) someRes;
            __log.debug("Returned list of size " + result.size());
            if ((result.size() == 1) && !(result.get(0) instanceof Node)) {
                // Dealing with a Java class
                Object simpleType = result.get(0);
                // Dates get a separate treatment as we don't want to call toString on them
                String textVal;
                if (simpleType instanceof Date)
                    textVal = ISO8601DateParser.format((Date) simpleType);
                else if (simpleType instanceof DurationValue)
                    textVal = ((DurationValue)simpleType).getStringValue();
                else
                    textVal = simpleType.toString();

                // Wrapping in a document
                Document d = DOMUtils.newDocument();
                // Giving our node a parent just in case it's an LValue expression
                Element wrapper = d.createElement("wrapper");
                Text text = d.createTextNode(textVal);
                wrapper.appendChild(text);
                d.appendChild(wrapper);
                result = Collections.singletonList(text);
            }
        } else if (someRes instanceof NodeList) {
            NodeList retVal = (NodeList) someRes;
            __log.debug("Returned node list of size " + retVal.getLength());
            result = new ArrayList(retVal.getLength());
            for(int m = 0; m < retVal.getLength(); ++m) {
                Node val = retVal.item(m);
                if (val.getNodeType() == Node.DOCUMENT_NODE) val = ((Document)val).getDocumentElement();
                result.add(val);
            }
        } else {
            result = null;
        }

        return result;
    }

    public Node evaluateNode(OExpression cexp, EvaluationContext ctx) throws FaultException  {
        List retVal = evaluate(cexp, ctx);
        if (retVal.size() == 0)
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, "No results for expression: " + cexp);
        if (retVal.size() > 1)
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, "Multiple results for expression: " + cexp);
        return (Node) retVal.get(0);
    }

    public Calendar evaluateAsDate(OExpression cexp, EvaluationContext context) throws FaultException {
        List literal = DOMUtils.toList(evaluate(cexp, context, XPathConstants.NODESET));
        if (literal.size() == 0)
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, "No results for expression: " + cexp);
        if (literal.size() > 1)
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, "Multiple results for expression: " + cexp);

        Object date =literal.get(0);
        if (date instanceof Calendar) return (Calendar) date;
        if (date instanceof Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) date);
            return cal;
        }
        if (date instanceof Element) date = ((Element)date).getTextContent();

        try {
            return ISO8601DateParser.parseCal(date.toString());
        } catch (Exception ex) {
            String errmsg = "Invalid date format: " + literal;
            __log.error(errmsg);
            throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue,errmsg);
        }
    }

    public Duration evaluateAsDuration(OExpression cexp, EvaluationContext context) throws FaultException  {
        String literal = this.evaluateAsString(cexp, context);
        try {
            return new Duration(literal);
        } catch (Exception ex) {
            String errmsg = "Invalid duration: " + literal;
            __log.error(errmsg, ex);
            throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue,errmsg);
        }
    }

    private Object evaluate(OExpression cexp, EvaluationContext ctx, QName type) throws FaultException {
        try {
            net.sf.saxon.xpath.XPathFactoryImpl xpf = new net.sf.saxon.xpath.XPathFactoryImpl();

            OXPath20ExpressionBPEL20 oxpath20 = ((OXPath20ExpressionBPEL20) cexp);
            xpf.setXPathFunctionResolver(new JaxpFunctionResolver(ctx, oxpath20));
            xpf.setXPathVariableResolver(new JaxpVariableResolver(ctx, oxpath20));
            XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();
            xpe.setNamespaceContext(oxpath20.namespaceCtx);
            // Just checking that the expression is valid
            XPathExpression expr = xpe.compile(((OXPath10Expression)cexp).xpath);

            Object evalResult = expr.evaluate(ctx.getRootNode() == null ? DOMUtils.newDocument() : ctx.getRootNode(), type);
            if (evalResult != null && __log.isDebugEnabled()) {
                __log.debug("Expression " + cexp.toString() + " generated result " + evalResult
                        + " - type=" + evalResult.getClass().getName());
                if (ctx.getRootNode() != null)
                    __log.debug("Was using context node " + DOMUtils.domToString(ctx.getRootNode()));
            }
            return evalResult;
        } catch (XPathExpressionException e) {
            // Extracting the real cause from all this wrapping isn't a simple task
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof DynamicError) {
                Throwable th = ((DynamicError)cause).getException();
                if (th != null) {
                    cause = th;
                    if (cause.getCause() != null) cause = cause.getCause();
                }
            }
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, cause.getMessage(), cause);
        } catch (WrappedResolverException wre) {
            wre.printStackTrace();
            throw (FaultException)wre.getCause();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.getMessage(), t);
        }

    }

}
