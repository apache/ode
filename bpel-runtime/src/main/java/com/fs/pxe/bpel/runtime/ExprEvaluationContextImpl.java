/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.explang.EvaluationContext;
import com.fs.pxe.bpel.o.*;
import com.fs.pxe.bpel.o.OMessageVarType.Part;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * The context in which BPEL expressions are evaluated. This class is handed of
 * the {@link com.fs.pxe.bpel.o.OExpression} instances to provide access to
 * variables, link statuses, and the like.
 */
public class ExprEvaluationContextImpl implements EvaluationContext {
  private static final Log __log = LogFactory
      .getLog(ExprEvaluationContextImpl.class);

  private BpelRuntimeContext _native;

  private ScopeFrame _scopeInstance;

  private Map<OLink, Boolean> _linkVals;

  private Node _root;

  public ExprEvaluationContextImpl(ScopeFrame scopeInstace,
      BpelRuntimeContext ntv) {
    _native = ntv;
    _scopeInstance = scopeInstace;
  }

  public ExprEvaluationContextImpl(ScopeFrame scopeInstace,
      BpelRuntimeContext ntv, Node root) {
    this(scopeInstace, ntv);
    _root = root;
  }

  public ExprEvaluationContextImpl(ScopeFrame scopeInstnce,
      BpelRuntimeContext ntv, Map<OLink, Boolean> linkVals) {
    this(scopeInstnce, ntv);
    _linkVals = linkVals;
  }

  public Node readVariable(OScope.Variable variable, OMessageVarType.Part part)
      throws FaultException {
    if (__log.isTraceEnabled())
      __log.trace("readVariable(" + variable + "," + part + ")");

    // TODO: check for null _scopeInstance

    Node ret;
    // TODO: catch resolveVariable(..) == null
    VariableInstance varInstance = _scopeInstance.resolve(variable);
    ret = _native.fetchVariableData(varInstance, part, false);
    return ret;
  }

  public Node evaluateQuery(Node root, OExpression expr) throws FaultException {
    try {
      return _native.getExpLangRuntime().evaluateNode(expr,
          new ExprEvaluationContextImpl(_scopeInstance, _native, root));
    } catch (com.fs.pxe.bpel.explang.EvaluationException e) {
      throw new InvalidProcessException("Expression Failed: " + expr, e);
    }
  }

  public String readMessageProperty(OScope.Variable variable,
      OProcess.OProperty property) throws FaultException {
    VariableInstance varInstance = _scopeInstance.resolve(variable);
    return _native.readProperty(varInstance, property);
  }

  public boolean isLinkActive(OLink olink) throws FaultException {
    return _linkVals.get(olink);
  }

  public String toString() {
    return "{ExprEvaluationContextImpl scopeInstance=" + _scopeInstance
        + ", activeLinks=" + _linkVals + "}";
  }

  public Node getRootNode() {
    return _root;
  }

  public Node getPartData(Element message, Part part) throws FaultException {
    return _native.getPartData(message, part);
  }

  public Long getProcessId() {
    return _native.getPid();
  }

}
