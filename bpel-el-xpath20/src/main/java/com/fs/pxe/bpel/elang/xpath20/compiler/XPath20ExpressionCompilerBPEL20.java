/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath20.compiler;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.capi.CompilerContext;
import com.fs.pxe.bpel.capi.ExpressionCompiler;
import com.fs.pxe.bpel.elang.xpath10.compiler.XPathMessages;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10Expression;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import com.fs.pxe.bpel.elang.xpath20.BpelFunctionLibrary;
import com.fs.pxe.bpel.elang.xpath20.Constants;
import com.fs.pxe.bpel.elang.xpath20.SaxonCompileContext;
import com.fs.pxe.bpel.elang.xpath20.runtime.XPath20ExpressionRuntime;
import com.fs.pxe.bpel.o.OExpression;
import com.fs.pxe.bpel.o.OLValueExpression;
import com.fs.utils.DOMUtils;
import com.fs.utils.msg.MessageBundle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.trans.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;


/**
 * XPath compiler based on the SAXON implementation.
 */
public class XPath20ExpressionCompilerBPEL20 implements ExpressionCompiler {

  private static final Log __log = LogFactory.getLog(XPath20ExpressionCompilerBPEL20.class);
  
  private static final QName _qnLinkStatus = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETLINKSTATUS);
  private static final QName _qnVarProp = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETVARIABLEPROPRTY);
  private static final QName _qnVarData = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETVARIABLEDATA);
  private static final QName _qnXslTransform = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_DOXSLTRANSFORM);

  private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

  private CompilerContext _compilerContext;
  
  private Map<String, String> _properties = new HashMap<String,String>();

  public XPath20ExpressionCompilerBPEL20() {
    super();
    _properties.put("runtime-class", XPath20ExpressionRuntime.class.getName());
  }

  public void setCompilerContext(CompilerContext compilerContext) {
    _compilerContext = compilerContext;
  }
  
  /**
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compileJoinCondition(java.lang.Object)
   */
  public OExpression compileJoinCondition(Object source) throws CompilationException {
  	return _compile((Expression)source, true);
  }
  
  /**
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
   */
  public OExpression compile(Object source) throws CompilationException {
  	return _compile((Expression)source, false);
  }
  /**
	 * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compileLValue(java.lang.Object)
	 */
	public OLValueExpression compileLValue(Object source) throws CompilationException {
		return (OLValueExpression)_compile((Expression)source, false);
	}

  /**
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
   */
  private OExpression _compile(Expression xpath, boolean isJoinCondition) throws CompilationException {
    OXPath10Expression oexp = new OXPath10ExpressionBPEL20(
            _compilerContext.getOProcess(),
            _qnVarData,
            _qnVarProp,
            _qnLinkStatus,
            _qnXslTransform,
            isJoinCondition);
    oexp.namespaceCtx = xpath.getNamespaceContext();
    doSaxonCompile(oexp, xpath);
    return oexp;
  }

  /**
   * Verifies validity of a xpath expression.
   */
  private void doSaxonCompile(OXPath10Expression out, Expression source)
                               throws CompilationException {
    String xpathStr = source.getXPathString();
    if(xpathStr == null){
      Node node = source.getNode();
      if(node == null){
        throw new IllegalStateException("XPath string and xpath node are both null");
      }
      if(node.getNodeType() != Node.TEXT_NODE){
        throw new CompilationException(__msgs.errUnexpectedNodeTypeForXPath(DOMUtils.domToString(node)));
      }
      xpathStr = node.getNodeValue();
    }
    xpathStr = xpathStr.trim();
    
    try {
      // TODO: REMOVE THIS UGLY HACK
      if (xpathStr.startsWith("/")) {
        xpathStr = "/message" + xpathStr;
      }
      out.xpath = xpathStr;
      validateExpression(SaxonCompileContext.compileExpression(out), out);
      
    } catch(XPathException e){
      __log.error(e);
    	throw new CompilationException(__msgs.errXPathSyntax(xpathStr));
    }
  }

  private void validateExpression(net.sf.saxon.expr.Expression expr, OXPath10Expression oxpath) throws CompilationException, XPathException {
    if(expr instanceof BpelFunctionLibrary.BaseBpelFunction){
      BpelFunctionLibrary.BaseBpelFunction bfunc = 
        (BpelFunctionLibrary.BaseBpelFunction)expr;
      bfunc.compile(_compilerContext, oxpath);
    }
    
    for(Iterator iter = expr.iterateSubExpressions(); iter.hasNext(); ){
  		  net.sf.saxon.expr.Expression sub = 
        (net.sf.saxon.expr.Expression)iter.next();
        validateExpression(sub, oxpath);
    }
  }

	/**
	 * @see com.fs.pxe.bpel.capi.ExpressionCompiler#getProperties()
	 */
	public Map<String, String> getProperties() {
		return _properties;
	}

}
