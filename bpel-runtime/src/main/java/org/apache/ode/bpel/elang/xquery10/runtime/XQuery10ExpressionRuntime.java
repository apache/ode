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
package org.apache.ode.bpel.elang.xquery10.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Validation;
import net.sf.saxon.trans.DynamicError;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.xqj.SaxonXQConnection;
import net.sf.saxon.xqj.SaxonXQDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.bpel.elang.xpath20.compiler.WrappedResolverException;
import org.apache.ode.bpel.elang.xpath20.runtime.JaxpFunctionResolver;
import org.apache.ode.bpel.elang.xpath20.runtime.JaxpVariableResolver;
import org.apache.ode.bpel.elang.xquery10.compiler.XQuery10BpelFunctions;
import org.apache.ode.bpel.elang.xquery10.o.OXQuery10ExpressionBPEL20;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.explang.ExpressionLanguageRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.xsd.Duration;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * XQuery 1.0 Expression Language run-time subsytem. Saxon implementation.
 */
@SuppressWarnings("deprecation")
public class XQuery10ExpressionRuntime implements ExpressionLanguageRuntime {
    static final short NODE_TYPE = 1;
    static final short NODESET_TYPE = 2;
    static final short STRING_TYPE = 3;
    static final short BOOLEAN_TYPE = 4;
    static final short NUMBER_TYPE = 5;

    /** Class-level logger. */
    private static final Log __log = LogFactory.getLog(XQuery10ExpressionRuntime.class);

    /**
     * Creates a new XQuery10ExpressionRuntime object.
     */
    public XQuery10ExpressionRuntime() {
    }

