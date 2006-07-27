/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.capi;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bpel.o.*;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;


/**
 * Interface providing access to the compiler .
 */
public interface CompilerContext {
  
  OExpression constantExpr(boolean value);
  
  OExpression compileJoinCondition(Expression expr)
  				throws CompilationException;
  
  OExpression compileExpr(Expression expr)
          throws CompilationException;
  
  OLValueExpression compileLValueExpr(Expression expr)
  				throws CompilationException;

  OXslSheet compileXslt(String docStrUri)
          throws CompilationException;

  OXsdTypeVarType resolveXsdType(QName typeName)
          throws CompilationException;

  OProcess.OProperty resolveProperty(QName name)
          throws CompilationException;

  OScope.Variable resolveVariable(String name)
          throws CompilationException;

  OScope.Variable resolveMessageVariable(String inputVar)
          throws CompilationException;

  OScope.Variable resolveMessageVariable(String inputVar, QName messageType)
          throws CompilationException;

  OMessageVarType.Part resolvePart(OScope.Variable variable, String partname)
          throws CompilationException;

  OActivity compile(Activity child)
          throws CompilationException;

  OActivity compileSLC(final Activity source)
          throws CompilationException;
  
  OPartnerLink resolvePartnerLink(String name)
          throws CompilationException;

  Operation resolvePartnerRoleOperation(OPartnerLink partnerLink, String operationName)
          throws CompilationException;

  Operation resolveMyRoleOperation(OPartnerLink partnerLink, String operationName)
          throws CompilationException;

  OProcess.OPropertyAlias resolvePropertyAlias(OScope.Variable variable, QName property)
          throws CompilationException;

  void recoveredFromError(Object where, CompilationException bce)
          throws CompilationException;

  OLink resolveLink(String linkName)
          throws CompilationException;

  OScope resolveCompensatableScope(String scopeToCompensate)
          throws CompilationException;

  OProcess getOProcess()
          throws CompilationException;

  OScope.CorrelationSet resolveCorrelationSet(String csetName)
          throws CompilationException;

  String getSourceLocation();

  void compile(OActivity context, Runnable run);

  public boolean isPartnerLinkAssigned(String plink);
}
