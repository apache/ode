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

import net.sf.saxon.Configuration;
import net.sf.saxon.xpath.XPathEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath20.Constants;
import org.apache.ode.bpel.elang.xpath20.WrappedResolverException;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.explang.ExpressionLanguageRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.xsd.Duration;
import org.apache.ode.utils.xsd.XMLCalendar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    /** Registered extension functions. */
    // TODO unused as of now
    // private final HashMap _extensionFunctions  = new HashMap();

    private Configuration _config;

    public XPath20ExpressionRuntime(){
        _config = new Configuration();
    }

    public void initialize(Map properties) throws ConfigurationException {
    }

    /**
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluateAsString(org.apache.ode.bpel.o.OExpression, org.apache.ode.bpel.explang.EvaluationContext)
     */
    public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        return (String)evaluate(cexp, ctx, XPathConstants.STRING);
    }

    /**
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluateAsBoolean(org.apache.ode.bpel.o.OExpression, org.apache.ode.bpel.explang.EvaluationContext)
     */
    public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        return (Boolean) evaluate(cexp, ctx, XPathConstants.BOOLEAN);
    }

    public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        return (Number) evaluate(cexp, ctx, XPathConstants.NUMBER);
    }

    /**
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluate(org.apache.ode.bpel.o.OExpression, org.apache.ode.bpel.explang.EvaluationContext)
     */
    public List evaluate(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        List result = null;
        Object someRes = evaluate(cexp, ctx, XPathConstants.NODESET);
        if (someRes instanceof List) {
            result = (List) someRes;
            if ((result.size() == 1) && !(result.get(0) instanceof Node)) {
              Document d = DOMUtils.newDocument();
              // Giving our node a parent just in case it's an LValue expression
              Element wrapper = d.createElement("wrapper");
              Text text = d.createTextNode(result.get(0).toString());
              wrapper.appendChild(text);
              d.appendChild(wrapper);
              result = Collections.singletonList(text);
            }
        }
        if (someRes instanceof NodeList) {
            NodeList retVal = (NodeList) someRes;
            result = new ArrayList(retVal.getLength());
            for(int m = 0; m < retVal.getLength(); m++) {
                Node val = retVal.item(m);
                if (val.getNodeType() == Node.DOCUMENT_NODE) val = ((Document)val).getDocumentElement();
                result.add(val);
            }
        }

        return result;
    }

    public Node evaluateNode(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
        List retVal = evaluate(cexp, ctx);
        if (retVal.size() == 0)
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, "No results for expression: " + cexp);
        if (retVal.size() > 1)
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure, "Multiple results for expression: " + cexp);
        return (Node) retVal.get(0);
    }

    public Calendar evaluateAsDate(OExpression cexp, EvaluationContext context) throws FaultException, EvaluationException {

        String literal = evaluateAsString(cexp, context);

        try {
            return new XMLCalendar(literal);
        } catch (Exception ex) {
            __log.error("Date conversion error." ,ex);
            throw new EvaluationException("Date conversion errror.", ex);
        }
    }

    public Duration evaluateAsDuration(OExpression cexp, EvaluationContext context) throws FaultException, EvaluationException {
        String literal = this.evaluateAsString(cexp, context);
        try {
            return new Duration(literal);
        } catch (Exception ex) {
            __log.error("Date conversion error.", ex);
            throw new EvaluationException("Duration conversion error." ,ex);
        }
    }

    private Object evaluate(OExpression cexp, EvaluationContext ctx, QName type) throws FaultException, EvaluationException {
        try {
            net.sf.saxon.xpath.XPathFactoryImpl xpf = new net.sf.saxon.xpath.XPathFactoryImpl();

            OXPath20ExpressionBPEL20 oxpath20 = ((OXPath20ExpressionBPEL20) cexp);
            xpf.setXPathFunctionResolver(new JaxpFunctionResolver(ctx, oxpath20, Constants.BPEL20_NS));
            xpf.setXPathVariableResolver(new JaxpVariableResolver(ctx, oxpath20));
            XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();
            xpe.setNamespaceContext(oxpath20.namespaceCtx);
            // Just checking that the expression is valid
            XPathExpression expr = xpe.compile(((OXPath10Expression)cexp).xpath);

            Object evalResult = expr.evaluate(ctx.getRootNode() == null ? DOMUtils.newDocument() : ctx.getRootNode(), type);
            if (evalResult != null && __log.isDebugEnabled())
                __log.debug("Expression " + cexp.toString() + " generated result " + evalResult
                        + " - type=" + evalResult.getClass().getName());
            return evalResult;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new EvaluationException("Error while executing an XPath expression." ,e);
        } catch (WrappedResolverException wre) {
            wre.printStackTrace();
            throw (FaultException)wre.getCause();
        }

    }

}
