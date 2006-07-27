/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.compiler;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.capi.CompilerContext;
import org.apache.ode.bpel.capi.ExpressionCompiler;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.runtime.XPath10ExpressionRuntime;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.jaxen.Function;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;


/**
 * XPath compiler based on the JAXEN implementation. Supports both 2.0 and 1.1 BPEL.
 */
public abstract class XPath10ExpressionCompilerImpl implements ExpressionCompiler {

  private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

  private HashMap<String,Function> _extensionFunctions = new HashMap<String,Function>();
  protected CompilerContext _compilerContext;

  /** Namespace of the BPEL functions (for v2 to v1 compatibility) . */
  private String _bpelNsURI;

  protected QName _qnFnGetVariableData;
  protected QName _qnFnGetVariableProperty;
  protected QName _qnFnGetLinkStatus;

  protected Map<String,String> _properties = new HashMap<String,String>();

  /**
   * Construtor.
   *
   * @param bpelNsURI the BPEL extension function namespace; varies depending on BPEL version.
   */
  public XPath10ExpressionCompilerImpl(String bpelNsURI) {
    _bpelNsURI = bpelNsURI;
    _qnFnGetVariableData = new QName(_bpelNsURI, "getVariableData");
    _qnFnGetVariableProperty = new QName(_bpelNsURI, "getVariableProperty");
    _qnFnGetLinkStatus = new QName(_bpelNsURI, "getLinkStatus");
    _properties.put("runtime-class", XPath10ExpressionRuntime.class.getName());
  }

  /**
   * @see org.apache.ode.bpel.capi.ExpressionCompiler#setCompilerContext(org.apache.ode.bpel.capi.CompilerContext)
   */
  public void setCompilerContext(CompilerContext compilerContext) {
    _compilerContext = compilerContext;
  }

  /**
   * @see org.apache.ode.bpel.capi.ExpressionCompiler#getProperties()
   */
  public Map<String,String> getProperties() {
    return _properties;
  }

  // TODO is this dead or just unfinished?
  private void registerExtensionFunction(String name, Class function) {
    try {
      Function jaxenFunction = (Function)function.newInstance();
      _extensionFunctions.put(name, jaxenFunction);
    } catch (InstantiationException e) {
      throw new RuntimeException("unexpected error creating extension function: "
                                 + name, e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("unexpected error creating extension function: "
                                 + name, e);
    } catch (ClassCastException e) {
      throw new RuntimeException("expected extension function of type "
                                 + Function.class.getName());
    }
  }

  /**
   * Verifies validity of a xpath expression.
   */
  protected void doJaxenCompile(OXPath10Expression out, Expression source)
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
      XPathReader reader = XPathReaderFactory.createReader();
      JaxenBpelHandler handler = new JaxenBpelHandler(_bpelNsURI, out, source.getNamespaceContext(), _compilerContext);
      reader.setXPathHandler(handler);

      reader.parse(xpathStr);
      out.xpath = xpathStr;
    } catch (CompilationExceptionWrapper e) {
      CompilationException ce = e.getCompilationException();
      if ( ce == null ) {
        ce = new CompilationException(__msgs.errUnexpectedCompilationError(e.getMessage()), e );
      }
      throw ce;
    } catch (SAXPathException e) {
      throw new CompilationException(__msgs.errXPathSyntax(xpathStr));
    }
  }


}
