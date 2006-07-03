/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.explang;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.o.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Context for evaluating expressions. Implementations of the
 * {@link ExpressionLanguageRuntime} interface use this interface to access BPEL
 * variables, property sets and link statuses.
 */
public interface EvaluationContext {

  /**
   * Read the value of a BPEL variable.
   * 
   * @param variable
   *          variable to read
   * @param part
   *          the part (or <code>null</code>)
   * @return the value of the variable, wrapped in a <code>Node</code>
   */
  Node readVariable(OScope.Variable variable, OMessageVarType.Part part) throws FaultException;

  Node getPartData(Element message, OMessageVarType.Part part) throws FaultException;
  /**
   * Read the value of a BPEL property.
   * 
   * @param variable
   *          variable containing property
   * @param property
   *          property to read
   * @return value of the property
   */
  String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
      throws FaultException;

  /**
   * Obtain the status of a control link.
   * 
   * @param olink
   *          link to check
   * @return <code>true</code> if the link is active, <code>false</code>
   *         otherwise.
   */
  boolean isLinkActive(OLink olink) throws FaultException;

  /**
   * Obtain the root node.
   * 
   * @return
   */
  Node getRootNode();

  /**
   * Evaluate a query expression.
   * 
   * @param root
   *          the root context
   * @param expr
   *          the query expression
   * @return node returned by query
   */
  Node evaluateQuery(Node root, OExpression expr) throws FaultException;

}
