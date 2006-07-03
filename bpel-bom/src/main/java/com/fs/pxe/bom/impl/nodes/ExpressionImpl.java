/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.Query;

import org.w3c.dom.Node;


/**
 * BPEL ExpressionImpl.
 */
public class ExpressionImpl extends BpelObjectImpl implements Expression,Query {

  private static final long serialVersionUID = -1L;

  private String _xpathString;
  private String _expressionLanguage;
  private Node _node;

  /**
   * Uses default expresion language
   *
   */
  public ExpressionImpl() {
    super();
  }
  
  public ExpressionImpl(String expressionLanguage) {
    super();
    _expressionLanguage = expressionLanguage;
  }

  public String getExpressionLanguage() {
    return _expressionLanguage;
  }

  public void setXPathString(String xpathString) {
    _xpathString = xpathString;
  }

  public String getXPathString() {
    return _xpathString;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    ExpressionImpl o = (ExpressionImpl) obj;

    return (hashCode() == o.hashCode()) && _xpathString.equals(o._xpathString)
            && getNamespaceContext().equals(o.getNamespaceContext());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return _xpathString.hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return _xpathString;
  }

	/**
	 * @see com.fs.pxe.bom.api.Expression#getNode()
	 */
	public Node getNode() {
		return _node;
	}

	/**
	 * @see com.fs.pxe.bom.api.Expression#setNode(org.w3c.dom.Node)
	 */
	public void setNode(Node node) {
		_node = node;
	}
}
