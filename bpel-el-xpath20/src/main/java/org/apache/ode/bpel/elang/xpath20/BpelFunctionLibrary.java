/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath20;

import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.capi.CompilerContext;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.compiler.*;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.elang.xpath10.runtime.XslRuntimeUriResolver;
import org.apache.ode.bpel.elang.xpath20.runtime.FaultXPathException;
import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.xsl.XslTransformHandler;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.DOMUtils;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.*;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.DynamicError;
import net.sf.saxon.trans.StaticError;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TextFragmentValue;
import net.sf.saxon.value.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.StringWriter;
import java.io.IOException;

public class BpelFunctionLibrary implements FunctionLibrary {
  private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);
  private static final Log __log = LogFactory.getLog(BpelFunctionLibrary.class);

  private String _bpelNamespace;

  public BpelFunctionLibrary(String bpelNamespace) {
    _bpelNamespace = bpelNamespace;
  }

  /**
   * @see net.sf.saxon.functions.FunctionLibrary#isAvailable(int, java.lang.String, java.lang.String, int)
   */
  public boolean isAvailable(int fingerprint, String uri, String local,
                             int arity) {
    return _bpelNamespace.equals(uri) &&
      (local.equals(Constants.EXT_FUNCTION_GETVARIABLEDATA)
       || local.equals(Constants.EXT_FUNCTION_GETVARIABLEPROPRTY)
       || local.equals(Constants.EXT_FUNCTION_GETLINKSTATUS)
       || local.equals(Constants.EXT_FUNCTION_DOXSLTRANSFORM));
  }
  /**
   * @see net.sf.saxon.functions.FunctionLibrary#bind(int, java.lang.String, java.lang.String, net.sf.saxon.expr.Expression[])
   */
  public Expression bind(int nameCode, String uri, String local,
                         Expression[] staticArgs) throws XPathException {
    FunctionCall func = null;
    if(Constants.isBpelNamespace(uri)){
      if(Constants.EXT_FUNCTION_GETVARIABLEDATA.equals(local))
        func = new GetVariableFunc();
      else if(Constants.EXT_FUNCTION_GETVARIABLEPROPRTY.equals(local))
        func = new GetPropertyFunc();
      else if(Constants.EXT_FUNCTION_GETLINKSTATUS.equals(local))
        func = new LinkStatusFunc();
      else if(Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(local))
        func = new DoXslTransformFunc();
      else
        throw new StaticError("Bad bpel extension func: " + local);
      func.setArguments(staticArgs);
    }
    return func;
  }

  static SaxonXPathContext getCustomContext(XPathContext ctx){
    if(ctx == null)
      throw new IllegalStateException("Unable to find custom context");
    if(ctx instanceof SaxonXPathContext)
      return(SaxonXPathContext)ctx;
    return getCustomContext(ctx.getCaller());
  }

  public static abstract class BaseBpelFunction extends FunctionCall {
    protected StaticContext _sctx;
    public Expression preEvaluate(StaticContext env) throws XPathException {
      _sctx = env;
      return this;
    }

    public abstract void compile(CompilerContext cctx, OXPath10Expression oxpath) throws CompilationException,XPathException;
  }

  public static class LinkStatusFunc extends BaseBpelFunction {

    /**
     * @see net.sf.saxon.expr.FunctionCall#checkArguments(net.sf.saxon.expr.StaticContext)
     */
    protected void checkArguments(StaticContext env) throws XPathException {
    }

    /**
     * @see net.sf.saxon.expr.ComputedExpression#computeCardinality()
     */
    protected int computeCardinality() {
      return StaticProperty.ALLOWS_ONE;
    }

    /**
     * @see net.sf.saxon.expr.Expression#getItemType()
     */
    public ItemType getItemType() {
      return Type.BOOLEAN_TYPE;
    }


    public Item evaluateItem(XPathContext context) throws XPathException {
      SaxonXPathContext cctx = getCustomContext(context);

      String link = argument[0].evaluateAsString(context);
      OLink olink = cctx.oxpath.links.get(link);

      try {
        return cctx.evalCtx.isLinkActive(olink)
          ? BooleanValue.TRUE : BooleanValue.FALSE;
      } catch (FaultException e) {
        throw new FaultXPathException(e);
      }
    }

    public void compile(CompilerContext cctx, OXPath10Expression oxpath) throws CompilationException, XPathException {
      if (argument.length != 1)
        throw  new CompilationException(__msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETLINKSTATUS));

      String linkName = argument[0].evaluateAsString(null);
      OLink olink = cctx.resolveLink(linkName);
      oxpath.links.put(linkName, olink);
    }
  }

  public static class GetPropertyFunc extends BaseBpelFunction {

    /**
     * @see net.sf.saxon.expr.FunctionCall#checkArguments(net.sf.saxon.expr.StaticContext)
     */
    protected void checkArguments(StaticContext env) throws XPathException {}

    /**
     * @see net.sf.saxon.expr.ComputedExpression#computeCardinality()
     */
    protected int computeCardinality() {
      return StaticProperty.ALLOWS_ONE;
    }

    /**
     * @see net.sf.saxon.expr.Expression#getItemType()
     */
    public ItemType getItemType() {
      return Type.STRING_TYPE;
    }



    public Item evaluateItem(XPathContext context) throws XPathException {

      SaxonXPathContext cctx = getCustomContext(context);

      String varStr = argument[0].evaluateAsString(context);
      String varProp = argument[1].evaluateAsString(context);
      OScope.Variable var = cctx.oxpath.vars.get(varStr);
      OProcess.OProperty property = cctx.oxpath.properties.get(varProp);

      if (__log.isDebugEnabled()) {
        __log.debug("function call:'bpws:getVariableProperty(" + var + ","
                    + property + ")'");
      }

      try {
        String str = cctx.evalCtx.readMessageProperty(var, property);
        return new StringValue(str);
      } catch (FaultException e) {
        throw new FaultXPathException(e);
      }
    }

    public void compile(CompilerContext cctx, OXPath10Expression oxpath) throws CompilationException, XPathException {
      if(argument.length != 2) {
        throw new CompilationException(
                            __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETVARIABLEPROPRTY));
      }

      String varName = argument[0].evaluateAsString(null);
      OScope.Variable v = cctx.resolveVariable(varName);
      oxpath.vars.put(varName, v);

      String propName = argument[1].evaluateAsString(null);
      QName qname = oxpath.namespaceCtx.derefQName(propName);

      if (qname == null)
        throw new CompilationException(
                                  __msgs.errInvalidQName(propName));

      OProcess.OProperty property = cctx.resolveProperty(qname);
      // Make sure we can...
      cctx.resolvePropertyAlias(v, qname);

      oxpath.properties.put(propName, property);
      oxpath.vars.put(varName, v);
    }
  }

  public static class GetVariableFunc extends BaseBpelFunction {

    public Item evaluateItem(XPathContext context) throws XPathException {
      SaxonXPathContext cctx = getCustomContext(context);
      String varname  = argument[0].evaluateAsString(context);
      String partname = argument.length > 1
          ? argument[1].evaluateAsString(context)
          : null;
      String xpathStr = argument.length > 2
         ? argument[2].evaluateAsString(context)
         : null;

      OXPath10Expression.OSigGetVariableData sig = cctx.oxpath.resolveGetVariableDataSig(varname,partname,xpathStr);
      if (sig == null) {
        String msg = "InternalError: Attempt to use an unknown getVariableData signature: [ " + varname + "," + partname + "," + xpathStr + "]";
        if (__log.isFatalEnabled())
          __log.fatal(msg);
        throw new DynamicError(msg);
      }

      try {
        Node ret = cctx.evalCtx.readVariable(sig.variable, sig.part);
        if (sig.location != null)
          ret = cctx.evalCtx.evaluateQuery(ret, sig.location);

        if (__log.isDebugEnabled()) {
          __log.debug("bpws:getVariableData(" + varname + "," + partname + "," + xpathStr  +  ")' = " + ret);
        }
        DocumentWrapper wrapper = new DocumentWrapper(ret.getOwnerDocument(), "", cctx.getController().getConfiguration());

        return wrapper.wrap(ret);
      } catch (FaultException e) {
        __log.error("bpws:getVariableData(" + varname + "," + partname + "," + xpathStr  +  ") threw FaultException");

        throw new FaultXPathException(e);
      }
    }
    /**
     * @see net.sf.saxon.expr.FunctionCall#checkArguments(net.sf.saxon.expr.StaticContext)
     */
    protected void checkArguments(StaticContext env) throws XPathException {}

    /**
     * @see net.sf.saxon.expr.ComputedExpression#computeCardinality()
     */
    protected int computeCardinality() {
      return StaticProperty.ALLOWS_ONE;
    }

    /**
     * @see net.sf.saxon.expr.Expression#getItemType()
     */
    public ItemType getItemType() {
      return Type.NODE_TYPE;
    }
    /**
     * @see org.apache.ode.bpel.elang.xpath20.BpelFunctionLibrary.BaseBpelFunction#compile(org.apache.ode.bpel.capi.CompilerContext, org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression)
     */
    public void compile(CompilerContext cctx, OXPath10Expression oxpath) throws CompilationException, XPathException {


      if (argument.length < 1 || argument.length > 3) {
        throw new CompilationException(
                                  __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETVARIABLEDATA));

      }
      String varname = argument[0].evaluateAsString(null);
      String partname = argument.length > 1 ? argument[1].evaluateAsString(null) : null;
      String locationstr = argument.length > 2 ? argument[2].evaluateAsString(null) : null;

      OScope.Variable var = cctx.resolveVariable(varname);
      OMessageVarType.Part part = partname != null ? cctx.resolvePart(var,partname) : null;
      OExpression location = null;
      if (locationstr != null) {
        // Create a virtual expression node.
        ExpressionImpl vExpSrc = new ExpressionImpl(oxpath.expressionLanguage.expressionLanguageUri);
        vExpSrc.setNamespaceContext(oxpath.namespaceCtx);
        vExpSrc.setXPathString(locationstr);
        location = cctx.compileExpr(vExpSrc);
      }

      oxpath.addGetVariableDataSig(varname, partname, locationstr,
              new OXPath10Expression.OSigGetVariableData(cctx.getOProcess(),var, part,location));

    }

  }

  public static class DoXslTransformFunc extends BaseBpelFunction {

    public Item evaluateItem(XPathContext context) throws XPathException {
      assert argument.length >= 2;
      assert argument.length % 2 == 0;
      if (__log.isDebugEnabled()) {
        __log.debug("call(context=" + context + " args=" + argument + ")");
      }
      SaxonXPathContext cctx = getCustomContext(context);
      if(!(cctx.oxpath instanceof OXPath10ExpressionBPEL20)) {
        throw new IllegalStateException("XPath function bpws:doXslTransform not supported in " +
                "BPEL 1.1!");
      }

      Element varElmt;
      try {
        if (argument[1] instanceof List) {
          List elmts = (List) argument[1];
          if (elmts.size() != 1) throw new FaultXPathException(
                  new FaultException(cctx.oxpath.getOwner().constants.qnXsltInvalidSource,
                  "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                  "element node."));
          varElmt = (Element) elmts.get(0);
        } else {
          varElmt = (Element) argument[1];
        }
      } catch (ClassCastException e) {
        throw new FaultXPathException(
                  new FaultException(cctx.oxpath.getOwner().constants.qnXsltInvalidSource,
                  "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                  "element node."));
      }

      URI xslUri;
      try {
        xslUri = new URI(argument[1].evaluateAsString(null));
      } catch (URISyntaxException use) {
        // Shouldn't happen, checked at compilation time
        throw new DynamicError("First parameter of the bpws:doXslTransform isn't a valid URI!", use);
      }
      OXslSheet xslSheet = cctx.oxpath.xslSheets.get(xslUri);
      // Shouldn't happen, checked at compilation time
      if (xslSheet == null) throw new DynamicError("Couldn't find the XSL sheet " + argument[0]
              + ", process compilation or deployment was probably incomplete!");

      if (!(varElmt instanceof Element)) {
        throw new FaultXPathException(
                new FaultException(cctx.oxpath.getOwner().constants.qnXsltInvalidSource,
                "Second parameter of the bpws:doXslTransform function MUST point to a single " +
                "element node."));
      }

      HashMap<QName, Object> parametersMap = null;
      if (argument.length > 2) {
        parametersMap = new HashMap<QName, Object>();
        for (int idx = 2; idx < argument.length; idx+=2) {
          QName keyQName = cctx.oxpath.namespaceCtx.derefQName(argument[idx].evaluateAsString(null));
          parametersMap.put(keyQName, argument[idx + 1]);
        }
      }

      DOMSource source = new DOMSource(varElmt);
      // Using a StreamResult as a DOMResult doesn't behaves properly when the result
      // of the transformation is just a string.
      StringWriter writerResult = new StringWriter();
      StreamResult result = new StreamResult(writerResult);
      XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(cctx.oxpath);

      XslTransformHandler.getInstance().cacheXSLSheet(xslUri, xslSheet.sheetBody, resolver);
      try {
        XslTransformHandler.getInstance().transform(xslUri, source, result, parametersMap, resolver);
      } catch (Exception e) {
        throw new FaultXPathException(
                new FaultException(cctx.oxpath.getOwner().constants.qnSubLanguageExecutionFault,
                        e.toString()));
      }
      writerResult.flush();

      String output = writerResult.toString();
      // I'm not really proud of that but hey, it does the job and I don't think there's
      // any other easy way.
      if (output.startsWith("<?xml")) {
        try {
          Node ret = DOMUtils.stringToDOM(writerResult.toString());
          DocumentWrapper wrapper = new DocumentWrapper(ret.getOwnerDocument(), "", cctx.getController().getConfiguration());
          return wrapper.wrap(ret);
        } catch (SAXException e) {
          throw new DynamicError(e);
        } catch (IOException e) {
          throw new DynamicError(e);
        }
      } else {
        return new TextFragmentValue(output, "");
      }

    }

    public void compile(CompilerContext cctx, OXPath10Expression oxpath) throws CompilationException, XPathException {
      if (argument.length < 2 || argument.length % 2 != 0) {
        throw new CompilationException(
            __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_DOXSLTRANSFORM));
      }

      String xslUri = getLiteralFromExpression(argument[0]);
      OXslSheet xslSheet = cctx.compileXslt(xslUri);
      XslTransformHandler.getInstance().parseXSLSheet(xslSheet.uri, xslSheet.sheetBody,
              new XslCompileUriResolver(cctx, oxpath));

      oxpath.xslSheets.put(xslSheet.uri, xslSheet);
    }

    protected void checkArguments(StaticContext staticContext) throws XPathException { }

    protected int computeCardinality() {
      return StaticProperty.ALLOWS_ONE;
    }

    public ItemType getItemType() {
      return Type.NODE_TYPE;
    }

    private String getLiteralFromExpression(Expression expr)
            throws CompilationException, XPathException {
      expr = expr.simplify(_sctx);

      if (expr instanceof Value)
        return ((Value)expr).getStringValue();

      throw new CompilationException(__msgs.errLiteralExpected(expr.toString()));
    }

  }

  public FunctionLibrary copy() {
    return new BpelFunctionLibrary(_bpelNamespace);
  }
}