    /**
     * Initialize XSL Transformer
     *
     * @param properties properties 
     *
     * @throws ConfigurationException ConfigurationException 
     */
    public void initialize(Map properties) throws ConfigurationException {
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);
    }

    /**
     * 
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluateAsString(org.apache.ode.bpel.o.OExpression,
     *      org.apache.ode.bpel.explang.EvaluationContext)
     */
    public String evaluateAsString(OExpression cexp, EvaluationContext ctx)
        throws FaultException, EvaluationException {
        return (String) evaluate(cexp, ctx, XPathConstants.STRING);
    }

    /**
     * 
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluateAsBoolean(org.apache.ode.bpel.o.OExpression,
     *      org.apache.ode.bpel.explang.EvaluationContext)
     */
    public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx)
        throws FaultException, EvaluationException {
        return (Boolean) evaluate(cexp, ctx, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate expression and return a number
     *
     * @param cexp cexp 
     * @param ctx ctx 
     *
     * @return type
     *
     * @throws FaultException FaultException 
     * @throws EvaluationException EvaluationException 
     */
    public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx)
        throws FaultException, EvaluationException {
        return (Number) evaluate(cexp, ctx, XPathConstants.NUMBER);
    }

    /**
     * 
     * @see org.apache.ode.bpel.explang.ExpressionLanguageRuntime#evaluate(org.apache.ode.bpel.o.OExpression,
     *      org.apache.ode.bpel.explang.EvaluationContext)
     */
    public List evaluate(OExpression cexp, EvaluationContext ctx)
        throws FaultException, EvaluationException {
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

                if (simpleType instanceof Date) {
                    textVal = ISO8601DateParser.format((Date) simpleType);
                } else if (simpleType instanceof DurationValue) {
                    textVal = ((DurationValue) simpleType).getStringValue();
                } else {
                    textVal = simpleType.toString();
                }

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

            for (int m = 0; m < retVal.getLength(); ++m) {
                Node val = retVal.item(m);

                if (val.getNodeType() == Node.DOCUMENT_NODE) {
                    val = ((Document) val).getDocumentElement();
                }

                result.add(val);
            }
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Evaluate expression and return a node
     *
     * @param cexp cexp 
     * @param ctx ctx 
     *
     * @return type
     *
     * @throws FaultException FaultException 
     * @throws EvaluationException EvaluationException 
     */
    public Node evaluateNode(OExpression cexp, EvaluationContext ctx)
        throws FaultException, EvaluationException {
        List retVal = evaluate(cexp, ctx);

        if (retVal.size() == 0) {
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure,
                "No results for expression: " + cexp);
        }

        if (retVal.size() > 1) {
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure,
                "Multiple results for expression: " + cexp);
        }

        return (Node) retVal.get(0);
    }

    /**
     * Evaluate expression and return a date
     *
     * @param cexp cexp 
     * @param context context 
     *
     * @return type
     *
     * @throws FaultException FaultException 
     * @throws EvaluationException EvaluationException 
     */
    public Calendar evaluateAsDate(OExpression cexp, EvaluationContext context)
        throws FaultException, EvaluationException {
        List literal = DOMUtils.toList(evaluate(cexp, context,
                    XPathConstants.NODESET));

        if (literal.size() == 0) {
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure,
                "No results for expression: " + cexp);
        }

        if (literal.size() > 1) {
            throw new FaultException(cexp.getOwner().constants.qnSelectionFailure,
                "Multiple results for expression: " + cexp);
        }

        Object date = literal.get(0);

        if (date instanceof Calendar) {
            return (Calendar) date;
        }

        if (date instanceof Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) date);

            return cal;
        }

        if (date instanceof Element) {
            date = ((Element) date).getTextContent();
        }

        try {
            return ISO8601DateParser.parseCal(date.toString());
        } catch (Exception ex) {
            String errmsg = "Invalid date: " + literal;
            __log.error(errmsg, ex);
            throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue,
                errmsg);
        }
    }

    /**
     * Evaluate expression and return duration
     *
     * @param cexp cexp 
     * @param context context 
     *
     * @return type
     *
     * @throws FaultException FaultException 
     * @throws EvaluationException EvaluationException 
     */
    public Duration evaluateAsDuration(OExpression cexp,
        EvaluationContext context) throws FaultException, EvaluationException {
        String literal = this.evaluateAsString(cexp, context);

        try {
            return new Duration(literal);
        } catch (Exception ex) {
            String errmsg = "Invalid duration: " + literal;
            __log.error(errmsg, ex);
            throw new FaultException(cexp.getOwner().constants.qnInvalidExpressionValue,
                errmsg);
        }
    }

    /**
     * Evaluate expression and return opaque type
     *
     * @param cexp cexp 
     * @param ctx ctx 
     * @param type type 
     *
     * @return type
     *
     * @throws FaultException FaultException 
     * @throws EvaluationException EvaluationException 
     */
    private Object evaluate(OExpression cexp, EvaluationContext ctx, QName type)
        throws FaultException, EvaluationException {
        try {
            OXQuery10ExpressionBPEL20 oxquery10 = ((OXQuery10ExpressionBPEL20) cexp);

            XQDataSource xqds = new SaxonXQDataSource();
            XQConnection xqconn = xqds.getConnection();

            Configuration configuration = ((SaxonXQConnection) xqconn).getConfiguration();
            configuration.setAllNodesUntyped(true);
            configuration.setHostLanguage(Configuration.XQUERY);

            XQStaticContext staticEnv = xqconn.getStaticContext();

            NSContext nsContext = oxquery10.namespaceCtx;
            Set<String> prefixes = nsContext.getPrefixes();
            for (String prefix : prefixes) {
                String uri = nsContext.getNamespaceURI(prefix);
                staticEnv.declareNamespace(prefix, uri);
            }

            configuration.setSchemaValidationMode(Validation.SKIP);
            xqconn.setStaticContext(staticEnv);

            // Prepare expression, for starters
            String xquery = oxquery10.xquery.replaceFirst(
                    Constants.XQUERY_FUNCTION_HANDLER_COMPILER,
                    Constants.XQUERY_FUNCTION_HANDLER_RUNTIME);
            XQPreparedExpression exp = xqconn.prepareExpression(xquery);

            JaxpFunctionResolver funcResolver = new JaxpFunctionResolver(ctx,
                    oxquery10);
            JaxpVariableResolver variableResolver = new JaxpVariableResolver(ctx,
                    oxquery10, configuration);
            // Bind external variables to runtime values
            for (QName variable : exp.getAllUnboundExternalVariables()) {
            	// Evaluate referenced variable
                Object value = variableResolver.resolveVariable(variable);
                
                // Figure out type of variable
                XQSequenceType xqType = getItemType(xqconn, value);
                
                // Saxon doesn't like binding sequences to variables
                if (value instanceof Node) {
                	// a node is a node-list, but the inverse isn't true.
                	// so, if the value is truly a node, leave it alone.
                } else if (value instanceof NodeList) {
                    // So extract the first item from the node list
                	NodeList nodeList = (NodeList) value;
                	ArrayList nodeArray = new ArrayList();
                	for (int i = 0; i < nodeList.getLength(); i++) {
                		nodeArray.add(nodeList.item(i));
                	}
                	value = xqconn.createSequence(nodeArray.iterator());
                }
                
                
                // Bind value with external variable
                if (value != null && xqType != null) {
                	if (value instanceof XQSequence) {
                		exp.bindSequence(variable, (XQSequence) value);
                	} else {
                		if (xqType instanceof XQItemType) {
			                exp.bindObject(variable, value, (XQItemType) xqType);
                		}
                	}
                }
            }

            // Set context node
            Node contextNode = (ctx.getRootNode() == null)
	            ? DOMUtils.newDocument() : ctx.getRootNode();
            contextNode.setUserData(XQuery10BpelFunctions.USER_DATA_KEY_FUNCTION_RESOLVER,
                funcResolver, null);
            exp.bindItem(XQConstants.CONTEXT_ITEM,
                xqconn.createItemFromNode(contextNode, xqconn.createNodeType()));

            // Execute query
            XQResultSequence result = exp.executeQuery();

            // Cast Saxon result to Java result
            Object evalResult = getResultValue(type, result);

            if ((evalResult != null) && __log.isDebugEnabled()) {
                __log.debug("Expression " + cexp.toString() +
                    " generated result " + evalResult + " - type=" +
                    evalResult.getClass().getName());

                if (ctx.getRootNode() != null) {
                    __log.debug("Was using context node " +
                        DOMUtils.domToString(ctx.getRootNode()));
                }
            }

            return evalResult;
        } catch (XQException xqe) {
            // Extracting the real cause from all this wrapping isn't a simple task
            Throwable cause = (xqe.getCause() != null) ? xqe.getCause() : xqe;

            if (cause instanceof DynamicError) {
                Throwable th = ((DynamicError) cause).getException();

                if (th != null) {
                    cause = th;

                    if (cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                }
            }

            throw new EvaluationException(
                "Error while executing an XQuery expression: " + cause.toString(), cause);
        } catch (WrappedResolverException wre) {
            __log.debug("Could not evaluate expression because of ", wre);
            throw (FaultException) wre.getCause();
        }
    }

    /**
     * Return opaque object embedded in XQuery Item
     *
     * @param item item 
     *
     * @return type
     *
     * @throws XQException XQException 
     */
    private Object getItemValue(XQItem item) throws XQException {
        XQItemType itemType = item.getItemType();
        Object itemValue = null;

        switch (itemType.getBaseType()) {
        case XQItemType.XQBASETYPE_BOOLEAN:
            itemValue = item.getBoolean();

            break;

        case XQItemType.XQBASETYPE_DECIMAL:
            itemValue = item.getDouble();

            break;

        case XQItemType.XQBASETYPE_BYTE:
            itemValue = item.getByte();

            break;

        case XQItemType.XQBASETYPE_FLOAT:
            itemValue = item.getFloat();

            break;

        case XQItemType.XQBASETYPE_INT:
        case XQItemType.XQBASETYPE_INTEGER:
            itemValue = item.getInt();

            break;

        case XQItemType.XQBASETYPE_LONG:
            itemValue = item.getLong();

            break;

        case XQItemType.XQBASETYPE_ANYTYPE:
            itemValue = item.getNode();

            break;

        case XQItemType.XQBASETYPE_ANYURI:
            itemValue = item.getNodeUri();

            break;

        case XQItemType.XQBASETYPE_SHORT:
            itemValue = item.getShort();

            break;

        case XQItemType.XQBASETYPE_STRING:
        case XQItemType.XQBASETYPE_ANYATOMICTYPE:
            itemValue = item.getAtomicValue();

            break;
            
        }

        return itemValue;
    }
    
    /**
     * Return XQuery type corresponding to given value
     *
     * @param xqconn XQuery connection 
     * @param value value 
     *
     * @return type
     *
     * @throws XQException XQException 
     */
    private XQSequenceType getItemType(XQConnection xqconn, Object value) throws XQException {
        XQSequenceType xqType = null;
        if (value instanceof Long) {
            xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_LONG);
        } else if (value instanceof String) {
            xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_STRING);
        } else if (value instanceof Boolean) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN);
        } else if (value instanceof Date) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_DATETIME);
        } else if (value instanceof BigDecimal) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_DECIMAL);
        } else if (value instanceof Float) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_FLOAT);
        } else if (value instanceof URI) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_ANYURI);
        } else if (value instanceof QName) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_QNAME);
        } else if (value instanceof BigInteger) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_INT);
        } else if (value instanceof Integer) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_INTEGER);
        } else if (value instanceof Double) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_DOUBLE);
        } else if (value instanceof Byte) {
        	xqType = xqconn.createAtomicType(XQItemType.XQBASETYPE_BYTE);
        } else if (value instanceof Node) {
        	xqType = xqconn.createNodeType();
        } else if (value instanceof NodeList || value instanceof XQSequence) {
            XQItemType xqItemType = xqconn.createNodeType();
            xqType = xqconn.createSequenceType(xqItemType, XQSequenceType.OCC_ZERO_OR_MORE);
        }
        return xqType;
    }
    
    /**
     * Cast XQuery sequence into an opaque list 
     *
     * @param type type 
     * @param result result 
     *
     * @return value
     *
     * @throws XQException XQException 
     */
    private Object getResultValue(QName type, XQResultSequence result) throws XQException {
    	Document document = DOMUtils.newDocument();
    	Object resultValue = null;
        if (XPathConstants.NODESET.equals(type)) {
            List list = new ArrayList();

            while (result.next()) {
                Object itemValue = getItemValue(result.getItem());
                if (itemValue instanceof Document) {
                	itemValue = DOMUtils.cloneNode(document, ((Document) itemValue).getDocumentElement());
                } else if (itemValue instanceof Node) {
                	itemValue = DOMUtils.cloneNode(document, (Node) itemValue);
                }

                if (itemValue != null) {
                    list.add(itemValue);
                }
            }

            resultValue = list;
        } else if (XPathConstants.NODE.equals(type)) {
        	XQItem item = null;
    		if (result.count() > 0) {
    			result.first();
    			if (result.isOnItem()) {
    				item = result.getItem();
    			}
    		}
    		if (item != null) {
	            resultValue = getItemValue(item);
	            if (resultValue instanceof Node) {
	            	resultValue = DOMUtils.cloneNode(document, (Node) resultValue); 
	            }
    		}
        } else if (XPathConstants.STRING.equals(type)) {
        	resultValue = result.getSequenceAsString(new Properties());
        } else if (XPathConstants.NUMBER.equals(type)) {
        	resultValue = result.getSequenceAsString(new Properties());
    		resultValue = Integer.parseInt((String) resultValue);
        } else if (XPathConstants.BOOLEAN.equals(type)) {
        	resultValue = result.getSequenceAsString(new Properties());
    		resultValue = Boolean.parseBoolean((String) resultValue);
        }
    	return resultValue;
    }
}
