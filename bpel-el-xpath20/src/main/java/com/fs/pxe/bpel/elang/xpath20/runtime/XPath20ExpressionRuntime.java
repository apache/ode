/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath20.runtime;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10Expression;
import com.fs.pxe.bpel.elang.xpath20.SaxonCompileContext;
import com.fs.pxe.bpel.elang.xpath20.SaxonXPathContext;
import com.fs.pxe.bpel.explang.ConfigurationException;
import com.fs.pxe.bpel.explang.EvaluationContext;
import com.fs.pxe.bpel.explang.EvaluationException;
import com.fs.pxe.bpel.explang.ExpressionLanguageRuntime;
import com.fs.pxe.bpel.o.OExpression;
import com.fs.utils.DOMUtils;
import com.fs.utils.xsd.Duration;
import com.fs.utils.xsd.XMLCalendar;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceExtent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

  /** Compiled expression cache. */
  private final Map<String, Expression> _compiledExpressions = new HashMap<String, Expression>();

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
   * @see com.fs.pxe.bpel.explang.ExpressionLanguageRuntime#evaluateAsString(com.fs.pxe.bpel.o.OExpression, com.fs.pxe.bpel.explang.EvaluationContext)
   */
  public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
  	return (String)evaluate(cexp, ctx, STRING_TYPE);
  }

  /**
   * @see com.fs.pxe.bpel.explang.ExpressionLanguageRuntime#evaluateAsBoolean(com.fs.pxe.bpel.o.OExpression, com.fs.pxe.bpel.explang.EvaluationContext)
   */
  public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return ((Boolean)evaluate(cexp, ctx, BOOLEAN_TYPE)).booleanValue();
  }

  public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return (Number) evaluate(cexp, ctx, NUMBER_TYPE);
  }

  /**
   * @see com.fs.pxe.bpel.explang.ExpressionLanguageRuntime#evaluate(com.fs.pxe.bpel.o.OExpression, com.fs.pxe.bpel.explang.EvaluationContext)
   */
  public List evaluate(OExpression cexp, EvaluationContext ctx) throws FaultException, EvaluationException {
    return (List)evaluate(cexp, ctx, NODESET_TYPE);
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
      Duration duration = new com.fs.utils.xsd.Duration(literal);
      return duration;
    } catch (Exception ex) {
      __log.error("Date conversion error.", ex);
      throw new EvaluationException("Duration conversion error." ,ex);
    }
  }
  
  private Object evaluate(OExpression cexp, EvaluationContext ctx, short type) throws FaultException, EvaluationException {
    try {
      Expression compiledXPath = compile((OXPath10Expression) cexp);
      Node node = ctx.getRootNode();
      NodeInfo startNode = null;
      if(node != null){
        ExternalObjectModel model = _config.findExternalObjectModel(ctx.getRootNode());
        if (model == null) {
            throw new IllegalArgumentException(
                    "Cannot locate an object model implementation for nodes of class "
                    + node.getClass().getName());
        }
        DocumentInfo doc = model.wrapDocument(node, "", _config);
        startNode = model.wrapNode(doc, node);
      }
      
      SaxonXPathContext xpathCtx = new SaxonXPathContext((OXPath10Expression) cexp, ctx, startNode, _config);
      xpathCtx.openStackFrame(1);
      SequenceIterator iter = compiledXPath.iterate(xpathCtx);
      switch(type){
        case NODESET_TYPE: {
          SequenceExtent extent = new SequenceExtent(iter);
          List list = (List)extent.convertToJava(List.class, xpathCtx);
          if(list.size() == 1 && !(list.get(0) instanceof Node)){
          	Document doc = DOMUtils.newDocument();
            Element e = doc.createElement("foo");
            Node tnode= doc.createTextNode(list.get(0).toString());
            doc.appendChild(e);
            e.appendChild(tnode);
            return Collections.singletonList(tnode);
          }else{
          	return list;
          }
        }
//        case NODE_TYPE:
//        {
//           Item first = iter.next();
//           if (first == null){
//            return null;
//           }else if(first instanceof NodeInfo) {
//             return Value.convert(first);
//           }else{
//           	 Document doc = DOMUtils.newDocument();
//             return doc.createTextNode(first.getStringValue());
//           }
//        }
        case BOOLEAN_TYPE: {
          return Boolean.valueOf(ExpressionTool.effectiveBooleanValue(iter));
        } case NUMBER_TYPE: {
          Item first = iter.next();
          if (first != null) {
            Item typedItem = first.getTypedValue().next();
            if (typedItem != null && typedItem instanceof NumericValue) return ((NumericValue)typedItem).longValue();
          } else return null;
        } case STRING_TYPE: {
           Item first = iter.next();
           return first != null
             ? first.getStringValue()
             : null;
        }
        default:
          throw new IllegalArgumentException("Bad type");
      }
    } catch(FaultXPathException e){
      throw (FaultException)e.getCause();
    } catch (XPathException e) {
      throw new EvaluationException(e.getMessage(), e);
    } 
  }
  

  private Expression compile(OXPath10Expression exp) throws net.sf.saxon.trans.XPathException {
    Expression expr = _compiledExpressions.get(exp.xpath);
    if (expr == null) {
      expr = SaxonCompileContext.compileExpression(exp, _config);
      synchronized(_compiledExpressions) {
        _compiledExpressions.put(exp.xpath, expr);
      }
    }
    return expr;
  }


}
