/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.Query;

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
	 * @see org.apache.ode.bom.api.Expression#getNode()
	 */
	public Node getNode() {
		return _node;
	}

	/**
	 * @see org.apache.ode.bom.api.Expression#setNode(org.w3c.dom.Node)
	 */
	public void setNode(Node node) {
		_node = node;
	}
}
