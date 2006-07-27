/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.runtime;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.explang.ExpressionLanguageRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.xsd.Duration;
import org.apache.ode.utils.xsd.XMLCalendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.*;

/**
 * XPath 1.0 Expression Language run-time subsytem.
 */
public class XPath10ExpressionRuntime implements ExpressionLanguageRuntime {
  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(XPath10ExpressionRuntime.class);

  /** Compiled expression cache. */
  private final Map<String, XPath> _compiledExpressions = new HashMap<String, XPath>();

  /** Registered extension functions. */
  private final Map _extensionFunctions  = new HashMap();

  public void initialize(Map properties) throws ConfigurationException {
  }

  public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    try {
      return compile((OXPath10Expression) cexp).stringValueOf(createContext((OXPath10Expression) cexp, ctx));
    } catch (JaxenException e) {
      handleJaxenException(e);
    }
    throw new AssertionError("UNREACHABLE");
  }

  public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
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
        // Giving our node a parent just in case it's an LValue expression
        Element wrapper = d.createElement("wrapper");
        Text text = d.createTextNode(retVal.get(0).toString());
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
      Duration duration = new org.apache.ode.utils.xsd.Duration(literal);
      return duration;
    } catch (Exception ex) {
      __log.error("Date conversion error.", ex);
      throw new EvaluationException("Duration conversion error." ,ex);
    }
  }

	private Context createContext(OXPath10Expression oxpath, EvaluationContext ctx) {
    JaxenContexts bpelSupport = new JaxenContexts(oxpath, _extensionFunctions, ctx);
    ContextSupport support = new ContextSupport(new JaxenNamespaceContextAdapter(oxpath.namespaceCtx),
                                                bpelSupport, bpelSupport, 
                                                new BpelDocumentNavigator(ctx.getRootNode()));
    Context jctx = new Context(support);

    if (ctx.getRootNode() != null)
      jctx.setNodeSet(Collections.singletonList(ctx.getRootNode()));

    return jctx;
  }


  private XPath compile(OXPath10Expression exp) throws JaxenException {
    XPath xpath = _compiledExpressions.get(exp.xpath);
    if (xpath == null) {
      xpath = new DOMXPath(exp.xpath);
      synchronized(_compiledExpressions) {
        _compiledExpressions.put(exp.xpath, xpath);
      }
    }
    return xpath;
  }

  private void handleJaxenException(JaxenException je) throws EvaluationException,FaultException {
    if (je instanceof WrappedFaultException) {
      throw ((WrappedFaultException)je).getFaultException();
    } else if (je.getCause() instanceof WrappedFaultException) {
      throw ((WrappedFaultException)je.getCause())
            .getFaultException();
    } else {
      throw new EvaluationException(je.getMessage(),je);
    }

  }
}
